package nl.tijsbeek.entities;

import jakarta.persistence.*;
import nl.tijsbeek.entities.BoardView.GroupBy;
import nl.tijsbeek.entities.BoardView.SortBy;
import nl.tijsbeek.entities.BoardView.SortDirection;
import nl.tijsbeek.entities.BoardView.ViewType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "board_view")
public class BoardViewJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardJPA board;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ViewType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_by", length = 20)
    private GroupBy groupBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_by", nullable = false, length = 20)
    private SortBy sortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_direction", nullable = false, length = 4)
    private SortDirection sortDirection;

    @Column(name = "position", nullable = false)
    private int position;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter_rules", nullable = false, columnDefinition = "jsonb")
    private List<FilterRule> filterRules = List.of();

    public BoardViewJPA() {
        // Required by JPA
    }

    public BoardViewJPA(Long id, BoardJPA board, String name, ViewType type,
                        GroupBy groupBy, SortBy sortBy, SortDirection sortDirection, int position,
                        List<FilterRule> filterRules) {
        this.id = id;
        this.board = board;
        this.name = name;
        this.type = type;
        this.groupBy = groupBy;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.position = position;
        this.filterRules = filterRules;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BoardJPA getBoard() {
        return board;
    }

    public void setBoard(BoardJPA board) {
        this.board = board;
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

}
