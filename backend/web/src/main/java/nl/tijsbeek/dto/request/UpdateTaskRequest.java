package nl.tijsbeek.dto.request;

import nl.tijsbeek.interfaces.services.FieldUpdate;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        JsonNullable<String> title,
        JsonNullable<String> description,
        JsonNullable<String> dueDate,
        JsonNullable<Long> status
) {
    public FieldUpdate<String> titleToFieldUpdate() {
        return toFieldUpdate(title);
    }

    public FieldUpdate<String> descriptionToFieldUpdate() {
        return toFieldUpdate(description);
    }

    public FieldUpdate<LocalDateTime> dueDateToFieldUpdate() {
        String dueDateValue = dueDate.orElse(null);
        return new FieldUpdate<>(
                dueDate.isPresent(),
                dueDateValue == null ? null : LocalDateTime.parse(dueDateValue)
        );
    }

    public FieldUpdate<Long> statusToFieldUpdate() {
        return toFieldUpdate(status);
    }

    private static <T> FieldUpdate<T> toFieldUpdate(JsonNullable<T> field) {
        return new FieldUpdate<>(field.isPresent(), field.orElse(null));
    }
}
