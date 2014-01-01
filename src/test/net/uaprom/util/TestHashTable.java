package net.uaprom.util;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


@RunWith(JUnit4.class)
public class TestHashTable {
    static float DELTA = 0.000001f;

    private void testHashTable(int[] keys, float[] values) {
        testHashTable(keys, values, -1);
    }

    private void testHashTable(int[] keys, float[] values, int expectedSize) {
        HashTable t = new HashTable(keys, values);
        if (expectedSize != -1) {
            assertEquals(expectedSize, t.bytes.length);
        }
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], t.get(keys[i]), DELTA);
            assertTrue(t.exists(keys[i]));
        }
        assertEquals(keys.length, t.len());
        assertEquals(-1.23456789f, t.get(123456789, -1.23456789f), DELTA);
        assertFalse(t.exists(123456789));
    }
    
    @Test
    public void test() {
        testHashTable(new int[] {5}, new float[] {5.5f}, 8);
        testHashTable(new int[] {1, 3}, new float[] {1.1f, 3.3f}, 20);
        testHashTable(new int[] {1, 3, 2}, new float[] {1.1f, 3.3f, 2.2f}, 36);
        testHashTable(new int[] {1, 2, 3, 33}, new float[] {1.1f, 2.2f, 3.3f, 33.3f}, 68);
        testHashTable(new int[] {0, 16, 4, 20, 1}, new float[] {0.1f, 0.16f, 0.4f, 0.20f, 0.1f}, 68);
    }
}
