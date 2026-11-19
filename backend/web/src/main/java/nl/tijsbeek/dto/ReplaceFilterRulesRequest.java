package nl.tijsbeek.dto;

import nl.tijsbeek.entities.FilterRule;

import java.util.List;
import java.util.Objects;

public record ReplaceFilterRulesRequest(List<FilterRuleRequest> filterRules) {
    public ReplaceFilterRulesRequest {
        if (filterRules == null || filterRules.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("filterRules must be an array of filter rules");
        }
    }

    public List<FilterRule> toDomain() {
        return filterRules.stream().map(FilterRuleRequest::toDomain).toList();
    }
}
