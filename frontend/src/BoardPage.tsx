import {useParams} from "react-router";
import {TasksProvider} from "./providers/TasksProvider.tsx";
import BoardLayout from "./components/layout/BoardLayout.tsx";

export default function BoardPage() {
    const {boardId} = useParams();

    if (!boardId || !/^[1-9]\d*$/.test(boardId)) {
        return <p>Invalid board ID.</p>;
    }

    return (
        <TasksProvider key={boardId} boardId={Number(boardId)}>
            <BoardLayout />
        </TasksProvider>
    );
}