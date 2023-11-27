/**
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package antonha.dateparse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

class CharDateParserTest {


  @TestFactory
  public Stream<DynamicTest> testParsesSameAsES() {
    return Stream.of(
        "2023-01-01T23:38:34.000Z",
        "2023-01-01T06:16:12.000Z",
        "2023-01-01T06:16:12.542Z",
        "2023-01-01T06:16:12Z",
        "2023-01-01Z",
        "2023-01-01",

        //Timezones
        "2023-01-01T23:38:34.000-0300",
        "2023-01-01T23:38:34.000+1200",

        //You could argue that some of these are not valid, but let's make sure that we parse the same as ES
        "",
        "fish",
        "3000 cats",
        "3000-ab",
        "3000-01-ab",
        "3000-01-01Taa",
        "3000-01-01T0a",
        //This could be interpreted as either an invalid time of "urkey" or as the valid TZ "Turkey"
        "3000-01-01Turkey",
        "3000-01-01T01:ab",
        "3000-01-01T01:01:ab",
        "3000-01-01T01:01:01.ab",
        "3000-01-01T01:01:01.12abCET",
        "3000-01-01T01:01:01Z is a date"

    ).map(dateString -> DynamicTest.dynamicTest(String.format("str: '%s'", dateString), () -> {
      Instant esParsed = toInstant(ElasticsearchParsers.doParse(dateString));
      Instant bytesParsed = toInstant(CharDateParser.parse(dateString));
      assertEquals(esParsed, bytesParsed);
    }));
  }

  private final Random random = new Random();
  private final Instant example = Instant.parse("2023-01-01T23:38:34.123456789Z");

  /*
  Tests that test that the CharDateParser parses the same as the Elasticsearch
  strict_date_optional_time parser.

  Set up using dynamic tests.
    - First, set up bunch of formats supported by the strict_date_optional_time parser
    - For each format, go through all available time zones
    - For each timezone, generate 10 random Instants
    - For each instant, format it using the format and timezone.
    - Ensure that the CharDateParser parses the same as the strict_date_optional_time parser.
   */
  @TestFactory
  @Execution(ExecutionMode.CONCURRENT)
  public Stream<DynamicContainer> testRandomParsesSameAsES() {
    long min = Instant.parse("0000-01-01T00:00:00Z").toEpochMilli();
    long max = Instant.parse("9999-12-31T23:59:59.999Z").toEpochMilli();
    return formats.stream().map(format -> {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
          return DynamicContainer.dynamicContainer(
              format,
              timeZones.stream().map(zone ->
                  DynamicContainer.dynamicContainer(
                      zone.toString(),
                      Stream.concat(
                          Stream.of(example),
                          Stream.generate(
                              () ->
                                  Instant.ofEpochMilli(min + random.nextLong(max - min))
                                      .plusNanos(random.nextLong(1_000_000_000))
                          )).limit(10).map(instant -> {
                        String dateString = instant.atZone(zone).format(formatter);
                        return DynamicTest.dynamicTest(dateString, () -> {
                          Instant esParsed = toInstant(ElasticsearchParsers.doParse(dateString));
                          Instant bytesParsed = toInstant(CharDateParser.parse(dateString));
                          assertEquals(esParsed, bytesParsed);
                        });
                      })
                  )));
        }
    );
  }

  static List<String> formats =
      Stream.concat(
          Stream.of(
              "uuuu",
              "uuuu-MM"
          ),
          Stream.of(
              "uuuu-MM-dd",
              "uuuu-MM-dd'T'hh",
              "uuuu-MM-dd'T'hh:mm",
              "uuuu-MM-dd'T'hh:mm:ss",
              "uuuu-MM-dd'T'hh:mm:ss'.'S",
              "uuuu-MM-dd'T'hh:mm:ss'.'SS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSSSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSSSSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSSSSSS",
              "uuuu-MM-dd'T'hh:mm:ss'.'SSSSSSSSS"
          ).flatMap(
              dateTimeFormat -> Stream.of("", "VV", "X", "ZZZ").map(zone -> dateTimeFormat + zone))
      ).toList();

  static List<ZoneId> timeZones = ZoneId.getAvailableZoneIds().stream().map(ZoneId::of)
      .toList();

  static Instant toInstant(TemporalAccessor tmp) {
    if (tmp == null) {
      return null;
    }
    var local = localDateTime(tmp);
    var zone = tmp.query(TemporalQueries.zone());
    if (zone != null) {
      return local.toInstant(zone.getRules().getOffset(local));
    }
    return local.toInstant(ZoneOffset.UTC);
  }

  static LocalDateTime localDateTime(TemporalAccessor tmp) {
    var date = localDate(tmp);
    if (!tmp.isSupported(ChronoField.HOUR_OF_DAY)) {
      return date.atStartOfDay();
    }
    int hour = tmp.get(ChronoField.HOUR_OF_DAY);
    if (!tmp.isSupported(ChronoField.MINUTE_OF_HOUR)) {
      return date.atTime(hour, 0);
    }
    int minute = tmp.get(ChronoField.MINUTE_OF_HOUR);
    if (!tmp.isSupported(ChronoField.SECOND_OF_MINUTE)) {
      return date.atTime(hour, minute);
    }
    int second = tmp.get(ChronoField.SECOND_OF_MINUTE);
    if (!tmp.isSupported(ChronoField.NANO_OF_SECOND)) {
      return date.atTime(hour, minute, second);
    }
    int nano = tmp.get(ChronoField.NANO_OF_SECOND);
    return date.atTime(hour, minute, second, nano);
  }

  static LocalDate localDate(TemporalAccessor tmp) {
    int year = tmp.get(ChronoField.YEAR);
    if (!tmp.isSupported(ChronoField.MONTH_OF_YEAR)) {
      return LocalDate.of(year, 1, 1);
    }
    int month = tmp.get(ChronoField.MONTH_OF_YEAR);
    if (!tmp.isSupported(ChronoField.DAY_OF_MONTH)) {
      return LocalDate.of(year, month, 1);
    }
    int day = tmp.get(ChronoField.DAY_OF_MONTH);
    return LocalDate.of(year, month, day);
  }

}