package nl.tijsbeek.entities;

import nl.tijsbeek.entities.FilterRule.FilterField;
import nl.tijsbeek.entities.FilterRule.FilterOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterRuleTest {

    @ParameterizedTest
    @MethodSource("invalidOperatorsAndValueCounts")
    void constructor_InvalidOperatorOrValueCount_ThrowsIllegalArgumentException(
            FilterField field,
            FilterOperator operator,
            List<String> values
    ) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new FilterRule(field, operator, values));
        assertEquals("Invalid operator or value count for " + field + " " + operator, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidValueFormats")
    void constructor_InvalidValueFormat_ThrowsIllegalArgumentException(
            FilterField field,
            FilterOperator operator,
            String value,
            Class<? extends RuntimeException> expectedCause
    ) {
        List<String> values = List.of(value);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new FilterRule(field, operator, values));
        assertEquals("Invalid filter value for " + field, exception.getMessage());
        assertInstanceOf(expectedCause, exception.getCause());
    }

    @Test
    void constructor_DueDateNullCheckWithNullValues_DefaultsToEmptyList() {
        // Act
        FilterRule rule = new FilterRule(FilterField.DUE_DATE, FilterOperator.IS_NULL, null);

        // Assert
        assertEquals(List.of(), rule.values());
    }

    private static Stream<Arguments> invalidOperatorsAndValueCounts() {
        return Stream.of(
                Arguments.of(FilterField.STATUS, FilterOperator.EQUALS, List.of("1")),
                Arguments.of(FilterField.STATUS, FilterOperator.IN, List.of()),
                Arguments.of(FilterField.TITLE, FilterOperator.CONTAINS, List.of("first", "second")),
                Arguments.of(FilterField.DUE_DATE, FilterOperator.EQUALS, List.of("2026-11-19T12:00")),
                Arguments.of(FilterField.DUE_DATE, FilterOperator.LESS_THAN_OR_EQUAL, List.of()),
                Arguments.of(FilterField.DUE_DATE, FilterOperator.IS_NULL, List.of("2026-11-19T12:00"))
        );
    }

    private static Stream<Arguments> invalidValueFormats() {
        return Stream.of(
                Arguments.of(FilterField.STATUS, FilterOperator.IN, "not-a-number", NumberFormatException.class),
                Arguments.of(FilterField.DUE_DATE, FilterOperator.GREATER_THAN_OR_EQUAL, "not-a-date",
                        DateTimeParseException.class)
        );
    }
}
