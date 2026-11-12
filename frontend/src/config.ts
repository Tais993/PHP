// Read frontend environment variables here so components share one configuration.
export const config = Object.freeze({
    apiBaseUrl: (import.meta.env.VITE_API_BASE_URL?.trim()
        || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api')).replace(/\/+$/, ''),
});

export function apiUrl(path: string): string {
    return `${config.apiBaseUrl}/${path.replace(/^\/+/, '')}`;
}
