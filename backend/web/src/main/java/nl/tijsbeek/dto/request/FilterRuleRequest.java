package nl.tijsbeek.dto.request;

import nl.tijsbeek.entities.FilterRule;
import nl.tijsbeek.entities.FilterRule.FilterField;
import nl.tijsbeek.entities.FilterRule.FilterOperator;
import nl.tijsbeek.exceptions.InvalidFilterRulesException;

import java.util.List;
import java.util.Objects;

public record FilterRuleRequest(
        FilterField field,
        FilterOperator operator,
        List<String> values
) {
    public FilterRuleRequest {
        Objects.requireNonNull(field, "Filter field is required");
        Objects.requireNonNull(operator, "Filter operator is required");
        values = values == null ? List.of() : List.copyOf(values);
    }

    public FilterRule toDomain() {
        try {
            return new FilterRule(field, operator, values);
        } catch (IllegalArgumentException exception) {
            throw new InvalidFilterRulesException(exception.getMessage());
        }
    }
}
