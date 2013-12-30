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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import net.uaprom.util.HashTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class HashTableFieldType extends FieldType {
    private static Logger log = LoggerFactory.getLogger(HashTableFieldType.class);

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
        // really true if the field is indexed and stored
        return true;
    }

    @Override
    public void checkSchemaField(final SchemaField field) {
    }

    @Override
    public final IndexableField createField(SchemaField field, Object value, float boost) {
        throw new IllegalStateException("instead call createFields() because isPolyField() is true");
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
        String externalValue = value.toString();
        List<IndexableField> indexFields = new ArrayList<IndexableField>();

        if (field.hasDocValues()) {
            KeysValues kv = parseMap(externalValue);
            HashTable table = new HashTable(kv.keys, kv.values);
            indexFields.add(new BinaryDocValuesField(field.getName(), new BytesRef(table.bytes)));
        }

        if (field.stored()) {
            indexFields.add(new StoredField(field.getName(), externalValue));
        }

        return indexFields;
    }

    private KeysValues parseMap(String str) {
        String strippedStr = StringUtils.strip(str, "{}");
        String[] entries = StringUtils.split(strippedStr, ',');

        List<Integer> keys = new ArrayList<Integer>();
        List<Float> values = new ArrayList<Float>();
        for (String entry : entries) {
            String[] pair = StringUtils.stripAll(StringUtils.split(StringUtils.strip(entry), ':'));
            keys.add(Integer.parseInt(StringUtils.strip(pair[0], "\"")));
            values.add(Float.parseFloat(StringUtils.strip(pair[1], "\"")));
        }

        return new KeysValues(ArrayUtils.toPrimitive(keys.toArray(new Integer[0])),
                              ArrayUtils.toPrimitive(values.toArray(new Float[0])));
    }

    class KeysValues {
        public int[] keys;
        public float[] values;
 
        public KeysValues(int[] keys, float[] values) {
            this.keys = keys;
            this.values = values;
        }
    }
}
