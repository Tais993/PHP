package nl.tijsbeek.dto;

import nl.tijsbeek.interfaces.services.FieldUpdate;
import org.openapitools.jackson.nullable.JsonNullable;

public class FieldUpdateMapper {
    private FieldUpdateMapper() {}

    public static <T> FieldUpdate<T> toFieldUpdate(JsonNullable<T> field) {
        return new FieldUpdate<>(field.isPresent(), field.orElse(null));
    }
}
