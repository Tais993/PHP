package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.BoardStatus;
import org.jspecify.annotations.NonNull;

public record StatusResponse(
        Long id,
        Long boardId,
        String name,
        int position) {

    public static @NonNull StatusResponse fromDomain(@NonNull BoardStatus status) {
        return new StatusResponse(status.getId(), status.getBoardId(), status.getName(), status.getPosition());
    }
}
