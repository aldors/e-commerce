import {
    apiJson,
    clearSession,
    getSession,
    hasSession,
    notifyError,
    scheduleSessionExpiration,
    toast
} from "./app.js";

import {
    renderHeader,
    setNavigationHandlers
} from "./navigation.js";

import {
    loadProducts,
    setCatalogHandlers
} from "./catalog.js";

import {
    openLogin,
    openRegister,
    openCart,
    openOrders,
    openProfile,
    openAccount,
    setAccountHandlers
} from "./account.js";

import {
    openAdmin,
    setAdminHandlers
} from "./admin.js";

import {
    closeModal,
    getModalBody,
    isModalOpen
} from "./modals.js";

/*
|--------------------------------------------------------------------------
| Estado de la aplicación
|--------------------------------------------------------------------------
*/

let currentUser = null;
let handlingSessionExpired = false;

/*
|--------------------------------------------------------------------------
| Manejo de errores
|--------------------------------------------------------------------------
*/

/**
 * Obtiene los mensajes adicionales de un error.
 */
function getErrorMessages(error) {

    if (!error) return [];

    const possibleErrors =
        error.errors ??
        error.validationErrors ??
        error.details;

    if (Array.isArray(possibleErrors)) {
        return possibleErrors.map((item) => {
            if (typeof item === "string") return item;

            return item.message || item.defaultMessage || String(item);
        });
    }

    if (possibleErrors && typeof possibleErrors === "object") {
        return Object.values(possibleErrors).map(String);
    }

    return [];
}

/**
 * Obtiene (o crea) el contenedor de avisos visibles en la página.
 */
function getPageFeedback() {

    let container = document.getElementById("page-feedback");

    if (!container) {

        container = document.createElement("div");
        container.id = "page-feedback";
        container.className = "page-feedback";
        container.setAttribute("aria-live", "polite");

        const main = document.getElementById("catalogo");

        if (main) {
            main.prepend(container);
        } else {
            document.body.prepend(container);
        }
    }

    return container;
}

/**
 * Limpia los avisos visibles en la página.
 */
function clearPageFeedback() {

    document.getElementById("page-feedback")?.replaceChildren();
}

/**
 * Muestra un error general de operación.
 */
export function showOperationError(error) {

    if (error?.status === 401) {
        return;
    }

    const container = document.createElement("section");

    container.className = "operation-error";
    container.setAttribute("role", "alert");

    const title = document.createElement("strong");
    title.textContent = "No fue posible completar la operación";

    const message = document.createElement("p");
    message.textContent =
        error?.message || "Ocurrió un error inesperado. Intenta nuevamente.";

    container.append(title, message);

    const errors = getErrorMessages(error);

    if (errors.length > 0) {

        const list = document.createElement("ul");

        errors.forEach((errorMessage) => {

            const item = document.createElement("li");
            item.textContent = errorMessage;

            list.appendChild(item);
        });

        container.appendChild(list);
    }

    const body = getModalBody();

    if (isModalOpen() && body) {

        body.querySelectorAll(".operation-error").forEach((element) => {
            element.remove();
        });

        body.prepend(container);
        body.scrollTop = 0;

        return;
    }

    const feedback = getPageFeedback();

    feedback.replaceChildren(container);

    feedback.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

/**
 * Muestra un error dentro de un formulario.
 */
export function showFormError(form, error) {

    if (!form) {
        showOperationError(error);
        return;
    }

    form.querySelector(".form-error")?.remove();

    const container = document.createElement("section");

    container.className = "form-error";
    container.setAttribute("role", "alert");

    const title = document.createElement("strong");
    title.textContent = "No fue posible procesar la solicitud";

    const message = document.createElement("p");
    message.textContent =
        error?.message || "Verifica los datos e intenta nuevamente.";

    container.append(title, message);

    const errors = getErrorMessages(error);

    if (errors.length > 0) {

        const list = document.createElement("ul");

        errors.forEach((errorMessage) => {

            const item = document.createElement("li");
            item.textContent = errorMessage;

            list.appendChild(item);
        });

        container.appendChild(list);
    }

    form.prepend(container);
}

/*
|--------------------------------------------------------------------------
| Navegación entre modales
|--------------------------------------------------------------------------
*/

/**
 * Abre un modal según su nombre.
 *
 * Los modales que requieren autenticación verifican primero
 * que exista una sesión activa.
 */
export function openNamedModal(name, options = {}) {

    clearPageFeedback();

    const protectedModals = [
        "cart",
        "orders",
        "profile",
        "account",
        "admin"
    ];

    if (protectedModals.includes(name) && !hasSession()) {

        openLogin(
            "Necesitas iniciar sesión para continuar.",
            "Inicia sesión con tu cuenta para acceder a esta sección."
        );

        return;
    }

    switch (name) {

        case "login":
            openLogin(options.context, options.copy);
            break;

        case "register":
            openRegister();
            break;

        case "cart":
            openCart();
            break;

        case "orders":
            openOrders();
            break;

        case "profile":
            openProfile();
            break;

        case "account":
            openAccount();
            break;

        case "admin":

            if (currentUser?.rol?.toUpperCase() !== "ADMIN") {
                toast("No tienes permisos para acceder a esta sección.", "error");
                return;
            }

            openAdmin();
            break;

        default:
            console.warn(`Modal no reconocido: ${name}`);
    }
}

/*
|--------------------------------------------------------------------------
| Sesión
|--------------------------------------------------------------------------
*/

/**
 * Consulta la información del usuario autenticado.
 */
async function loadCurrentUser() {

    if (!hasSession()) {
        currentUser = null;
        return null;
    }

    try {

        currentUser = await apiJson("/auth/me");

        return currentUser;

    } catch (error) {

        console.error("No fue posible recuperar la sesión:", error);

        if (error?.status >= 400 && error?.status < 500) {
            clearSession();

            currentUser = null;

            document.dispatchEvent(new CustomEvent("session:expired"));

            return null;
        }

        currentUser = getSession();

        return currentUser;
    }
}

/**
 * Se ejecuta cuando el usuario inicia sesión correctamente.
 */
async function handleAuthenticated(user) {

    handlingSessionExpired = false;

    currentUser = user;

    renderHeader(currentUser);

    scheduleSessionExpiration();

    await loadProducts();

    await updateCartBadge();
}

/**
 * Actualiza la interfaz cuando la sesión deja de ser válida.
 */
async function handleSessionExpired() {

    if (handlingSessionExpired) return;

    handlingSessionExpired = true;

    clearSession();

    currentUser = null;

    closeModal();

    document.dispatchEvent(new CustomEvent("app:clear-operation-error"));

    renderHeader(null);

    toast(
        "Tu sesión ha terminado. Inicia sesión nuevamente para continuar.",
        "info"
    );
}

/**
 * Actualiza la interfaz cuando el usuario cierra sesión manualmente.
 */
async function handleLoggedOut() {

    clearSession();

    currentUser = null;

    closeModal();

    document.dispatchEvent(new CustomEvent("app:clear-operation-error"));

    renderHeader(null);

    await loadProducts();

    toast("Sesión cerrada correctamente.", "success");
}

/*
|--------------------------------------------------------------------------
| Contador del carrito
|--------------------------------------------------------------------------
*/

/**
 * Consulta el carrito y actualiza el indicador del encabezado.
 */
export async function updateCartBadge() {

    if (!hasSession()) return;

    try {

        const cart = await apiJson("/carrito");

        const quantity = (cart.items || []).reduce(
            (total, item) => total + Number(item.cantidad || 0),
            0
        );

        const badge = document.getElementById("cart-badge");

        if (!badge) return;

        badge.textContent = String(quantity);
        badge.hidden = quantity === 0;

    } catch (error) {

        console.error("No fue posible actualizar el contador del carrito:", error);
    }
}

/*
|--------------------------------------------------------------------------
| Configuración de los módulos
|--------------------------------------------------------------------------
*/

/**
 * Conecta los módulos secundarios con este controlador.
 */
function configureModules() {

    setNavigationHandlers({
        onOpenModal: openNamedModal
    });

    setCatalogHandlers({
        onOpenModal: openNamedModal,
        onOperationError: showOperationError
    });

    setAccountHandlers({
        onAuthenticated: handleAuthenticated,
        onOpenModal: openNamedModal,
        onFormError: showFormError,
        onOperationError: showOperationError,

        onProfileUpdated: async () => {
            try {
                currentUser = await apiJson("/auth/me");
                renderHeader(currentUser);
            } catch (error) {
                showOperationError(error);
            }
        },

        onProductsChanged: async () => {
            await loadProducts();
        }
    });

    setAdminHandlers({
        onOperationError: showOperationError,
        onFormError: showFormError,

        onProductsChanged: async () => {
            await loadProducts();
        }
    });
}

/*
|--------------------------------------------------------------------------
| Eventos globales
|--------------------------------------------------------------------------
*/

/**
 * Configura los eventos de elementos que existen directamente
 * en el index.html.
 */
function configureGlobalEvents() {

    // Ir al catálogo desde el hero.
    document.querySelectorAll("[data-scroll-catalog]").forEach((button) => {

        button.addEventListener("click", (event) => {

            event.preventDefault();

            document.querySelector(".catalog-section")?.scrollIntoView({
                behavior: "smooth"
            });
        });
    });

    // Año actual en el footer.
    const year = document.getElementById("year");

    if (year) {
        year.textContent = String(new Date().getFullYear());
    }

    // Acceso a login o perfil desde el footer.
    document.querySelector("[data-footer-login]")?.addEventListener(
        "click",
        () => {

            if (hasSession()) {
                openNamedModal("profile");
            } else {
                openNamedModal("login");
            }
        }
    );

    // Acceso a las compras desde el footer.
    document.querySelector("[data-footer-orders]")?.addEventListener(
        "click",
        () => openNamedModal("orders")
    );

    // Limpieza de avisos visibles al iniciar una nueva operación.
    document.addEventListener("app:clear-operation-error", clearPageFeedback);

    // Actualización del contador después de modificar el carrito.
    document.addEventListener("cart:updated", updateCartBadge);

    // Sesión expirada.
    document.addEventListener("session:expired", handleSessionExpired);

    // Cierre de sesión manual.
    document.addEventListener("session:logout", handleLoggedOut);

    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible") {
            scheduleSessionExpiration();
        }
    });

    window.addEventListener("focus", scheduleSessionExpiration);

    window.addEventListener("storage", (event) => {
        if (event.key === "accessToken" || event.key === "refreshToken") {
            scheduleSessionExpiration();
        }
    });

    // Errores globales de la aplicación.
    document.addEventListener("app:error", (event) => {

        const error = event.detail?.error || event.detail;

        showOperationError(error);
    });
}

/*
|--------------------------------------------------------------------------
| Inicialización
|--------------------------------------------------------------------------
*/

/**
 * Inicializa la aplicación.
 */
async function bootstrap() {

    configureModules();

    configureGlobalEvents();

    currentUser = await loadCurrentUser();

    renderHeader(currentUser);

    scheduleSessionExpiration();

    await loadProducts();

    await updateCartBadge();
}

/*
|--------------------------------------------------------------------------
| Inicio automático
|--------------------------------------------------------------------------
*/

bootstrap().catch((error) => {

    console.error("Error al inicializar Mercado Prime:", error);

    notifyError(error);
});