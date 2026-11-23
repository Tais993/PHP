package nl.tijsbeek.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardTest {
    @ParameterizedTest
    @MethodSource("invalidTitles")
    void invalidTitleIsRejectedOnConstructionAndRename(String title) {
        // Construction checks
        assertThrows(IllegalArgumentException.class, () -> new Board(null, title, ""));

        // Modify existing board
        String originalTitle = "x".repeat(Board.MAX_TITLE_LENGTH);
        Board board = new Board(null, originalTitle, "");
        assertThrows(IllegalArgumentException.class, () -> board.changeTitle(title));
        assertEquals(originalTitle, board.getTitle());
    }

    @Test
    void descriptionIsRequiredOnConstructionAndUpdate() {
        // Construction checks
        assertThrows(NullPointerException.class, () -> new Board(null, "Inbox", null));

        // Modify existing board
        Board board = new Board(null, "Inbox", "");
        assertThrows(NullPointerException.class, () -> board.setDescription(null));
        assertEquals("", board.getDescription());
    }

    @Test
    void statusesCanBeNewOrAlreadyLinkedToTheBoard() {
        BoardStatus newStatus = new BoardStatus(null, null, "To do", 0);
        BoardStatus linkedStatus = new BoardStatus(10L, 1L, "Done", 1);

        assertEquals(List.of(newStatus), new Board(null, "Inbox", "", List.of(newStatus)).getStatuses());
        assertEquals(List.of(linkedStatus, newStatus),
                new Board(1L, "Inbox", "", List.of(linkedStatus, newStatus)).getStatuses());
        assertThrows(IllegalArgumentException.class,
                () -> new Board(2L, "Inbox", "", List.of(linkedStatus)));
        assertThrows(IllegalArgumentException.class,
                () -> new Board(null, "Inbox", "", List.of(linkedStatus)));
    }

    private static Stream<String> invalidTitles() {
        return Stream.of(null, "", " \t", "x".repeat(Board.MAX_TITLE_LENGTH + 1));
    }
}
