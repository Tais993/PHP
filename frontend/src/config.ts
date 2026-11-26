function stripTrailingSlashes(url: string): string {
    let end = url.length;
    while (end > 0 && url[end - 1] === '/') {
        end--;
    }
    return url.slice(0, end);
}

// Read frontend environment variables here so components share one configuration.
export const config = Object.freeze({
    apiBaseUrl: stripTrailingSlashes(import.meta.env.VITE_API_BASE_URL?.trim()
        || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api')),
});

export function apiUrl(path: string): string {
    return `${config.apiBaseUrl}/${path.replace(/^\/+/, '')}`;
}
