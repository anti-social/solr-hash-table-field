package net.uaprom.solr.schema;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.SortField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.BinaryDocValuesField;

import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.MapSolrParams;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import net.uaprom.util.HashTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class HashTableField extends FieldType {
    private static Logger log = LoggerFactory.getLogger(HashTableField.class);

    protected int simpleFormatThreshold;

    public static final String SIMPLE_FORMAT_THRESHOLD = "simpleFormatThreshold";
    
    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        SolrParams p = new MapSolrParams(args);
        simpleFormatThreshold = p.getInt(SIMPLE_FORMAT_THRESHOLD, HashTable.DEFAULT_SIMPLE_FORMAT_THRESHOLD);
        args.remove(SIMPLE_FORMAT_THRESHOLD);
        super.init(schema, args);
    }

    @Override
    public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
        writer.writeStr(name, f.stringValue(), false);
    }

    @Override
    public SortField getSortField(SchemaField field, boolean top) {
        throw new RuntimeException("Cannot sort on a HashMap field");
    }

    @Override
    public final boolean isPolyField() {
        // really true if the field is stored
        return true;
    }

    @Override
    public void checkSchemaField(final SchemaField field) {
        // supports doc values
    }

    @Override
    public final IndexableField createField(SchemaField field, Object value, float boost) {
        throw new IllegalStateException("instead call createFields() because isPolyField() is true");
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
        String externalValue = value.toString();
        List<IndexableField> indexFields = new ArrayList<IndexableField>();

        KeysValues kv;
        try {
            kv = parseValue(externalValue);
        } catch (RuntimeException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e.getMessage());
        }
        BytesRef ref = new BytesRef(HashTable.hcreate(kv.keys, kv.values, simpleFormatThreshold));
        indexFields.add(new BinaryDocValuesField(field.getName(), ref));

        if (field.stored()) {
            indexFields.add(new StoredField(field.getName(), externalValue));
        }

        return indexFields;
    }

    public KeysValues parseValue(String str) {
        String[] entries;
        List<Integer> keys = new ArrayList<Integer>();
        List<Float> values = new ArrayList<Float>();

        str = StringUtils.strip(str);
        if (StringUtils.startsWith(str, "{")) {
            entries = StringUtils.split(StringUtils.strip(str, "{}"), ',');
            for (String entry : entries) {
                String[] pair = StringUtils.stripAll(StringUtils.split(StringUtils.strip(entry), ':'));
                // JSON object properties should be strings
                // for example, python: json.dumps({1: 1.1}) -> '{"1": 1.1}'
                keys.add(Integer.parseInt(StringUtils.strip(pair[0], "\"")));
                values.add(Float.parseFloat(pair[1]));
            }
        } else if (StringUtils.startsWith(str, "[")) {
            entries = StringUtils.split(str, ',');
            int i = 0;
            for (String entry : entries) {
                // don't care about nesting
                entry = StringUtils.strip(entry, "[] ");
                if (i % 2 == 0) {
                    keys.add(Integer.parseInt(entry));
                }
                else {
                    values.add(Float.parseFloat(entry));
                }
                i++;
            }
            if (keys.size() != values.size()) {
                throw new RuntimeException("Keys and values length mismatch.");
            }
        } else {
            throw new RuntimeException("Cannot parse value");
        }

        return new KeysValues(ArrayUtils.toPrimitive(keys.toArray(new Integer[0])),
                              ArrayUtils.toPrimitive(values.toArray(new Float[0])));
    }

    public class KeysValues {
        public int[] keys;
        public float[] values;
 
        public KeysValues(int[] keys, float[] values) {
            this.keys = keys;
            this.values = values;
        }
    }
}
