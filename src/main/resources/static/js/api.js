function getJWT() {
    return localStorage.getItem('jwt_token');
}

async function apiFetch(url, options = {}) {
    const headers = options.headers || {};
    const token = getJWT();
    if (token) headers['Authorization'] = 'Bearer ' + token;
    options.headers = headers;
    const res = await fetch(url, options);
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

// Example usage:
// apiFetch('/api/rooms').then(rooms => { ... });

