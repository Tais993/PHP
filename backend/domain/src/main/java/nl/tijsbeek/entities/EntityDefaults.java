package nl.tijsbeek.entities;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class EntityDefaults {

    public static @NonNull BoardView defaultBoardView(long boardId) {
        return new BoardView(boardId, "Default", BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, 0);
    }

    public static @NonNull List<BoardStatus> defaultKanbanBoardStatuses(long boardId) {
        return List.of(
                new BoardStatus(boardId, "Backlog", 1),
                new BoardStatus(boardId, "To Do", 2),
                new BoardStatus(boardId, "In Progress", 3),
                new BoardStatus(boardId, "Done", 4)
        );
    }
}
