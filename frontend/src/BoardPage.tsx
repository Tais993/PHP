import {useParams} from "react-router";
import {TasksProvider} from "./providers/TasksProvider.tsx";
import BoardLayout from "./components/boards/BoardLayout.tsx";
import {BoardViewsProvider} from "./providers/BoardViewsProvider.tsx";

export default function BoardPage() {
    const {boardId, boardViewId} = useParams();

    if (!boardId || !/^[1-9]\d*$/.test(boardId)) {
        return <p>Invalid board ID.</p>;
    }

    if (boardViewId !== undefined && !/^[1-9]\d*$/.test(boardViewId)) {
        return <p>Invalid board view ID.</p>;
    }

    return (
        <BoardViewsProvider key={boardId} boardId={Number(boardId)}
                            boardViewId={boardViewId === undefined ? undefined : Number(boardViewId)}>
            <TasksProvider key={boardId} boardId={Number(boardId)}>
                <BoardLayout/>
            </TasksProvider>
        </BoardViewsProvider>
    );
}