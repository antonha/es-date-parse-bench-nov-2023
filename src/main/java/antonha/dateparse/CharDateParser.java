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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

public class CharDateParser {

    public static TemporalAccessor parse(String dateString) {

        int year = charToInt(dateString.charAt(0)) * 1000 + charToInt(dateString.charAt(1)) * 100 + charToInt(dateString.charAt(2)) * 10 + charToInt(dateString.charAt(3));
        int month = charToInt(dateString.charAt(5)) * 10 + charToInt(dateString.charAt(6));
        int day = charToInt(dateString.charAt(8)) * 10 + charToInt(dateString.charAt(9));
        int hour = 0, minute = 0, second = 0, millis = 0;
        if (dateString.length() > 10 && dateString.charAt(10) == 'T') {
            // T
            hour = charToInt(dateString.charAt(11)) * 10 + charToInt(dateString.charAt(12));
            minute = charToInt(dateString.charAt(14)) * 10 + charToInt(dateString.charAt(15));
            second = charToInt(dateString.charAt(17)) * 10 + charToInt(dateString.charAt(18));
            if (dateString.charAt(19) == '.') {
                millis = charToInt(dateString.charAt(20)) * 100 + charToInt(dateString.charAt(21)) * 10 + charToInt(dateString.charAt(22));
            }
        }
        return ZonedDateTime.of(
                year,
                month,
                day,
                hour,
                minute,
                second,
                millis * 1_000_000,
                ZoneOffset.UTC
        );
    }

    private static int charToInt(char c) {
        return c - '0';
    }
}