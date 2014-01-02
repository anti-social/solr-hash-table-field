package net.uaprom.lucene.queries.function;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.docvalues.BoolDocValues;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.uaprom.util.HashTable;


public class HashExistsFunction extends ValueSource {
    private static Logger log = LoggerFactory.getLogger(HashExistsFunction.class);

    protected final String fieldName;
    protected final int key;

    public HashExistsFunction(String fieldName, int key) {
        this.fieldName = fieldName;
        this.key = key;
    }

    @Override
    public String description() {
        return "hexists(" + fieldName + ")";
    }

    public static final int hash = HashExistsFunction.class.hashCode();
    @Override
    public int hashCode() {
        return hash + fieldName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != HashExistsFunction.class) {
            return false;
        }
        HashExistsFunction other = (HashExistsFunction) o;
        return fieldName.equals(other.fieldName);
    }

    @Override
    public FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
        final FieldInfo fieldInfo = readerContext.reader().getFieldInfos().fieldInfo(fieldName);
        final BinaryDocValues binaryValues = FieldCache.DEFAULT.getTerms(readerContext.reader(), fieldName, true);
        final BytesRef target = new BytesRef();

        return new BoolDocValues(this) {
            @Override
            public boolean boolVal(int doc) {
                binaryValues.get(doc, target);
                return HashTable.hexists(target.bytes, target.offset, target.length, key);
            }
        };
    }
}
