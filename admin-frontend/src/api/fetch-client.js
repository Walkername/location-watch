
// Public paths where authorization is not required
const PUBLIC_PATHS = [
    "/auth/login",
    "/auth/register",
    "/auth/refresh",
    "/zones"
];

export default async function request(path, options = {}) {
    const url = process.env.REACT_APP_BACKEND_URL + path;
    const rawBody = options.body != null
        ? JSON.stringify(options.body)
        : undefined;

    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };

    const isPublic = PUBLIC_PATHS.includes(path);
    if (!isPublic) {
        const token = localStorage.getItem("accessToken");
        if (token) headers.Authorization = `Bearer ${token}`;
    }

    let response = await fetch(url, { ...options, headers, body: rawBody });

    // If 401 status code, then use refresh and the same rawBody
    if (response.status === 401) {
        const refreshRes = await fetch(
            process.env.REACT_APP_BACKEND_URL + "/auth/refresh",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken: localStorage.getItem("refreshToken") })
            }
        );
        if (!refreshRes.ok) throw new Error("Session expired");

        const { accessToken, refreshToken } = await refreshRes.json();
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);

        // Updating authorization header
        headers.Authorization = `Bearer ${accessToken}`;

        // Repeating initial request with the same rawBody
        response = await fetch(url, {
            ...options,
            headers,
            body: rawBody
        });
    }

    if (!response.ok) {
        const errBody = await response.json().catch(() => ({}));
        throw new Error(errBody.message || response.statusText);
    }

    return response.json();
}
