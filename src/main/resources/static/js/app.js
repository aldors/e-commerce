export const API_BASE_URL = window.__API_BASE_URL__ || "http://localhost:8080";

let refreshPromise = null;

export class ApiError extends Error {
    constructor(context, errors = [], status = 0) {
        super(context || "No se pudo completar la operación.");
        this.name = "ApiError";
        this.context = this.message;
        this.errors = Array.isArray(errors) ? errors.filter(Boolean) : [];
        this.status = status;
    }
}

export const getAccessToken = () => localStorage.getItem("accessToken");
export const getRefreshToken = () => localStorage.getItem("refreshToken");
export const hasSession = () => Boolean(getAccessToken() || getRefreshToken());

export function saveSession({ accessToken, refreshToken } = {}) {
    if (accessToken) localStorage.setItem("accessToken", accessToken);
    if (refreshToken) localStorage.setItem("refreshToken", refreshToken);
}

export function clearSession() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
}

export function getSession() {
    const token = getAccessToken();
    if (!token) return null;
    try {
        const part = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        const json = new TextDecoder().decode(Uint8Array.from(atob(part.padEnd(part.length + (4 - part.length % 4) % 4, "=")), c => c.charCodeAt(0)));
        const payload = JSON.parse(json);
        const roles = (payload.roles || []).map(role => role.authority || role);
        const isAdmin = roles.includes("ROLE_ADMIN");
        return { nombre: payload.nombre || payload.sub || "Usuario", email: payload.sub || "", roles, isAdmin, rol: isAdmin ? "ADMIN" : "USER" };
    } catch { return null; }
}

async function readBody(response) {
    const text = await response.text();
    if (!text) return null;
    try { return JSON.parse(text); } catch { return null; }
}

export async function refreshAccessToken() {
    if (refreshPromise) return refreshPromise;
    const refreshToken = getRefreshToken();
    if (!refreshToken) throw new ApiError("Tu sesión venció", ["Inicia sesión de nuevo para continuar."], 401);
    refreshPromise = (async () => {
        let response;
        try {
            response = await fetch(`${API_BASE_URL}/auth/refresh-token`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken }) });
        } catch { throw new ApiError("No se pudo conectar", ["Verifica que el servidor esté disponible."], 0); }
        const body = await readBody(response);
        if (!response.ok || !body?.success || !body?.data?.accessToken) {
            throw new ApiError(body?.message || "Tu sesión venció", body?.errors || ["Inicia sesión de nuevo para continuar."], response.status);
        }
        saveSession(body.data);
        return body.data.accessToken;
    })().finally(() => { refreshPromise = null; });
    return refreshPromise;
}

export async function apiJson(path, options = {}) {
    const { auth = true, method = "GET", body, headers = {}, retry = true } = options;
    const request = async token => {
        const requestHeaders = { ...headers };
        if (body !== undefined && !(body instanceof FormData)) requestHeaders["Content-Type"] = "application/json";
        if (auth && token) requestHeaders.Authorization = `Bearer ${token}`;
        let response;
        try {
            response = await fetch(`${API_BASE_URL}${path}`, { method, headers: requestHeaders, body: body === undefined || body instanceof FormData ? body : JSON.stringify(body) });
        } catch { throw new ApiError("No se pudo conectar", ["Verifica tu conexión e inténtalo de nuevo."], 0); }
        const responseBody = await readBody(response);
        if (!response.ok) throw new ApiError(responseBody?.message || `No se pudo completar la operación (${response.status}).`, responseBody?.errors || [], response.status);
        if (responseBody?.success === false) throw new ApiError(responseBody.message || "No se pudo completar la operación.", responseBody.errors || [], response.status);
        return responseBody?.data ?? null;
    };

    try { return await request(getAccessToken()); }
    catch (error) {
        if (error?.status !== 401 || !auth || !retry) throw error;
        try { return await request(await refreshAccessToken()); }
        catch (refreshError) {
            if (refreshError?.status === 401) {
                clearSession();
                document.dispatchEvent(new CustomEvent("session:expired"));
            }
            throw refreshError;
        }
    }
}

export async function logout() {
    const refreshToken = getRefreshToken();
    try {
        if (refreshToken) await fetch(`${API_BASE_URL}/auth/logout`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken }) });
    } finally { clearSession(); }
}

export const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]));
export const formatMoney = value => new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(Number.isFinite(Number(value)) ? Number(value) : 0);
export const formatDate = value => { const date = new Date(value); return Number.isNaN(date.getTime()) ? "Sin fecha" : date.toLocaleString("es-MX", { dateStyle: "medium", timeStyle: "short" }); };
export const initials = value => String(value || "?").trim().split(/\s+/).filter(Boolean).slice(0, 2).map(word => word[0]).join("").toUpperCase() || "?";

export function toast(message, type = "info") {
    const root = document.getElementById("toast-root");
    if (!root) return;
    const labels = { success: ["✓", "Listo"], info: ["i", "Información"], error: ["!", "Atención"] };
    const [icon, title] = labels[type] || labels.info;
    const element = document.createElement("div");
    element.className = `toast toast--${type}`;
    element.setAttribute("role", "status");
    const iconElement = document.createElement("span");
    iconElement.className = "toast__icon";
    iconElement.textContent = icon;
    const content = document.createElement("div");
    const heading = document.createElement("strong");
    heading.textContent = title;
    const text = document.createElement("p");
    text.textContent = message;
    content.append(heading, text);
    const close = document.createElement("button");
    close.type = "button";
    close.className = "toast__close";
    close.setAttribute("aria-label", "Cerrar aviso");
    close.textContent = "×";
    close.addEventListener("click", () => element.remove());
    element.append(iconElement, content, close);
    root.append(element);
    window.setTimeout(() => element.remove(), 4400);
}

export function notifyError(error) {
    console.error(error);
    const normalized = error instanceof ApiError ? error : new ApiError("Ocurrió un error inesperado", ["Inténtalo de nuevo en unos momentos."]);
    document.dispatchEvent(new CustomEvent("app:error", { detail: normalized }));
}

