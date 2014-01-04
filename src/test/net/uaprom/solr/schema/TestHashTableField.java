package net.uaprom.solr.schema;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertArrayEquals;


@RunWith(JUnit4.class)
public class TestHashTableField {
    static float DELTA = 0.000001f;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testParseValue() {
        HashTableField f = new HashTableField();
        HashTableField.KeysValues kv;

        kv = f.parseValue("[1, 1.1, 2, 2.2]");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);

        kv = f.parseValue("[[1, 1.1], [2, 2.2]]");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);
        
        kv = f.parseValue(" [ [ 1, 1.1 ], [ 2, 2.2 ] ] ");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);
        
        kv = f.parseValue("{1: 1.1, 2: 2.2}");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);

        kv = f.parseValue("{\"1\": 1.1, \"2\": 2.2}");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);

        kv = f.parseValue(" { 1 : 1.1, 2 : 2.2 } ");
        assertArrayEquals(new int[] {1, 2}, kv.keys);
        assertArrayEquals(new float[] {1.1f, 2.2f}, kv.values, DELTA);
    }

    @Test
    public void failedParseIllegalValue() {
        HashTableField f = new HashTableField();
        exception.expect(RuntimeException.class);
        HashTableField.KeysValues kv = f.parseValue("test");
    }

    @Test
    public void failedParseNotNumberValue() {
        HashTableField f = new HashTableField();
        exception.expect(RuntimeException.class);
        HashTableField.KeysValues kv = f.parseValue("{\"test\": 1.1}");
    }
}
