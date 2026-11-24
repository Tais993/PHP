import {useParams} from "react-router";
import {TasksProvider} from "./providers/TasksProvider.tsx";
import BoardLayout from "./components/boards/BoardLayout.tsx";
import {BoardViewsProvider} from "./providers/BoardViewsProvider.tsx";

export default function BoardPage() {
    const {boardId, boardViewId, taskId} = useParams();

    if (!boardId || !/^[1-9]\d*$/.test(boardId)) {
        return <p>Invalid board ID.</p>;
    }

    if (boardViewId !== undefined && !/^[1-9]\d*$/.test(boardViewId)) {
        return <p>Invalid board view ID.</p>;
    }

    if (taskId !== undefined && (!/^[1-9]\d*$/.test(taskId) || !Number.isSafeInteger(Number(taskId)))) {
        return <p>Invalid task ID.</p>;
    }

    const boardPath = boardViewId === undefined ? `/boards/${boardId}` : `/boards/${boardId}/views/${boardViewId}`;

    return (
        <BoardViewsProvider key={boardId} boardId={Number(boardId)}
                            boardViewId={boardViewId === undefined ? undefined : Number(boardViewId)}>
            <TasksProvider key={boardId} boardId={Number(boardId)}>
                <BoardLayout boardId={Number(boardId)} boardPath={boardPath}
                             taskId={taskId === undefined ? undefined : Number(taskId)}/>
            </TasksProvider>
        </BoardViewsProvider>
    );
}
