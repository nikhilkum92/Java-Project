/**
 * CampusGrid — Authentication & Token Management Service
 * Production Dual-Token (Access + Refresh) Handler
 */

const Auth = {
    ACCESS_TOKEN_KEY: 'campusgrid_access_token',
    REFRESH_TOKEN_KEY: 'campusgrid_refresh_token',
    USER_KEY: 'campusgrid_user',

    getToken() {
        return localStorage.getItem(this.ACCESS_TOKEN_KEY);
    },

    getRefreshToken() {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    },

    getUser() {
        const u = localStorage.getItem(this.USER_KEY);
        return u ? JSON.parse(u) : null;
    },

    setSession(authData) {
        localStorage.setItem(this.ACCESS_TOKEN_KEY, authData.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, authData.refreshToken);
        localStorage.setItem(this.USER_KEY, JSON.stringify(authData.user));

        // Also set accessToken in cookie for server-side route guards if needed
        document.cookie = `accessToken=${authData.accessToken}; path=/; max-age=86400; SameSite=Lax`;
    },

    clearSession() {
        localStorage.removeItem(this.ACCESS_TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        document.cookie = 'accessToken=; path=/; max-age=0; SameSite=Lax';
    },

    async login(username, password, rememberMe = false) {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, rememberMe })
        });

        const data = await response.json();
        if (!response.ok || data.status !== 'SUCCESS') {
            throw new Error(data.message || 'Authentication failed.');
        }

        this.setSession(data.data);
        return data.data;
    },

    async refreshToken() {
        const rToken = this.getRefreshToken();
        if (!rToken) {
            this.clearSession();
            throw new Error('No refresh token available.');
        }

        const response = await fetch('/api/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: rToken })
        });

        const data = await response.json();
        if (!response.ok || data.status !== 'SUCCESS') {
            this.clearSession();
            throw new Error('Session expired. Please log in again.');
        }

        this.setSession(data.data);
        return data.data.accessToken;
    },

    async logout() {
        const rToken = this.getRefreshToken();
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: rToken })
            });
        } catch (e) {
            // Ignore network errors during logout
        } finally {
            this.clearSession();
            window.location.href = '/login';
        }
    },

    // Production Fetch Interceptor with Silent Refresh
    async fetchWithAuth(url, options = {}) {
        let token = this.getToken();
        if (!options.headers) {
            options.headers = {};
        }

        if (token) {
            options.headers['Authorization'] = `Bearer ${token}`;
        }

        let response = await fetch(url, options);

        // If Access Token expired (401), attempt silent refresh
        if (response.status === 401 && this.getRefreshToken()) {
            try {
                token = await this.refreshToken();
                options.headers['Authorization'] = `Bearer ${token}`;
                response = await fetch(url, options);
            } catch (err) {
                this.logout();
                throw err;
            }
        }

        return response;
    },

    // Role-based post-login redirection
    redirectByRole(role) {
        switch (role) {
            case 'ADMIN':
                window.location.href = '/admin/users';
                break;
            case 'FACULTY':
                window.location.href = '/faculty/dashboard';
                break;
            case 'STUDENT':
                window.location.href = '/student/dashboard';
                break;
            default:
                window.location.href = '/login';
        }
    }
};

// Global export
window.Auth = Auth;
