package nl.tijsbeek.dto;

public record CreateBoardRequest(
        String title,
        String description
) {}
