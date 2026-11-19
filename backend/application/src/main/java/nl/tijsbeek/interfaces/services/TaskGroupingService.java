package nl.tijsbeek.interfaces.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.results.TaskViewResult;

import java.util.List;

public interface TaskGroupingService {

    public TaskViewResult groupByStatus(List<Task> tasks, List<BoardStatus> statuses);
}
