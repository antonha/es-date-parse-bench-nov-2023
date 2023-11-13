
# ES Date parsing benchmarks

This repository contains a set of benchmarks for parsing date strings, focusing on the performance of the default date 
parser used by Elasticsearch (`strict_date_optional_time`). The parser used comes from `java.time`, and turns out to be
not very fast. This repository contains a hand-rolled parser which is ~50x faster than `strict_date_optional_time`.

This parser is used during data ingestion, and can make ingestion significantly slower. How much slower depends on how 
of the data consists of date strings, and if you are CPU bound. For fake log data used by Elasticsearch benchmarks in 
[elastic/elasticsearch-opensearch-benchmark](https://github.com/elastic/elasticsearch-opensearch-benchmark), using a 
faster parser can make indexing 10% faster on a CPU-bound machine.

These benchmarks were done as a part of a blog post over at 
[blunders.io/posts/es-benchmark-1-date-parse](https://blunders.io/posts/es-benchmark-1-date-parse), 
which also delves deeper into details.

## The implementations

I tested 5 different implementations
- `strict_date_optional_time` - The default parser in Elasticsearch, which is very lenient in the granularity it accepts.
- `strict_date_time` - Another parser in Elasticsearch, which is more strict. It is about 2x as fast as 
  `strict_date_optional_time`.
- `Instant.parse` - This standard library function also parses strict date times, and is about as fast as `strict_date_time`.  I mostly included this as a sanity check for `strict_date_time`.
- `Regex` - A non-feature-complete regex test - I wanted to see if regexes would be faster than the parsers in `java.time`. It was - about 2x faster than `strict_date_time` and thus 4x faster than `strict_date_optional_time`.
- `ITUParser` - A third party library from [ethlo/itu](https://github.com/ethlo/itu) built for a bit more speed, was about 30x faster than `strict_date_time` and 60x faster than `strict_date_optional_time`. I do however not believe that it can't be made as lenient as `strict_date_optional_time`.
- `CharParser` - A hand-rolled parser which is about 50x faster than `strict_date_optional_time`, designed to be as lenient as `strict_date_optional_time`. This repository also contains tests to test that it parses date strings the same way as `strict_date_optional_time`. The tests do test about 230K generated cases - but I do not think that is 100% correct. It could become 100% with more eyes and tests on it though - PRs are welcome!

## Running the benchmarks

The benchmarks are based on JMH. You need to have maven installed.

There is a `run_bench.sh` script, which you can run using `sh run_bench.sh`.  It is rather simple:

```shell
mvn clean verify -DskipTests=true
java -jar target/benchmarks.jar
```

The tests are skipped since they do take a little bit of time to run. They should however pass - if they do not it's a 
bug.

## Benchmark results

These are the results from running the benchmarks on a desktop with an `AMD Ryzen 5 5600X`. Remember to only look at 
benchmarks results relative to each other - other machines will have different absolute numbers. If you can not 
reproduce the relative results, please let me know!

```
Benchmark                                 (dateString)  Mode  Cnt     Score    Error  Units
Benchmark.benchCharParser     2023-01-01T23:38:34.000Z  avgt    5    22.197 ±  0.229  ns/op
Benchmark.benchCharParser     1970-01-01T00:16:12.675Z  avgt    5    22.129 ±  0.170  ns/op
Benchmark.benchCharParser     5050-01-01T12:02:01.123Z  avgt    5    22.235 ±  0.288  ns/op
Benchmark.benchESParse        2023-01-01T23:38:34.000Z  avgt    5  1108.601 ± 16.202  ns/op
Benchmark.benchESParse        1970-01-01T00:16:12.675Z  avgt    5  1115.164 ± 40.150  ns/op
Benchmark.benchESParse        5050-01-01T12:02:01.123Z  avgt    5  1107.708 ± 14.095  ns/op
Benchmark.benchESStrictParse  2023-01-01T23:38:34.000Z  avgt    5   500.326 ±  3.116  ns/op
Benchmark.benchESStrictParse  1970-01-01T00:16:12.675Z  avgt    5   496.774 ±  4.574  ns/op
Benchmark.benchESStrictParse  5050-01-01T12:02:01.123Z  avgt    5   498.732 ±  4.134  ns/op
Benchmark.benchITUParser      2023-01-01T23:38:34.000Z  avgt    5    35.242 ±  1.965  ns/op
Benchmark.benchITUParser      1970-01-01T00:16:12.675Z  avgt    5    35.028 ±  0.434  ns/op
Benchmark.benchITUParser      5050-01-01T12:02:01.123Z  avgt    5    34.107 ±  0.367  ns/op
Benchmark.benchInstantParse   2023-01-01T23:38:34.000Z  avgt    5   470.640 ±  1.939  ns/op
Benchmark.benchInstantParse   1970-01-01T00:16:12.675Z  avgt    5   484.131 ±  4.044  ns/op
Benchmark.benchInstantParse   5050-01-01T12:02:01.123Z  avgt    5   482.272 ±  2.527  ns/op
Benchmark.benchRegex          2023-01-01T23:38:34.000Z  avgt    5   283.709 ±  4.295  ns/op
Benchmark.benchRegex          1970-01-01T00:16:12.675Z  avgt    5   271.189 ±  0.452  ns/op
Benchmark.benchRegex          5050-01-01T12:02:01.123Z  avgt    5   274.563 ±  7.238  ns/op
```
