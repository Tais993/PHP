import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Outlet, Route, Routes } from 'react-router'
import { AppShell, MantineProvider } from '@mantine/core'

import '@mantine/core/styles.css'
import './index.css'

import BoardLayout from './components/layout/BoardLayout.tsx'
import TopBar from './components/layout/TopBar.tsx'
import Prototype from './prototype/Prototype.tsx'
import { BoardsProvider } from "./providers/BoardsProvider.tsx"

function AppLayout() {
    return (
        <AppShell
            header={{ height: 60 }}
            footer={{ height: 44 }}
            padding="lg"
        >
            <AppShell.Header>
                <TopBar />
            </AppShell.Header>

            <AppShell.Main>
                <Outlet />
            </AppShell.Main>
        </AppShell>
    )
}

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <MantineProvider>
            <BrowserRouter>
                <Routes>
                    {/* Any normal page shares the same layout */}
                    <Route element={
                                <BoardsProvider>
                                   <AppLayout />
                               </BoardsProvider>
                           }>
                        <Route index element={<BoardLayout />} />
                        <Route path="boards/:boardId" element={<BoardLayout />} />
                    </Route>

                    {/* Prototype needs its own layout */}
                    <Route path="/prototype" element={<Prototype />} />

                    <Route
                        path="*"
                        element={<p>Page not found.</p>}
                    />
                </Routes>
            </BrowserRouter>
        </MantineProvider>
    </StrictMode>,
)