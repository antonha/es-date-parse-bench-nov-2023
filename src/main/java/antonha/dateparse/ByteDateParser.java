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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Date parser which is based on byte arrays to demonstrate that it is possible to parse dates faster than the Java
 * parsers.

 * -- THIS CODE IS NOT ONLY A DEMONSTRATION AND SHOULD NOT BE USED IN PRODUCTION --
 */
public class ByteDateParser {

    public static TemporalAccessor parse(String dateString) {
        byte[] bytes = dateString.getBytes(StandardCharsets.UTF_8);
        int year = byteToInt(bytes[0]) * 1000 + byteToInt(bytes[1]) * 100 + byteToInt(bytes[2]) * 10 + byteToInt(bytes[3]);
        int month = byteToInt(bytes[5]) * 10 + byteToInt(bytes[6]);
        int day = byteToInt(bytes[8]) * 10 + byteToInt(bytes[9]);
        // T
        if (bytes.length > 10 && bytes[10] == 0x54) {
            int hour = byteToInt(bytes[11]) * 10 + byteToInt(bytes[12]);
            int minute = byteToInt(bytes[14]) * 10 + byteToInt(bytes[15]);
            int second = byteToInt(bytes[17]) * 10 + byteToInt(bytes[18]);
            int millis = 0;
            if (bytes[19] == 0x2E) {
                millis = byteToInt(bytes[20]) * 100 + byteToInt(bytes[21]) * 10 + byteToInt(bytes[22]);
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
        } else {
            return LocalDate.of(year, month, day);
        }

    }

    private static int byteToInt(byte b) {
        return b - 0x30;
    }
}
