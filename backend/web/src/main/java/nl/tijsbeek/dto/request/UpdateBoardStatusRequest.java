package nl.tijsbeek.dto.request;

import nl.tijsbeek.interfaces.services.FieldUpdate;
import org.openapitools.jackson.nullable.JsonNullable;

import static nl.tijsbeek.dto.FieldUpdateMapper.toFieldUpdate;

public record UpdateBoardStatusRequest(
        JsonNullable<String> title,
        JsonNullable<Integer> position
) {

    public FieldUpdate<String> titleToFieldUpdate() {
        return toFieldUpdate(title);
    }

    public FieldUpdate<Integer> positionToFieldUpdate() {
        return toFieldUpdate(position);
    }
}
