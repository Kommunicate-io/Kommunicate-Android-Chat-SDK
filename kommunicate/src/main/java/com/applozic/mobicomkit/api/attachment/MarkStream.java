package com.applozic.mobicomkit.api.attachment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MarkStream extends InputStream {

    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int DEFAULT_LIMIT_INCREMENT = 1024;
    private final InputStream inputStream;

    private long offsetValue;
    private long resetValue;
    private long limit;
    private long defaultValue = -1;

    private boolean allowExpire = true;
    private int limitIncrement = -1;

    public MarkStream(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    public MarkStream(InputStream in, int size) {
        this(in, size, DEFAULT_LIMIT_INCREMENT);
    }

    private MarkStream(InputStream inputStream, int size, int limitIncrement) {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream, size);
        }
        this.inputStream = inputStream;
        this.limitIncrement = limitIncrement;
    }

    @Override
    public void mark(int readLimit) {
        defaultValue = setPos(readLimit);
    }

    public long setPos(int readLimit) {
        long offsetLimit = offsetValue + readLimit;
        if (limit < offsetLimit) {
            setLimit(offsetLimit);
        }
        return offsetValue;
    }

    public void allowMarksToExpire(boolean allowExpire) {
        this.allowExpire = allowExpire;
    }

    private void setLimit(long limit) {
        try {
            if (resetValue < offsetValue && offsetValue <= this.limit) {
                inputStream.reset();
                inputStream.mark((int) (limit - resetValue));
                skipBytes(resetValue, offsetValue);
            } else {
                resetValue = offsetValue;
                inputStream.mark((int) (limit - offsetValue));
            }
            this.limit = limit;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to mark: " + e);
        }
    }


    @Override
    public void reset() throws IOException {
        resetPos(defaultValue);
    }


    public void resetPos(long token) throws IOException {
        if (offsetValue > limit || token < resetValue) {
            throw new IOException("Cannot reset the pos ");
        }
        inputStream.reset();
        skipBytes(resetValue, token);
        offsetValue = token;
    }

    private void skipBytes(long current, long pos) throws IOException {
        while (current < pos) {
            long skipped = inputStream.skip(pos - current);
            if (skipped == 0) {
                if (read() == -1) {
                    break; // EOF
                } else {
                    skipped = 1;
                }
            }
            current += skipped;
        }
    }

    @Override
    public int read() throws IOException {
        if (!allowExpire && (offsetValue + 1 > limit)) {
            setLimit(limit + limitIncrement);
        }
        int result = inputStream.read();
        if (result != -1) {
            offsetValue++;
        }
        return result;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (!allowExpire && (offsetValue + buffer.length > limit)) {
            setLimit(offsetValue + buffer.length + limitIncrement);
        }
        int byteCount = inputStream.read(buffer);
        if (byteCount != -1) {
            offsetValue += byteCount;
        }
        return byteCount;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (!allowExpire && (this.offsetValue + length > limit)) {
            setLimit(this.offsetValue + length + limitIncrement);
        }
        int byteCount = inputStream.read(buffer, offset, length);
        if (byteCount != -1) {
            this.offsetValue += byteCount;
        }
        return byteCount;
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if (!allowExpire && (offsetValue + byteCount > limit)) {
            setLimit(offsetValue + byteCount + limitIncrement);
        }
        long skipped = inputStream.skip(byteCount);
        offsetValue += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}