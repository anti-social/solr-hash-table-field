package net.uaprom.solr.search;

import net.uaprom.lucene.queries.function.HashGetFunction;

import org.apache.lucene.queries.function.ValueSource;

import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.ValueSourceParser;


public class HashGetValueSourceParser extends ValueSourceParser {
    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        String field = fp.parseArg();
        int key = fp.parseInt();
        float defaultValue = fp.hasMoreArguments() ? fp.parseFloat() : 0.0f;

        return new HashGetFunction(field, key, defaultValue);
    }
}
