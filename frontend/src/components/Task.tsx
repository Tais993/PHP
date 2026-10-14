import type { TaskData } from '../App'

type TaskProps = {
    task: TaskData
}

export function Task({ task }: TaskProps) {
    return (
        <div>
            <h2>{task.title}</h2>
            <p>{task.description}</p>
            <span>Status: {task.status}</span>
        </div>
    )
}