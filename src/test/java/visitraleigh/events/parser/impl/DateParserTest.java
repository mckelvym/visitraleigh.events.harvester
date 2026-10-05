package visitraleigh.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DateParser.
 * Verifies date parsing with multiple format strategies and day-of-week prefix cleaning.
 */
class DateParserTest {

    private DateParser parser;

    @BeforeEach
    void setUp() {
        parser = new DateParser();
    }

    // Tests for parse() method with full month name formats

    @Test
    void parse_withFullMonthFormat_returnsLocalDate() {
        LocalDate result = parser.parse("December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("January 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withFullMonthNoComma_returnsLocalDate() {
        LocalDate result = parser.parse("December 15 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    // Tests for parse() method with abbreviated month formats

    @Test
    void parse_withAbbreviatedMonthFormat_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withAbbreviatedMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Jan 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withAbbreviatedMonthNoComma_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 15 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    // Tests for parse() method with day of week prefix

    @Test
    void parse_withMondayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Monday, December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withTuesdayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Tuesday, January 5, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withWednesdayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Wednesday, March 12, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 3, 12));
    }

    @Test
    void parse_withThursdayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Thursday, April 20, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 4, 20));
    }

    @Test
    void parse_withFridayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Friday, May 30, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 30));
    }

    @Test
    void parse_withSaturdayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Saturday, June 14, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 6, 14));
    }

    @Test
    void parse_withSundayPrefix_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Sunday, July 4, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 7, 4));
    }

    @Test
    void parse_withDayOfWeekPrefixNoComma_cleansPrefixAndParses() {
        LocalDate result = parser.parse("Monday December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullDayOfWeekFormat_returnsLocalDate() {
        // Format: "EEEE, MMMM d, yyyy"
        LocalDate result = parser.parse("Monday, December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullDayOfWeekDoubleDigitFormat_returnsLocalDate() {
        // Format: "EEEE, MMMM dd, yyyy"
        LocalDate result = parser.parse("Tuesday, January 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method with slash formats

    @Test
    void parse_withSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("12/15/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withSingleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("1/5/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withDoubleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("01/05/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method with ISO format

    @Test
    void parse_withIsoFormat_returnsLocalDate() {
        LocalDate result = parser.parse("2025-12-15");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withIsoFormatSingleDigits_returnsLocalDate() {
        LocalDate result = parser.parse("2025-01-05");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method with RFC 1123 format
    // Note: RFC_1123 format uses abbreviated day names (Mon, Tue, etc.)
    // which don't match the day-of-week cleaning regex that expects full names,
    // so the string passes through unchanged and RFC_1123 formatter handles it

    @Test
    void parse_withRfc1123Format_returnsLocalDate() {
        LocalDate result = parser.parse("Mon, 15 Dec 2025 10:00:00 GMT");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withRfc1123FormatDifferentDay_returnsLocalDate() {
        // RFC_1123 validates day-of-week, so we need to use a correct date
        // Sunday, January 5, 2025
        LocalDate result = parser.parse("Sun, 5 Jan 2025 14:30:00 GMT");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method with whitespace

    @Test
    void parse_withLeadingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withTrailingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withSurroundingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    // Tests for parse() method with null/blank/invalid inputs

    @Test
    void parse_withNull_returnsNull() {
        LocalDate result = parser.parse(null);

        assertThat(result).isNull();
    }

    @Test
    void parse_withEmptyString_returnsNull() {
        LocalDate result = parser.parse("");

        assertThat(result).isNull();
    }

    @Test
    void parse_withBlankString_returnsNull() {
        LocalDate result = parser.parse("   ");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidFormat_returnsNull() {
        LocalDate result = parser.parse("not a date");

        assertThat(result).isNull();
    }

    @Test
    void parse_withPartialDate_returnsNull() {
        LocalDate result = parser.parse("December 15");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidDay_returnsNull() {
        LocalDate result = parser.parse("December 32, 2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidMonth_returnsNull() {
        LocalDate result = parser.parse("13/15/2025");

        assertThat(result).isNull();
    }

    // Edge case tests

    @Test
    void parse_withLeapYearDate_returnsLocalDate() {
        LocalDate result = parser.parse("February 29, 2024");

        assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void parse_withNonLeapYearFebruary29_adjustsToFebruary28() {
        // DateTimeFormatter uses ResolverStyle.SMART by default which adjusts invalid dates
        LocalDate result = parser.parse("February 29, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    void parse_withFirstDayOfYear_returnsLocalDate() {
        LocalDate result = parser.parse("January 1, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void parse_withLastDayOfYear_returnsLocalDate() {
        LocalDate result = parser.parse("December 31, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 31));
    }
}
