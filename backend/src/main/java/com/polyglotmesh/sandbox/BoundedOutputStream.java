package com.polyglotmesh.sandbox;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Collects guest output but stops growing once the limit is reached. */
public class BoundedOutputStream extends OutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final int maxBytes;
    private boolean truncated;

    public BoundedOutputStream(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public synchronized void write(int b) {
        if (buffer.size() < maxBytes) {
            buffer.write(b);
        } else {
            truncated = true;
        }
    }

    @Override
    public synchronized void write(byte[] bytes, int off, int len) {
        int room = maxBytes - buffer.size();
        if (room > 0) {
            buffer.write(bytes, off, Math.min(room, len));
        }
        if (len > room) {
            truncated = true;
        }
    }

    public synchronized String text() {
        String value = buffer.toString(StandardCharsets.UTF_8);
        return truncated ? value + "\n... output truncated ..." : value;
    }
}
