package nl.tijsbeek.entities;

import java.util.List;

public class BoardView {

    private final Long id;
    private final Long boardId;
    private String name;
    private ViewType type;
    private GroupBy groupBy;
    private SortBy sortBy;
    private SortDirection sortDirection;
    private int position;
    private List<FilterRule> filterRules;

    public BoardView(Long boardId, String name, ViewType type,
                     GroupBy groupBy, SortBy sortBy, SortDirection sortDirection, int position) {
        this(null, boardId, name, type, groupBy, sortBy, sortDirection, position, List.of());
    }

    @SuppressWarnings("java:S107") /* Maximum parameter count of 7 for constructors, doesn't matter, it's a constructor */
    public BoardView(Long id, Long boardId, String name, ViewType type,
                     GroupBy groupBy, SortBy sortBy, SortDirection sortDirection, int position) {
        this(id, boardId, name, type, groupBy, sortBy, sortDirection, position, List.of());
    }

    @SuppressWarnings("java:S107") /* Maximum parameter count of 7 for constructors, doesn't matter, it's a constructor */
    public BoardView(Long id, Long boardId, String name, ViewType type,
                     GroupBy groupBy, SortBy sortBy, SortDirection sortDirection, int position,
                     List<FilterRule> filterRules) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.type = type;
        this.groupBy = groupBy;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.position = position;
        setFilterRules(filterRules);
    }

    public BoardView duplicate(String name) {
        return new BoardView(
                null,
                boardId,
                name,
                type,
                groupBy,
                sortBy,
                sortDirection,
                0,
                filterRules
        );
    }

    public Long getId() {
        return id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ViewType getType() {
        return type;
    }

    public void setType(ViewType type) {
        this.type = type;
    }

    public GroupBy getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<FilterRule> getFilterRules() {
        return filterRules;
    }

    public void setFilterRules(List<FilterRule> filterRules) {
        this.filterRules = filterRules == null ? List.of() : List.copyOf(filterRules);
    }

    public enum ViewType {
        BOARD, TABLE, ROADMAP
    }

    public enum GroupBy {
        STATUS
    }

    public enum SortBy {
        TITLE, CREATED_AT
    }

    public enum SortDirection {
        ASC, DESC
    }
}
