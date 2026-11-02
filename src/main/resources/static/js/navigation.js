import {
    escapeHtml,
    hasSession,
    initials,
    logout
} from "./app.js";

const header = document.getElementById("app-header");

let currentUser = null;
let navigationHandlers = {
    onOpenModal: null
};

/**
 * Permite conectar las acciones de navegación con pages.js.
 */
export function setNavigationHandlers(handlers = {}) {
    navigationHandlers = {
        ...navigationHandlers,
        ...handlers
    };
}

/**
 * Renderiza el encabezado según el estado de sesión.
 */
export function renderHeader(user = null) {

    if (!header) {
        console.error("No se encontró el elemento #app-header.");
        return;
    }

    currentUser = user;

    const authenticated = hasSession();
    const isAdmin = currentUser?.rol?.toUpperCase() === "ADMIN";

    const theme = document.documentElement.dataset.theme || "light";
    const themeLabel = theme === "dark"
        ? "Cambiar a tema claro"
        : "Cambiar a tema oscuro";

    const themeIcon = theme === "dark" ? "☀" : "☾";

    let actions = "";

    if (authenticated) {

        const userInitials = escapeHtml(
            initials(currentUser?.nombre || currentUser?.email || "Usuario")
        );

        const userName = escapeHtml(
            currentUser?.nombre || "Mi perfil"
        );

        actions = `
            <a href="#compras" class="nav__link" data-modal="orders">
                Mis compras
            </a>

            ${isAdmin ? `
                <a href="#administracion" class="nav__link" data-modal="admin">
                    Administración
                </a>
            ` : ""}

            <button
                type="button"
                class="nav__link nav__cart"
                data-modal="cart"
                aria-label="Abrir carrito"
            >
                Carrito
                <span class="nav__cart-badge" id="cart-badge" hidden>0</span>
            </button>

            <button
                type="button"
                class="nav__profile"
                data-modal="profile"
                aria-label="Abrir perfil de ${userName}"
            >
                <span class="nav__avatar">${userInitials}</span>
                <span class="nav__profile-name">${userName}</span>
            </button>

            <button
                type="button"
                class="nav__link nav__theme"
                data-theme-toggle
                aria-label="${themeLabel}"
                title="${themeLabel}"
            >
                <span aria-hidden="true">${themeIcon}</span>
            </button>

            <button
                type="button"
                class="nav__link nav__logout"
                data-logout
            >
                Salir
            </button>
        `;

    } else {

        actions = `
            <button
                type="button"
                class="nav__link"
                data-modal="login"
            >
                Iniciar sesión
            </button>

            <button
                type="button"
                class="nav__link nav__register"
                data-modal="register"
            >
                Crear cuenta
            </button>

            <button
                type="button"
                class="nav__link nav__theme"
                data-theme-toggle
                aria-label="${themeLabel}"
                title="${themeLabel}"
            >
                <span aria-hidden="true">${themeIcon}</span>
            </button>
        `;
    }

    header.innerHTML = `
        <div class="nav">
            <a href="#" class="nav__brand" data-home aria-label="Mercado Prime, inicio">
                Mercado <span>Prime</span>
            </a>

            <button
                type="button"
                class="nav__menu-btn"
                id="nav-menu-btn"
                aria-label="Abrir menú de navegación"
                aria-expanded="false"
                aria-controls="nav-actions"
            >
                <span></span>
                <span></span>
                <span></span>
            </button>

            <nav
                class="nav__actions"
                id="nav-actions"
                aria-label="Navegación principal"
            >
                ${actions}
            </nav>
        </div>
    `;

    attachNavigationEvents();
}

/**
 * Asigna los eventos a los elementos del encabezado.
 */
function attachNavigationEvents() {

    // Volver al inicio.
    header.querySelector("[data-home]")?.addEventListener("click", (event) => {
        event.preventDefault();

        closeMobileMenu();

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    });

    // Abrir los distintos modales.
    header.querySelectorAll("[data-modal]").forEach((button) => {

        button.addEventListener("click", (event) => {

            event.preventDefault();

            const modalName = button.dataset.modal;

            closeMobileMenu();

            if (typeof navigationHandlers.onOpenModal === "function") {
                navigationHandlers.onOpenModal(modalName);
            }
        });
    });

    // Cambiar el tema.
    header.querySelector("[data-theme-toggle]")?.addEventListener(
        "click",
        toggleTheme
    );

    // Cerrar sesión.
    header.querySelector("[data-logout]")?.addEventListener(
        "click",
        async () => {
            closeMobileMenu();

            try {
                await logout();
            } finally {
                document.dispatchEvent(new CustomEvent("session:logout"));
            }
        }
    );

    // Menú hamburguesa.
    const menuButton = header.querySelector("#nav-menu-btn");
    const navActions = header.querySelector("#nav-actions");

    menuButton?.addEventListener("click", () => {

        const isOpen = navActions.classList.toggle("is-open");

        menuButton.classList.toggle("is-active", isOpen);
        menuButton.setAttribute("aria-expanded", String(isOpen));

        menuButton.setAttribute(
            "aria-label",
            isOpen ? "Cerrar menú de navegación" : "Abrir menú de navegación"
        );
    });
}

/**
 * Cierra el menú hamburguesa.
 */
function closeMobileMenu() {

    const menuButton = header?.querySelector("#nav-menu-btn");
    const navActions = header?.querySelector("#nav-actions");

    navActions?.classList.remove("is-open");
    menuButton?.classList.remove("is-active");

    menuButton?.setAttribute("aria-expanded", "false");
    menuButton?.setAttribute("aria-label", "Abrir menú de navegación");
}

/**
 * Alterna entre el tema claro y oscuro.
 */
export function toggleTheme() {

    const currentTheme = document.documentElement.dataset.theme || "light";

    const newTheme = currentTheme === "dark"
        ? "light"
        : "dark";

    document.documentElement.dataset.theme = newTheme;

    localStorage.setItem("theme", newTheme);

    renderHeader(currentUser);
}