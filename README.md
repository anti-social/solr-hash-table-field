HashTableField for Solr
===========================

Solr field to store hash table with integer keys and float values


Add in solrconfig.xml:

```xml
  <valueSourceParser name="hget" class="net.uaprom.solr.search.HashGetValueSourceParser"/>
  <valueSourceParser name="hlen" class="net.uaprom.solr.search.HashLenValueSourceParser"/>
  <valueSourceParser name="hexists" class="net.uaprom.solr.search.HashExistsValueSourceParser"/>
```

schema.xml:
```xml
  <field name="tag_ranks" type="htable" docValues="true" stored="true"/>

  <fieldType name="htable" class="net.uaprom.solr.schema.HashTableField"/>
```

Use in requests:

```
  fq={!frange l=1}hlen(tag_ranks)
  sort=hlen(tag_ranks) desc
  boost=hget(tag_ranks,123456,1.0)
  group.func=hexists(tag_ranks,123456)
```
