package net.uaprom.lucene.queries.function;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.uaprom.util.HashTable;


public class HashLenFunction extends ValueSource {
    private static Logger log = LoggerFactory.getLogger(HashLenFunction.class);

    protected final String fieldName;

    public HashLenFunction(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String description() {
        return "hlen(" + fieldName + ")";
    }

    public static final int hash = HashLenFunction.class.hashCode();
    @Override
    public int hashCode() {
        return hash + fieldName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != HashLenFunction.class) {
            return false;
        }
        HashLenFunction other = (HashLenFunction) o;
        return fieldName.equals(other.fieldName);
    }

    @Override
    public FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
        final FieldInfo fieldInfo = readerContext.reader().getFieldInfos().fieldInfo(fieldName);
        final BinaryDocValues binaryValues = FieldCache.DEFAULT.getTerms(readerContext.reader(), fieldName, true);

        return new IntDocValues(this) {
            @Override
            public int intVal(int doc) {
                BytesRef target = new BytesRef();
                binaryValues.get(doc, target);
                if (target.length == 0) {
                    return 0;
                }

                HashTable table = new HashTable(target.bytes, target.offset, target.length);
                return table.len();
            }
        };
    }
}
