package nl.tijsbeek.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardStatusTest {
    @ParameterizedTest
    @MethodSource("invalidNames")
    void rejectsInvalidNamesInBothConstructors(String name) {
        assertThrows(IllegalArgumentException.class, () -> new BoardStatus(1L, name, 0));
        assertThrows(IllegalArgumentException.class, () -> new BoardStatus(2L, 1L, name, 0));
    }

    @Test
    void rejectsNegativePositionInBothConstructors() {
        assertThrows(IllegalArgumentException.class, () -> new BoardStatus(1L, "To do", -1));
        assertThrows(IllegalArgumentException.class, () -> new BoardStatus(2L, 1L, "To do", -1));
    }

    @Test
    void acceptsMaximumNameLengthAndZeroPosition() {
        String name = "x".repeat(BoardStatus.MAX_NAME_LENGTH);
        BoardStatus status = new BoardStatus(1L, name, 0);

        assertEquals(name, status.getName());
        assertEquals(0, status.getPosition());
    }

    private static Stream<String> invalidNames() {
        return Stream.of(null, "", " \t", "x".repeat(BoardStatus.MAX_NAME_LENGTH + 1));
    }
}
