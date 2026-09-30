package io.kommunicate.ui.conversation.voice;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** HTTP client for the Kommunicate voice-to-text and text-to-voice endpoints. */
public class KmVoiceApiClient {
    private static final String TAG = "KmVoiceMode";
    public static final String DEFAULT_BASE_URL = "https://omni-channel-test.kommunicate.io";
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 30_000;
    private static final int STT_SAMPLE_RATE = 16_000;
    private static final int TTS_SAMPLE_RATE = 24_000;

    private final String baseUrl;
    private volatile HttpURLConnection activeConnection;

    public KmVoiceApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public KmVoiceApiClient(@NonNull String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @NonNull
    public String transcribe(@NonNull byte[] pcmAudio, long conversationId) throws IOException {
        long startedAt = SystemClock.elapsedRealtime();
        Log.d(TAG, "stt_request_started audioBytes=" + pcmAudio.length);
        HttpURLConnection connection = openConnection("/voice/voice-to-text");
        activeConnection = connection;
        try {
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("X-Audio-Bits-Per-Sample", "16");
            connection.setRequestProperty("X-Audio-Channel-Count", "1");
            connection.setRequestProperty("X-Audio-Sample-Rate", String.valueOf(STT_SAMPLE_RATE));
            connection.setRequestProperty("X-Stt-Mode", "recognize");
            connection.setRequestProperty("X-Voice-Source", "web");
            connection.setRequestProperty("X-Voice-Ucid", String.valueOf(conversationId));
            connection.setFixedLengthStreamingMode(pcmAudio.length);
            writeRequest(connection, pcmAudio);

            byte[] response = readSuccessfulResponse(connection);
            try {
                Object payload = new JSONTokener(
                        new String(response, StandardCharsets.UTF_8)
                ).nextValue();
                String transcript = extractTranscript(payload, 0);
                Log.d(TAG, "stt_request_succeeded transcriptLength=" + transcript.length()
                        + " durationMs=" + (SystemClock.elapsedRealtime() - startedAt));
                return transcript;
            } catch (JSONException exception) {
                throw new IOException("Invalid voice-to-text response", exception);
            }
        } catch (IOException exception) {
            Log.e(TAG, "stt_request_failed durationMs="
                    + (SystemClock.elapsedRealtime() - startedAt), exception);
            throw exception;
        } finally {
            clearActiveConnection(connection);
            connection.disconnect();
        }
    }

    @NonNull
    public AudioResponse synthesize(@NonNull String text) throws IOException {
        long startedAt = SystemClock.elapsedRealtime();
        Log.d(TAG, "tts_request_started textLength=" + text.length());
        HttpURLConnection connection = openConnection("/voice/text-to-voice");
        activeConnection = connection;
        try {
            connection.setRequestProperty("Accept", "audio/mpeg, audio/*;q=0.9");
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject payload = new JSONObject();
            try {
                payload.put("text", text);
                payload.put("source", "web");
                payload.put("responseFormat", "binary");
                payload.put("sampleRate", TTS_SAMPLE_RATE);
            } catch (JSONException exception) {
                throw new IOException("Unable to create text-to-voice request", exception);
            }
            writeRequest(connection, payload.toString().getBytes(StandardCharsets.UTF_8));
            byte[] audio = readSuccessfulResponse(connection);
            if (audio.length == 0) {
                throw new IOException("Empty text-to-voice response");
            }
            Log.d(TAG, "tts_request_succeeded audioBytes=" + audio.length
                    + " contentType=" + connection.getContentType()
                    + " durationMs=" + (SystemClock.elapsedRealtime() - startedAt));
            return new AudioResponse(
                    audio,
                    connection.getContentType()
            );
        } catch (IOException exception) {
            Log.e(TAG, "tts_request_failed durationMs="
                    + (SystemClock.elapsedRealtime() - startedAt), exception);
            throw exception;
        } finally {
            clearActiveConnection(connection);
            connection.disconnect();
        }
    }

    public void cancelActiveRequest() {
        HttpURLConnection connection = activeConnection;
        if (connection != null) {
            connection.disconnect();
        }
    }

    private void clearActiveConnection(HttpURLConnection connection) {
        if (activeConnection == connection) {
            activeConnection = null;
        }
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        return connection;
    }

    private void writeRequest(HttpURLConnection connection, byte[] body) throws IOException {
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body);
        }
    }

    private byte[] readSuccessfulResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream responseStream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        byte[] response = responseStream == null ? new byte[0] : readFully(responseStream);
        if (responseCode < 200 || responseCode >= 300) {
            String message = new String(response, StandardCharsets.UTF_8);
            throw new IOException("Voice API request failed (" + responseCode + "): " + message);
        }
        return response;
    }

    private byte[] readFully(InputStream inputStream) throws IOException {
        try (InputStream stream = inputStream;
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8_192];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }

    @NonNull
    private String extractTranscript(Object payload, int depth) {
        if (payload == null || payload == JSONObject.NULL || depth > 3) {
            return "";
        }
        if (payload instanceof String) {
            return ((String) payload).trim();
        }
        if (payload instanceof JSONArray) {
            JSONArray values = (JSONArray) payload;
            StringBuilder transcript = new StringBuilder();
            for (int index = 0; index < values.length(); index++) {
                String part = extractTranscript(values.opt(index), depth + 1);
                if (!part.isEmpty()) {
                    if (transcript.length() > 0) {
                        transcript.append(' ');
                    }
                    transcript.append(part);
                }
            }
            return transcript.toString().trim();
        }
        if (!(payload instanceof JSONObject)) {
            return "";
        }

        JSONObject object = (JSONObject) payload;
        String[] directTextKeys = {"text", "transcript", "displayText"};
        for (String key : directTextKeys) {
            String text = extractTranscript(object.opt(key), depth + 1);
            if (!text.isEmpty()) {
                return text;
            }
        }

        JSONArray results = object.optJSONArray("results");
        if (results != null) {
            StringBuilder transcript = new StringBuilder();
            for (int index = 0; index < results.length(); index++) {
                JSONObject result = results.optJSONObject(index);
                JSONArray alternatives = result == null
                        ? null
                        : result.optJSONArray("alternatives");
                JSONObject firstAlternative = alternatives == null
                        ? null
                        : alternatives.optJSONObject(0);
                String part = firstAlternative == null
                        ? ""
                        : firstAlternative.optString("transcript", "").trim();
                if (!part.isEmpty()) {
                    if (transcript.length() > 0) {
                        transcript.append(' ');
                    }
                    transcript.append(part);
                }
            }
            if (transcript.length() > 0) {
                return transcript.toString();
            }
        }

        String[] nestedKeys = {"voiceToText", "response", "data", "result"};
        for (String key : nestedKeys) {
            String text = extractTranscript(object.opt(key), depth + 1);
            if (!text.isEmpty()) {
                return text;
            }
        }
        return extractTranscript(object.opt("message"), depth + 1);
    }

    public static class AudioResponse {
        private final byte[] audio;
        private final String contentType;

        AudioResponse(byte[] audio, String contentType) {
            this.audio = audio;
            this.contentType = contentType;
        }

        public byte[] getAudio() {
            return audio;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
