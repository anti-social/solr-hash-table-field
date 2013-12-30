package net.uaprom.util;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class HashTable {
    public byte[] bytes;
    public int offset;
    public int length;

    static int DUMMY = -1;
    static int PROBE = 5;

    public HashTable(int[] keys, float[] values) {
        int length = Math.min(keys.length, values.length);
        int size = getTableSize(length);
        int mask = size - 1;
        // 2 bytes - mask
        // 2 bytes - length
        // size * 4 - keys
        // size * 4 - values
        ByteBuffer buffer = ByteBuffer.allocate(2 + 2 + size * 2 * 4);
        buffer.putShort((short) length);
        buffer.putShort((short) mask);
        for (int i = 0; i < size; i++) {
            buffer.putInt(DUMMY);
            buffer.putFloat(0.0f);
        }

        int[] keyTable = new int[size];
        Arrays.fill(keyTable, DUMMY);
        for (int i = 0; i < length; i++) {
            int h = keys[i] & mask;
            while (keyTable[h] != DUMMY) {
                h = (h + PROBE) & mask;
            }
            buffer.position(4 + h * 8);
            buffer.putInt(keys[i]);
            buffer.putFloat(values[i]);
            keyTable[h] = keys[i];
        }

        this.bytes = buffer.array();
        this.offset = 0;
        this.length = buffer.capacity();
    }

    public HashTable(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public HashTable(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    public float get(int key) {
        return get(key, 0.0f);
    }

    public float get(int key, float defaultValue) {
        ByteBuffer buffer = ByteBuffer.wrap(this.bytes, this.offset, this.length);
        int length = buffer.getShort();
        int mask = buffer.getShort();

        int i, h, j, k;
        i = 0;
        h = key & mask;
        do {
            j = h;
            buffer.position(4 + j * 8);
            k = buffer.getInt();
            if (k == DUMMY || i == length) {
                return defaultValue;
            }
            h = (j + PROBE) & mask;
            i++;
        } while (key != k);

        buffer.position(4 + j * 8 + 4);
        return buffer.getFloat();
    }

    public boolean exists(int key) {
        float val = get(key, Float.NaN);
        if (Float.isNaN(val)) {
            return false;
        }
        return true;
    }

    public int len() {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        return buffer.getShort();
    }

    private int getTableSize(int count) {
        int size;
        int m = count;
        int i = 0;
        while (m > 1) {
            m = m / 2;
            i++;
        }
        size = 1 << i;
        if (count > 2 && count > size * 3 / 4) {
            size = size << 1;
        }
        return size;
    }
}
