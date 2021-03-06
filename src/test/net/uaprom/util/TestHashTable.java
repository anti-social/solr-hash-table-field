package net.uaprom.util;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Set;
import java.util.HashSet;


@RunWith(JUnit4.class)
public class TestHashTable {
    static float DELTA = 0.000001f;

    private void testHashTable(int[] keys, float[] values) {
        testHashTable(keys, values, -1);
    }

    private void testHashTable(int[] keys, float[] values, int expectedSize) {
        testHashTable(keys, values, HashTable.DEFAULT_SIMPLE_FORMAT_THRESHOLD, expectedSize);
    }

    private void testHashTable(int[] keys, float[] values, int simpleFormatThreshold, int expectedSize) {
        HashTable t = new HashTable(keys, values, simpleFormatThreshold);
        if (expectedSize != -1) {
            assertEquals(expectedSize, t.bytes.length);
        }
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], t.get(keys[i]), DELTA);
            assertTrue(t.exists(keys[i]));
        }
        assertEquals(keys.length, t.len());
        assertEquals(-1.23456789f, t.get(123456789, -1.23456789f), DELTA);
        assertEquals(0.0f, t.get(123456789), DELTA);
        assertFalse(t.exists(123456789));
    }
    
    @Test
    public void test() {
        testHashTable(new int[] {5}, new float[] {5.5f}, 8);
        testHashTable(new int[] {1, 3}, new float[] {1.1f, 3.3f}, 16);
        testHashTable(new int[] {1, 3, 2}, new float[] {1.1f, 3.3f, 2.2f}, 36);
        testHashTable(new int[] {-1, 3, 7}, new float[] {1.1f, 3.3f, 7.7f}, 36);
        testHashTable(new int[] {1, 2, 3, 33}, new float[] {1.1f, 2.2f, 3.3f, 33.3f}, 68);
        testHashTable(new int[] {1, 2, 3, 33}, new float[] {1.1f, 2.2f, 3.3f, 33.3f}, 4, 32);
        testHashTable(new int[] {0, 16, 4, 20, 1}, new float[] {0.1f, 0.16f, 0.4f, 0.20f, 0.1f}, 68);
        testHashTable(new int[] {0, 16, 4, 20, 1, 17, 33},
                      new float[] {0.1f, 0.16f, 0.4f, 0.20f, 0.1f, 0.17f, 0.33f}, 132);
        testHashTable(new int[] {0, 16, 4, 20, 1, 17, 33, 65},
                      new float[] {0.1f, 0.16f, 0.4f, 0.20f, 0.1f, 0.17f, 0.33f, 0.65f}, 132);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDummyKey() {
        testHashTable(new int[] {Integer.MIN_VALUE}, new float[] {1.1f}, 8);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDummyKey2() {
        testHashTable(new int[] {Integer.MIN_VALUE, 0, 1}, new float[] {-1.1f, 0.0f, 1.1f}, 36);
    }

    @Test
    public void testRandom() {
        int N = 1000;
        int MAX_LENGTH = 10000;
        float MIN_VALUE = (float) Integer.MIN_VALUE;
        float MAX_VALUE = (float) Integer.MAX_VALUE;

        for (int i = 0; i < N; i++) {
            int length = 1 + (int) (Math.random() * MAX_LENGTH);
            int[] keys = new int[length];
            float[] values = new float[length];
            Set<Integer> uniqueKeys = new HashSet<Integer>();
            for (int j = 0; j < length; j++) {
                int k = (int) (MIN_VALUE + 1 + Math.random() * (MAX_VALUE - MIN_VALUE));
                if (uniqueKeys.contains(k)) {
                    continue;
                }
                uniqueKeys.add(k);
                keys[j] = k;
                values[j] = (float) (MIN_VALUE + Math.random() * (MAX_VALUE - MIN_VALUE));
            }
            testHashTable(keys, values);
        }
    }

    @Test
    public void benchmark() {
        HashTable t = new HashTable(new int[] {1, 2, 9, 17},
                                    new float[] {1.1f, 2.2f, 9.9f, 1.7f});
        byte[] data = t.bytes;
        final int N = 1000000;
        final int WARMUP_N = N * 10;
        float[] res = new float[N];
        int i;

        // warmup
        for (i = 0; i < WARMUP_N; i++) {
            t = new HashTable(data);
            res[i % N] = t.get(1);
            res[i % N] = HashTable.hget(data, 1);
        }

        long startTime, endTime;

        System.out.println("Benchmark instance vs static:");
        
        // test instance creation
        startTime = System.currentTimeMillis();
        for (i = 0; i < N; i++) {
            t = new HashTable(data);
            res[i] = t.get(17);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Instance: " + (endTime - startTime) + "ms");

        // test static method
        startTime = System.currentTimeMillis();
        for (i = 0; i < N; i++) {
            res[i] = HashTable.hget(data, 17);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Static: " + (endTime - startTime) + "ms");

        System.out.println("Res[0]: " + res[0]);
    }
}
