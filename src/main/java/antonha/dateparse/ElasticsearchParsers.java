package antonha.dateparse;

import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

/**
 * Relevant code in here copied from
 * https://github.com/elastic/elasticsearch/blob/main/server/src/main/java/org/elasticsearch/common/time/DateFormatters.java
 * for demonstration purposes.
 *
 * This code is copyrighted and licensed under elasticsearch:
 *
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 *
 */
public class ElasticsearchParsers {


    private static final DateTimeFormatter TIME_ZONE_FORMATTER_NO_COLON = new DateTimeFormatterBuilder().appendOffset("+HHmm", "Z")
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_YEAR_MONTH_DAY_FORMATTER = new DateTimeFormatterBuilder().appendValue(
                    ChronoField.YEAR,
                    4,
                    4,
                    SignStyle.EXCEEDS_PAD
            )
            .optionalStart()
            .appendLiteral("-")
            .appendValue(MONTH_OF_YEAR, 2, 2, SignStyle.NOT_NEGATIVE)
            .optionalStart()
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NOT_NEGATIVE)
            .optionalEnd()
            .optionalEnd()
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_DATE_OPTIONAL_TIME_FORMATTER = new DateTimeFormatterBuilder().append(
                    STRICT_YEAR_MONTH_DAY_FORMATTER
            )
            .optionalStart()
            .appendLiteral('T')
            .optionalStart()
            .appendValue(HOUR_OF_DAY, 2, 2, SignStyle.NOT_NEGATIVE)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NOT_NEGATIVE)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NOT_NEGATIVE)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .optionalStart()
            .appendLiteral(',')
            .appendFraction(NANO_OF_SECOND, 1, 9, false)
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .optionalStart()
            .appendZoneOrOffsetId()
            .optionalEnd()
            .optionalStart()
            .append(TIME_ZONE_FORMATTER_NO_COLON)
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    static TemporalAccessor doParse(String string) {
        return (TemporalAccessor) STRICT_DATE_OPTIONAL_TIME_FORMATTER.toFormat().parseObject(string, new ParsePosition(0));
    }

    private static final DateTimeFormatter STRICT_HOUR_MINUTE_SECOND_FORMATTER = new DateTimeFormatterBuilder().appendValue(
            HOUR_OF_DAY,
            2,
            2,
            SignStyle.NOT_NEGATIVE
        )
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NOT_NEGATIVE)
        .toFormatter(Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_DATE_FORMATTER = new DateTimeFormatterBuilder().append(STRICT_YEAR_MONTH_DAY_FORMATTER)
        .appendLiteral('T')
        .append(STRICT_HOUR_MINUTE_SECOND_FORMATTER)
        .optionalStart()
        .appendFraction(NANO_OF_SECOND, 1, 9, true)
        .optionalEnd()
        .toFormatter(Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT);

    static TemporalAccessor doParseStrict(String string) {
        return (TemporalAccessor) STRICT_DATE_FORMATTER.toFormat().parseObject(string, new ParsePosition(0));
    }

    /**
     * Example for being able to run this code in a profiler.
     */
    static TemporalAccessor parsed = null;
    public static void main(String[] args) {
        while (true) {
            parsed = doParse("2023-01-01T23:38:34.000Z");
        }
    }
}
