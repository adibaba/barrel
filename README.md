# Run Elasticsearch with Docker

- See https://www.elastic.co/guide/en/elasticsearch/reference/7.5/docker.html
- `sudo docker pull docker.elastic.co/elasticsearch/elasticsearch:7.5.2`
-  `sudo docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.5.2`
- List indexes (with column headings): http://localhost:9200/_cat/indices?v=true
# Run Barrel

Configure Barrel via configuration.txt.

```
Configuration file:
 configuration.txt
Indexes:
  [barrel]
Usage:
 index  <indexId>                      Indexes unknown PDF files
 index  <indexId> all                  Indexes all PDF files
 list   <indexId>                      Lists PDF files
 search <indexId> <query>              Searches for query
 search <indexId> <query> <fileFilter> Searches for query in filenames containing filter
```

You can also run it in Java using MainManual.java.