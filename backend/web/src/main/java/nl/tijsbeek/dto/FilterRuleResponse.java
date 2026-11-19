package nl.tijsbeek.dto;

import nl.tijsbeek.entities.FilterRule;
import nl.tijsbeek.entities.FilterRule.FilterField;
import nl.tijsbeek.entities.FilterRule.FilterOperator;
import org.jetbrains.annotations.Contract;

import java.util.List;

public record FilterRuleResponse(
        FilterField field,
        FilterOperator operator,
        List<String> values
) {
    @Contract("_ -> new")
    public static FilterRuleResponse fromDomain(FilterRule rule) {
        return new FilterRuleResponse(rule.field(), rule.operator(), rule.values());
    }
}
