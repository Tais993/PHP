package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.interfaces.services.TaskGroupingService;
import nl.tijsbeek.results.TaskViewResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskGroupingServiceImpl implements TaskGroupingService {

    @Override
    public TaskViewResult groupByStatus(List<Task> tasks, List<BoardStatus> statuses) {
        LinkedHashMap<Long, TaskViewResult.Group> groupedTasks = statuses.stream()
                .sorted(Comparator.comparingInt(BoardStatus::getPosition))
                .collect(Collectors.toMap(
                        BoardStatus::getId,
                        TaskViewResult.Group::fromStatus,
                        (_, _) -> {
                            throw new IllegalStateException("Two statuses should not be capable of having the same ID");
                        },
                        LinkedHashMap::new
                ));

        tasks.forEach(task -> {
            groupedTasks.computeIfPresent(task.getStatus().getId(), (id, group) -> {
                group.tasks().add(task);
                return group;
            });
        });

        return new TaskViewResult(BoardView.GroupBy.STATUS, groupedTasks);
    }
}