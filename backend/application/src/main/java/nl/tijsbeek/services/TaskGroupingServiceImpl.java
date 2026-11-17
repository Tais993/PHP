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

    // Synthetic response key; the group's status ID remains null.
    private static final long UNASSIGNED_GROUP_KEY = 0L;

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

        ArrayList<Task> unassignedTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getStatus() == null) {
                unassignedTasks.add(task);
                continue;
            }

            groupedTasks.computeIfPresent(task.getStatus().getId(), (id, group) -> {
                group.tasks().add(task);
                return group;
            });
        }

        if (!unassignedTasks.isEmpty()) {
            groupedTasks.put(UNASSIGNED_GROUP_KEY, new TaskViewResult.Group(
                    null, "Unassigned", Integer.MAX_VALUE, unassignedTasks));
        }

        return new TaskViewResult(BoardView.GroupBy.STATUS, groupedTasks);
    }
}