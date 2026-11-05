import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {AppShell, MantineProvider} from '@mantine/core'
import './index.css'
import '@mantine/core/styles.css'
import App from "./App.tsx";
import TopBar from "./components/TopBar.tsx";

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <MantineProvider>
            <AppShell
                // If we're on the boars page, the header should be twice the size. Is it maybe possible to just have a second header instead'
                header={{ height: 60 }}
                footer={{ height: 44 }}
                padding="lg"
            >
                <AppShell.Header>
                    <TopBar/>
                </AppShell.Header>

                <AppShell.Main>
                    <App/>
                </AppShell.Main>
            </AppShell>
            {/*<Container> <- should provide the topbar and footer*/}
            {/*    <Content <- the content itself, in this case the boards and views.*/}
            {/*</Container>*/}
        </MantineProvider>
    </StrictMode>,
)
