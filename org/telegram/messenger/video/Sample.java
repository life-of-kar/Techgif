package org.telegram.messenger.video;

public class Sample {
    private long offset;
    private long size;

    public Sample(long offset, long size) {
        this.offset = 0;
        this.size = 0;
        this.offset = offset;
        this.size = size;
    }

    public long getOffset() {
        return this.offset;
    }

    public long getSize() {
        return this.size;
    }
}
