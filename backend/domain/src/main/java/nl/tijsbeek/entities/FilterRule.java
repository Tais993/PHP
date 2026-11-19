package nl.tijsbeek.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public record FilterRule(
        FilterField field,
        FilterOperator operator,
        List<String> values
) {

    public FilterRule {
        Objects.requireNonNull(field, "Filter field is required");
        Objects.requireNonNull(operator, "Filter operator is required");
        values = values == null ? List.of() : List.copyOf(values);

        try {
            switch (field) {
                case STATUS -> validateStatusRule(operator, values);
                case TITLE -> validateTitleRule(operator, values);
                case DUE_DATE -> validateDueDateRule(operator, values);
            }
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid filter value for " + field, exception);
        }
    }

    private static void validateStatusRule(FilterOperator operator, List<String> values) {
        if ((operator != FilterOperator.IN && operator != FilterOperator.NOT_IN) || values.isEmpty()) {
            throw invalidOperatorOrValueCount(FilterField.STATUS, operator);
        }

        values.forEach(Long::parseLong);
    }

    private static void validateTitleRule(FilterOperator operator, List<String> values) {
        if ((operator != FilterOperator.CONTAINS && operator != FilterOperator.EQUALS) || values.size() != 1) {
            throw invalidOperatorOrValueCount(FilterField.TITLE, operator);
        }
    }

    private static void validateDueDateRule(FilterOperator operator, List<String> values) {
        boolean valid = switch (operator) {
            case LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL -> values.size() == 1;
            case IS_NULL, IS_NOT_NULL -> values.isEmpty();
            default -> false;
        };

        if (!valid) {
            throw invalidOperatorOrValueCount(FilterField.DUE_DATE, operator);
        }

        if (!values.isEmpty()) {
            LocalDateTime.parse(values.getFirst());
        }
    }

    private static IllegalArgumentException invalidOperatorOrValueCount(
            FilterField field,
            FilterOperator operator
    ) {
        return new IllegalArgumentException("Invalid operator or value count for " + field + " " + operator);
    }

    public enum FilterField {
        STATUS, TITLE, DUE_DATE
    }

    public enum FilterOperator {
        EQUALS, CONTAINS, IN, NOT_IN,
        LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, IS_NULL, IS_NOT_NULL
    }
}
