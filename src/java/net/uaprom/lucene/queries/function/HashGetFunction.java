package net.uaprom.lucene.queries.function;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.uaprom.util.HashTable;


public class HashGetFunction extends ValueSource {
    private static Logger log = LoggerFactory.getLogger(HashGetFunction.class);

    protected final String fieldName;
    protected final int key;
    protected final float defaultValue;

    public HashGetFunction(String fieldName, int key, float defaultValue) {
        this.fieldName = fieldName;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String description() {
        return "hget(" + fieldName + "," + key + "," + defaultValue + ")";
    }

    @Override
    public int hashCode() {
        int h = fieldName.hashCode();
        h += key >>> 16;
        h += Float.floatToIntBits(defaultValue);
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != HashGetFunction.class) {
            return false;
        }
        HashGetFunction other = (HashGetFunction) o;
        return fieldName.equals(other.fieldName)
            && key == other.key
            && defaultValue == other.defaultValue;
    }

    @Override
    public FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
        final FieldInfo fieldInfo = readerContext.reader().getFieldInfos().fieldInfo(fieldName);
        final BinaryDocValues binaryValues = FieldCache.DEFAULT.getTerms(readerContext.reader(), fieldName, true);

        return new FloatDocValues(this) {
            @Override
            public float floatVal(int doc) {
                BytesRef target = new BytesRef();
                binaryValues.get(doc, target);
                if (target.length == 0) {
                    return defaultValue;
                }

                HashTable table = new HashTable(target.bytes, target.offset, target.length);
                return table.get(key, defaultValue);
            }
        };
    }
}
