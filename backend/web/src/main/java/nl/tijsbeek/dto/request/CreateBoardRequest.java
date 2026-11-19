package nl.tijsbeek.dto.request;

public record CreateBoardRequest(
        String title,
        String description
) {}
