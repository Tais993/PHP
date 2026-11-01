import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import Prototype from './Prototype.tsx'
import '@mantine/core/styles.css'
import {MantineProvider} from '@mantine/core'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <MantineProvider>
            <Prototype/>
        </MantineProvider>
    </StrictMode>,
)
