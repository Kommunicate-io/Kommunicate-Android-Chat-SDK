package io.kommunicate.ui.conversation.voice;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/** Captures 16 kHz mono PCM and completes a segment after speech followed by silence. */
public class KmVoiceAudioRecorder {
    private static final String TAG = "KmVoiceMode";
    private static final int SAMPLE_RATE = 16_000;
    private static final int FRAME_SAMPLES = 320;
    private static final int VAD_CALIBRATION_DURATION_MS = 180;
    private static final int VAD_START_FRAMES = 3;
    private static final double VAD_START_FACTOR = 2.2;
    private static final double VAD_END_FACTOR = 1.8;
    private static final double VAD_NOISE_ALPHA = 0.95;
    private static final double VAD_CALIBRATION_ALPHA = 0.7;
    private static final double VAD_INITIAL_NOISE_RMS = 0.002 * Short.MAX_VALUE;
    private static final double VAD_MIN_NOISE_RMS = 0.00005 * Short.MAX_VALUE;
    private static final double VAD_MIN_START_RMS = 160;
    private static final int SILENCE_DURATION_MS = 600;
    private static final int INITIAL_SPEECH_TIMEOUT_MS = 5_000;
    private static final int MAX_SEGMENT_DURATION_MS = 30_000;
    private static final int PRE_ROLL_BYTES = SAMPLE_RATE;

    public interface Listener {
        void onAudioCaptured(@NonNull byte[] pcmAudio);

        void onNoSpeech();

        void onError(@NonNull Exception exception);
    }

    private final Listener listener;
    private volatile boolean recording;
    private volatile boolean cancelled;
    private volatile boolean noiseCalibrated;
    private volatile double estimatedNoiseRms = VAD_INITIAL_NOISE_RMS;
    private int recordingGeneration;
    private AudioRecord audioRecord;

    public KmVoiceAudioRecorder(@NonNull Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    public synchronized void start() {
        if (recording || audioRecord != null) {
            return;
        }
        int minimumBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        if (minimumBufferSize <= 0) {
            IllegalStateException exception =
                    new IllegalStateException("Unable to determine audio buffer size");
            Log.e(TAG, "recording_start_failed", exception);
            listener.onError(exception);
            return;
        }

        AudioRecord recorder = null;
        try {
            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    Math.max(minimumBufferSize, FRAME_SAMPLES * 4)
            );
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("Audio recorder initialization failed");
            }

            audioRecord = recorder;
            cancelled = false;
            recording = true;
            int generation = ++recordingGeneration;
            recorder.startRecording();
            AudioRecord activeRecorder = recorder;
            Thread recordingThread = new Thread(
                    () -> capture(activeRecorder, generation),
                    "KmVoiceRecorder"
            );
            recordingThread.start();
            Log.d(TAG, "recording_started sampleRate=16000 channels=1 encoding=pcm16");
        } catch (Exception exception) {
            recording = false;
            audioRecord = null;
            if (recorder != null) {
                recorder.release();
            }
            Log.e(TAG, "recording_start_failed", exception);
            listener.onError(exception);
        }
    }

    public synchronized void stop() {
        boolean wasRecording = recording;
        cancelled = true;
        recording = false;
        recordingGeneration++;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException ignored) {
            }
        }
        if (wasRecording) {
            Log.d(TAG, "recording_stopped");
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void resetNoiseCalibration() {
        noiseCalibrated = false;
        estimatedNoiseRms = VAD_INITIAL_NOISE_RMS;
    }

    private void capture(AudioRecord recorder, int generation) {
        short[] samples = new short[FRAME_SAMPLES];
        ByteArrayOutputStream speechAudio = new ByteArrayOutputStream();
        Deque<byte[]> preRoll = new ArrayDeque<>();
        int preRollSize = 0;
        boolean speechStarted = false;
        long startedAt = SystemClock.elapsedRealtime();
        long silenceStartedAt = 0;
        double noiseRms = estimatedNoiseRms;
        int speechStartFrames = 0;
        int maximumRms = 0;
        int calibrationFrameTarget = Math.max(
                1,
                VAD_CALIBRATION_DURATION_MS * SAMPLE_RATE / (FRAME_SAMPLES * 1_000)
        );
        int calibrationFrames = noiseCalibrated ? calibrationFrameTarget : 0;

        Exception captureError = null;
        try {
            while (recording) {
                int read = recorder.read(samples, 0, samples.length);
                if (read <= 0) {
                    if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                        throw new IllegalStateException("Unable to read microphone audio");
                    }
                    continue;
                }

                byte[] frame = toLittleEndianPcm(samples, read);
                int rms = calculateRms(samples, read);
                long now = SystemClock.elapsedRealtime();
                maximumRms = Math.max(maximumRms, rms);

                if (!speechStarted) {
                    preRoll.addLast(frame);
                    preRollSize += frame.length;
                    while (preRollSize > PRE_ROLL_BYTES && !preRoll.isEmpty()) {
                        preRollSize -= preRoll.removeFirst().length;
                    }

                    if (calibrationFrames < calibrationFrameTarget) {
                        noiseRms = updateNoiseEstimate(
                                noiseRms,
                                rms,
                                VAD_CALIBRATION_ALPHA,
                                VAD_MIN_NOISE_RMS
                        );
                        calibrationFrames++;
                    } else {
                        double startThreshold = Math.max(
                                noiseRms * VAD_START_FACTOR,
                                VAD_MIN_START_RMS
                        );
                        if (rms > startThreshold) {
                            speechStartFrames++;
                        } else {
                            speechStartFrames = 0;
                            noiseRms = updateNoiseEstimate(
                                    noiseRms,
                                    rms,
                                    VAD_NOISE_ALPHA,
                                    VAD_MIN_NOISE_RMS
                            );
                        }
                    }

                    if (speechStartFrames >= VAD_START_FRAMES) {
                        speechStarted = true;
                        for (byte[] bufferedFrame : preRoll) {
                            speechAudio.write(bufferedFrame, 0, bufferedFrame.length);
                        }
                        preRoll.clear();
                    } else if (now - startedAt >= INITIAL_SPEECH_TIMEOUT_MS) {
                        break;
                    }
                } else {
                    speechAudio.write(frame, 0, frame.length);
                    if (rms < noiseRms * VAD_END_FACTOR) {
                        if (silenceStartedAt == 0) {
                            silenceStartedAt = now;
                        } else if (now - silenceStartedAt >= SILENCE_DURATION_MS) {
                            break;
                        }
                    } else {
                        silenceStartedAt = 0;
                    }
                    if (now - startedAt >= MAX_SEGMENT_DURATION_MS) {
                        break;
                    }
                }
            }
        } catch (Exception exception) {
            captureError = exception;
        } finally {
            release(recorder);
        }

        if (!isCurrentRecording(generation)) {
            return;
        }
        estimatedNoiseRms = noiseRms;
        noiseCalibrated = true;
        if (captureError != null) {
            Log.e(TAG, "recording_failed", captureError);
            listener.onError(captureError);
            return;
        }
        byte[] audio = speechAudio.toByteArray();
        if (!speechStarted || audio.length < SAMPLE_RATE * 2 * 160 / 1_000) {
            Log.d(TAG, "recording_no_speech maxRms=" + maximumRms
                    + " noiseRms=" + Math.round(noiseRms));
            listener.onNoSpeech();
        } else {
            Log.d(TAG, "recording_captured audioBytes=" + audio.length
                    + " maxRms=" + maximumRms
                    + " noiseRms=" + Math.round(noiseRms));
            listener.onAudioCaptured(audio);
        }
    }

    private synchronized void release(AudioRecord recorder) {
        if (audioRecord == recorder) {
            recording = false;
            audioRecord = null;
        }
        try {
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop();
            }
        } catch (IllegalStateException ignored) {
        }
        recorder.release();
    }

    private synchronized boolean isCurrentRecording(int generation) {
        return !cancelled && recordingGeneration == generation;
    }

    private byte[] toLittleEndianPcm(short[] samples, int length) {
        byte[] pcm = new byte[length * 2];
        for (int index = 0; index < length; index++) {
            pcm[index * 2] = (byte) (samples[index] & 0xff);
            pcm[index * 2 + 1] = (byte) ((samples[index] >> 8) & 0xff);
        }
        return pcm;
    }

    private int calculateRms(short[] samples, int length) {
        long sum = 0;
        for (int index = 0; index < length; index++) {
            long sample = samples[index];
            sum += sample * sample;
        }
        return (int) Math.sqrt((double) sum / Math.max(length, 1));
    }

    private double updateNoiseEstimate(double currentValue,
                                       double measuredValue,
                                       double alpha,
                                       double minimumValue) {
        return Math.max(
                currentValue * alpha + measuredValue * (1 - alpha),
                minimumValue
        );
    }
}
