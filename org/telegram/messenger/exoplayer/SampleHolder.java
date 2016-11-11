package org.telegram.messenger.exoplayer;

import java.nio.ByteBuffer;

public final class SampleHolder {
    public static final int BUFFER_REPLACEMENT_MODE_DIRECT = 2;
    public static final int BUFFER_REPLACEMENT_MODE_DISABLED = 0;
    public static final int BUFFER_REPLACEMENT_MODE_NORMAL = 1;
    private final int bufferReplacementMode;
    public final CryptoInfo cryptoInfo;
    public ByteBuffer data;
    public int flags;
    public int size;
    public long timeUs;

    public SampleHolder(int bufferReplacementMode) {
        this.cryptoInfo = new CryptoInfo();
        this.bufferReplacementMode = bufferReplacementMode;
    }

    public void ensureSpaceForWrite(int length) throws IllegalStateException {
        if (this.data == null) {
            this.data = createReplacementBuffer(length);
            return;
        }
        int capacity = this.data.capacity();
        int position = this.data.position();
        int requiredCapacity = position + length;
        if (capacity < requiredCapacity) {
            ByteBuffer newData = createReplacementBuffer(requiredCapacity);
            if (position > 0) {
                this.data.position(BUFFER_REPLACEMENT_MODE_DISABLED);
                this.data.limit(position);
                newData.put(this.data);
            }
            this.data = newData;
        }
    }

    public boolean isEncrypted() {
        return (this.flags & BUFFER_REPLACEMENT_MODE_DIRECT) != 0;
    }

    public boolean isDecodeOnly() {
        return (this.flags & C0747C.SAMPLE_FLAG_DECODE_ONLY) != 0;
    }

    public boolean isSyncFrame() {
        return (this.flags & BUFFER_REPLACEMENT_MODE_NORMAL) != 0;
    }

    public void clearData() {
        if (this.data != null) {
            this.data.clear();
        }
    }

    private ByteBuffer createReplacementBuffer(int requiredCapacity) {
        if (this.bufferReplacementMode == BUFFER_REPLACEMENT_MODE_NORMAL) {
            return ByteBuffer.allocate(requiredCapacity);
        }
        if (this.bufferReplacementMode == BUFFER_REPLACEMENT_MODE_DIRECT) {
            return ByteBuffer.allocateDirect(requiredCapacity);
        }
        throw new IllegalStateException("Buffer too small (" + (this.data == null ? BUFFER_REPLACEMENT_MODE_DISABLED : this.data.capacity()) + " < " + requiredCapacity + ")");
    }
}
