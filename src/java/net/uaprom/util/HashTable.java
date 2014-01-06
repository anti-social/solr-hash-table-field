package net.uaprom.util;

import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Represents compact hash table with integer keys and float values.
 *
 * To save memory for small hash tables there are simple format.
 * For simple format it doesn't store mask and length of the hash table.
 *
 * For collision resolution it uses formula from python's dictobject.c:
 *
 *  j = (5*j) + 1 + perturb;
 *  perturb >>= PERTURB_SHIFT;
 *  use j % 2**i as the next table index;
 *
 * See https://github.com/python-git/python/blob/master/Objects/dictobject.c for more details
 */
public class HashTable {
    public byte[] bytes;
    public int offset;
    public int length;

    static int MAX_SIZE = 1 << 16;
    static int SIMPLE_FORMAT_THRESHOLD = 2;

    static int DUMMY = -1;

    static int PERTURB_SHIFT = 5;

    public HashTable(int[] keys, float[] values) {
        bytes = hcreate(keys, values);
        offset = 0;
        length = bytes.length;
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
        return hget(bytes, offset, length, key);
    }

    public float get(int key, float defaultValue) {
        return hget(bytes, offset, length, key, defaultValue);
    }

    public boolean exists(int key) {
        return hexists(bytes, offset, length, key);
    }

    public int len() {
        return hlen(bytes, offset, length);
    }

    // static methods to use without creating HashTable object
    public static byte[] hcreate(int[] keys, float[] values) {
        ByteBuffer buffer;
        int len = Math.min(keys.length, values.length);
        // special case: saves 4 bytes for hash table with one pair
        if (len <= SIMPLE_FORMAT_THRESHOLD) {
            // 4 bytes - key
            // 4 bytes - value
            // ...
            buffer = ByteBuffer.allocate(len * 8);
            for (int i = 0; i < len; i++) {
                buffer.putInt(keys[i]);
                buffer.putFloat(values[i]);
            }
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
                int h = keys[i];
                int perturb = h;
                int j = h & mask;
                while (keyTable[j] != DUMMY) {
                    h = 5 * h + perturb + 1;
                    perturb >>>= PERTURB_SHIFT;
                    j = h & mask;
                }
                buffer.position(4 + j * 8);
                buffer.putInt(keys[i]);
                buffer.putFloat(values[i]);
                keyTable[j] = keys[i];
            }
        }

        return buffer.array();
    }

    public static float hget(byte[] bytes, int key) {
        return hget(bytes, 0, bytes.length, key, 0.0f);
    }

    public static float hget(byte[] bytes, int key, float defaultValue) {
        return hget(bytes, 0, bytes.length, key, defaultValue);
    }

    public static float hget(byte[] bytes, int offset, int length, int key) {
        return hget(bytes, offset, length, key, 0.0f);
    }

    public static float hget(byte[] bytes, int offset, int length, int key, float defaultValue) {
        if (length == 0) {
            return defaultValue;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        if (length % 8 == 0) {
            int size = length / 8;
            for (int i = 0; i < size; i++) {
                buffer.position(i * 8);
                if (buffer.getInt() == key) {
                    return buffer.getFloat();
                }
            }
            return defaultValue;
        }
        int len = buffer.getShort();
        int mask = buffer.getShort();

        int j, k;
        int h = key;
        int perturb = h;
        do {
            j = h & mask;
            buffer.position(4 + j * 8);
            k = buffer.getInt();
            if (k == DUMMY) {
                return defaultValue;
            }
            h = 5 * h + perturb + 1;
            perturb >>>= PERTURB_SHIFT;
        } while (key != k);

        buffer.position(4 + j * 8 + 4);
        return buffer.getFloat();
    }

    public static int hlen(byte[] bytes) {
        return hlen(bytes, 0, bytes.length);
    }

    public static int hlen(byte[] bytes, int offset, int length) {
        if (length == 0) {
            return 0;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        if (length % 8 == 0) {
            return length / 8;
        }
        return buffer.getShort();
    }

    public static boolean hexists(byte[] bytes, int key) {
        return hexists(bytes, 0, bytes.length, key);
    }

    public static boolean hexists(byte[] bytes, int offset, int length, int key) {
        if (length == 0) {
            return false;
        }

        float val = hget(bytes, offset, length, key, Float.NaN);
        if (Float.isNaN(val)) {
            return false;
        }
        return true;
    }

    //private methods
    private static int getTableSize(int count) {
        int size;
        int m = count;
        int i = 0;
        while (m > 0) {
            m = m / 2;
            i++;
        }
        size = 1 << i;
        if (count > size * 3 / 4) {
            size = size << 1;
        }
        if (size > MAX_SIZE) {
            throw new RuntimeException("Maximum size of hash table: " + MAX_SIZE);
        }
        return size;
    }
}
