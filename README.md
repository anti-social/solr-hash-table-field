HashTableField for Solr
===========================

Solr field to store hash table with integer keys and float values

Build instructions:
-------------------

```sh
  cd ~/workspace
  git clone https://github.com/apache/lucene-solr.git
  cd lucene-solr
  git checkout lucene_solr_4_6
  ant ivy-bootstrap
  cd solr
  ant dist
  
  cd ../../
  git clone git@github.com:anti-social/solr-hash-table-field.git
  cd solr-hash-table-field
  ant dist
```

If you has different path for Solr sources you should specify it:

```sh
  ant -Dsolr.proj.dir=/opt/workspace/solr-4.6.0 dist
  # or
  env SOLR_PROJ=/opt/workspace/solr-4.6.0 ant dist
```

Usage:
------

Copy `dist/solr-hash-table-field-0.4.jar` (version can differ) into `lib` directory of your solr collection.

Add in solrconfig.xml:

```xml
  <valueSourceParser name="hget" class="net.uaprom.solr.search.HashGetValueSourceParser"/>
  <valueSourceParser name="hlen" class="net.uaprom.solr.search.HashLenValueSourceParser"/>
  <valueSourceParser name="hexists" class="net.uaprom.solr.search.HashExistsValueSourceParser"/>
```

schema.xml:
```xml
  <field name="tag_ranks" type="htable" stored="true"/>

  <fieldType name="htable" class="net.uaprom.solr.schema.HashTableField"/>
```

Use in requests:

```
  fq={!frange l=2.2}hget(tag_ranks,1234)
  fq={!frange l=1}hlen(tag_ranks)
  sort=hlen(tag_ranks) desc
  boost=hget(tag_ranks,123456,1.0)
  group.func=hexists(tag_ranks,123456)
```
