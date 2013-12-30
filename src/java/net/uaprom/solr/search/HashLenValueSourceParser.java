package net.uaprom.solr.search;

import net.uaprom.lucene.queries.function.HashLenFunction;

import org.apache.lucene.queries.function.ValueSource;

import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.ValueSourceParser;


public class HashLenValueSourceParser extends ValueSourceParser {
    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        String field = fp.parseArg();
        return new HashLenFunction(field);
    }
}
