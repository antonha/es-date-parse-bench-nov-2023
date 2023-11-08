/**
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package antonha.dateparse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Date parser which is based on charAt() to demonstrate that it is possible to parse dates faster than the Java
 * parsers.

 * Might be fine to use in prod - there are quite a lot of tests, and they seem to work.
 */
public class CharDateParser {

    public static TemporalAccessor parse(String dateString) {

        int year = charToInt(dateString.charAt(0)) * 1000
            + charToInt(dateString.charAt(1)) * 100
            + charToInt(dateString.charAt(2)) * 10
            + charToInt(dateString.charAt(3));
        if(dateString.length() == 4 || dateString.charAt(4) != '-') {
            return LocalDate.of(year, 1, 1);
        }
        int month = charToInt(dateString.charAt(5)) * 10
            + charToInt(dateString.charAt(6));
        if(dateString.length() == 7 || dateString.charAt(7) != '-') {
            return LocalDate.of(year, month, 1);
        }
        int day = charToInt(dateString.charAt(8)) * 10
            + charToInt(dateString.charAt(9));
        if(dateString.length() == 10 || dateString.charAt(10) != 'T') {
            return LocalDate.of(year, month, day);
        }
        int hourPart = charToInt(dateString.charAt(11));
        if(hourPart < 0 || hourPart > 9) {
            return LocalDate.of(year, month, day);
        }
        int hour = hourPart * 10 + charToInt(dateString.charAt(12));
        if(dateString.length() == 13 || dateString.charAt(13) != ':') {
            return withZone(
                year, month, day, hour, 0, 0, 0,
                dateString, 13
            );
        }
        int minute = charToInt(dateString.charAt(14)) * 10
            + charToInt(dateString.charAt(15));
        if(dateString.length() == 16 || dateString.charAt(16) != ':') {
            return withZone(
                year, month, day, hour, minute, 0, 0,
                dateString, 16
            );
        }
        int second = charToInt(dateString.charAt(17)) * 10
            + charToInt(dateString.charAt(18));
        if(dateString.length() == 19 || dateString.charAt(19) != '.') {
            return withZone(
                year, month, day, hour, minute, second, 0,
                dateString, 19
            );
        }
        int nanos = 0;
        int pos = 20;
        while (pos < dateString.length()) {
            int num = charToInt(dateString.charAt(pos));
            if(num < 0 || num > 9) {
                break;
            }
            nanos = nanos * 10 + num;
            pos++;
        }
        int j = pos - 20;
        while(j < 9 ) {
            nanos *= 10;
            j++;
        }
        return withZone(
            year, month, day, hour, minute, second, nanos,
            dateString, pos
        );
    }

    private static TemporalAccessor withZone(
        int year, int month, int day,
        int hour, int minute, int second, int nanos,
        String dateString, int pos
    ) {
        if(pos == dateString.length()) {
            return LocalDateTime.of(year, month, day, hour, minute, second, nanos);
        }
        return ZonedDateTime.of(
            year, month, day, hour, minute, second, nanos,
            parseZone(dateString, pos)
        );
    }

    private static ZoneId parseZone(String dateString, int pos) {
        //Fast-track for UTC, since we don't need to create a substring.
        //Would love to have slices in Java.
        if(dateString.charAt(pos) == 'Z') {
            return ZoneOffset.UTC;
        }
        return ZoneId.of(dateString.substring(pos));
    }

    private static int charToInt(char c) {
        return c - '0';
    }
}
