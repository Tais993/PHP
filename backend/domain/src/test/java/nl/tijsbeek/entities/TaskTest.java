package nl.tijsbeek.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {
    @ParameterizedTest
    @MethodSource("invalidTitles")
    void invalidTitleIsRejectedOnConstructionAndRename(String title) {
        // Construction checks
        assertThrows(IllegalArgumentException.class, () -> new Task(1L, title, "", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Task(9L, 1L, title, "", null, null, null));

        // Modify existing task
        String originalTitle = "x".repeat(Task.MAX_TITLE_LENGTH);
        Task task = new Task(1L, originalTitle, "", null, null);

        assertThrows(IllegalArgumentException.class, () -> task.changeTitle(title));
        assertEquals(originalTitle, task.getTitle());
    }

    @Test
    void boardAndDescriptionAreRequiredOnConstructionAndUpdate() {
        // Construction checks
        assertThrows(NullPointerException.class, () -> new Task(null, "Title", "", null, null));
        assertThrows(NullPointerException.class, () -> new Task(1L, "Title", null, null, null));

        // Modify existing task
        Task task = new Task(1L, "Title", "", null, null);
        assertThrows(NullPointerException.class, () -> task.setBoardId(null));
        assertThrows(NullPointerException.class, () -> task.setDescription(null));
        assertEquals(1L, task.getBoardId());
        assertEquals("", task.getDescription());
    }

    @Test
    void boardAndStatusMustMatchOnConstructionAndUpdate() {
        // Construction checks
        BoardStatus status = new BoardStatus(10L, 1L, "To do", 0);
        BoardStatus otherStatus = new BoardStatus(20L, 2L, "To do", 0);
        assertThrows(IllegalArgumentException.class,
                () -> new Task(1L, "Title", "", null, otherStatus));

        // Modify existing task
        Task task = new Task(1L, "Title", "", null, status);
        assertThrows(IllegalArgumentException.class, () -> task.setStatus(otherStatus));
        assertThrows(IllegalArgumentException.class, () -> task.setBoardId(2L));
        assertEquals(1L, task.getBoardId());
        assertSame(status, task.getStatus());

        task.setStatus(null);
        task.setBoardId(2L);
        assertEquals(2L, task.getBoardId());
        assertNull(task.getStatus());
    }

    private static Stream<String> invalidTitles() {
        return Stream.of(null, "", " \t", "x".repeat(Task.MAX_TITLE_LENGTH + 1));
    }
}
