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
        ByteBuffer buffer;
        int len = Math.min(keys.length, values.length);
        // special case: saves 4 bytes for hash table with one pair
        if (len == 1) {
            // 4 bytes - key
            // 4 bytes - value
            buffer = ByteBuffer.allocate(8);
            buffer.putInt(keys[0]);
            buffer.putFloat(values[0]);
        } else {
            int size = getTableSize(len);
            int mask = size - 1;
            // 2 bytes - mask
            // 2 bytes - len
            // size * 4 - keys
            // size * 4 - values
            buffer = ByteBuffer.allocate(2 + 2 + size * 2 * 4);
            buffer.putShort((short) len);
            buffer.putShort((short) mask);
            for (int i = 0; i < size; i++) {
                buffer.putInt(DUMMY);
                buffer.putFloat(0.0f);
            }

            int[] keyTable = new int[size];
            Arrays.fill(keyTable, DUMMY);
            for (int i = 0; i < len; i++) {
                int h = keys[i] & mask;
                while (keyTable[h] != DUMMY) {
                    h = (h + PROBE) & mask;
                }
                buffer.position(4 + h * 8);
                buffer.putInt(keys[i]);
                buffer.putFloat(values[i]);
                keyTable[h] = keys[i];
            }
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
        if (this.length == 8) {
            if (buffer.getInt() == key) {
                return buffer.getFloat();
            } else {
                return defaultValue;
            }
        }
        int len = buffer.getShort();
        int mask = buffer.getShort();

        int i, h, j, k;
        i = 0;
        h = key & mask;
        do {
            j = h;
            buffer.position(4 + j * 8);
            k = buffer.getInt();
            if (k == DUMMY || i == len) {
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
        ByteBuffer buffer = ByteBuffer.wrap(this.bytes, this.offset, this.length);
        if (this.length == 8) {
            return 1;
        }
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
