/**
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package antonha.dateparse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteDateParserTest {


    @TestFactory
    public Stream<DynamicTest> testParsesSameAsES() {
        return Stream.of(
                "2023-01-01T23:38:34.000Z",
                "2023-01-01T06:16:12.000Z",
                "2023-01-01T06:16:12.542Z",
                "2023-01-01T06:16:12Z",
                "2023-01-01Z",
                "2023-01-01"

        ).map(dateString -> DynamicTest.dynamicTest(dateString, () -> {
            Instant esParsed = toInstant(ElasticsearchParsers.doParse(dateString));
            Instant bytesParsed = toInstant(ByteDateParser.parse(dateString));
            assertEquals(esParsed, bytesParsed);
        }));
    }


    private final Random random = new Random();

    @TestFactory
    public Stream<DynamicTest> testRandomParsesSameAsES() {
        long min = Instant.parse("0000-01-01T00:00:00Z").toEpochMilli();
        long max = Instant.parse("9999-12-31T23:59:59.999Z").toEpochMilli();
        return Stream.generate(
                () -> Instant.ofEpochMilli(min + random.nextLong(max - min)).toString()
        ).limit(200).map(dateString -> DynamicTest.dynamicTest(dateString, () -> {
            Instant esParsed = toInstant(ElasticsearchParsers.doParse(dateString));
            Instant bytesParsed = toInstant(ByteDateParser.parse(dateString));
            assertEquals(esParsed, bytesParsed);
        }));
    }

    static Instant toInstant(TemporalAccessor tmp) {
        if(tmp.isSupported(ChronoField.HOUR_OF_DAY)) {
            return ZonedDateTime.from(tmp).toInstant();
        } else {
            return LocalDate.from(tmp).atStartOfDay(ZoneOffset.UTC).toInstant();
        }

    }

}