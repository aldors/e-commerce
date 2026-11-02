import {
    apiJson,
    clearSession,
    formatDate,
    formatMoney,
    saveSession,
    toast,
    notifyError
} from "./app.js";

import {
    openModal,
    closeModal,
    getModalBody
} from "./modals.js";

/*
|--------------------------------------------------------------------------
| Configuración
|--------------------------------------------------------------------------
*/

let accountHandlers = {
    onAuthenticated: null,
    onOpenModal: null,
    onFormError: null,
    onOperationError: null,
    onProfileUpdated: null,
    onProductsChanged: null
};

let currentAccountTab = "name";

/**
 * Recibe las funciones del controlador principal.
 */
export function setAccountHandlers(handlers = {}) {
    accountHandlers = {
        ...accountHandlers,
        ...handlers
    };
}

/*
|--------------------------------------------------------------------------
| Funciones auxiliares
|--------------------------------------------------------------------------
*/

/**
 * Obtiene una plantilla HTML y devuelve una copia de su contenido.
 */
function cloneTemplate(templateId) {
    const template = document.getElementById(templateId);

    if (!template || !(template instanceof HTMLTemplateElement)) {
        console.error(`No se encontró la plantilla: ${templateId}`);
        return null;
    }

    return template.content.cloneNode(true);
}

/**
 * Abre un modal utilizando una plantilla HTML.
 */
function showAccountModal(title, templateId, options = {}) {
    return openModal({
        title,
        templateId,
        eyebrow: options.eyebrow || "",
        wide: options.wide || false,
        className: options.className || ""
    });
}

/**
 * Notifica los errores de una operación.
 */
function handleOperationError(error) {
    if (error?.status === 401) {
        return;
    }

    if (typeof accountHandlers.onOperationError === "function") {
        accountHandlers.onOperationError(error);
    } else {
        notifyError(error);
    }
}

/**
 * Notifica los errores de un formulario.
 */
function handleFormError(form, error) {
    if (typeof accountHandlers.onFormError === "function") {
        accountHandlers.onFormError(form, error);
    } else {
        handleOperationError(error);
    }
}

/**
 * Solicita abrir otro modal al controlador principal.
 */
function requestModal(name) {
    if (typeof accountHandlers.onOpenModal === "function") {
        accountHandlers.onOpenModal(name);
    }
}

/**
 * Notifica que cambió la información del carrito.
 */
function notifyCartUpdated() {
    document.dispatchEvent(new CustomEvent("cart:updated"));
}

/*
|--------------------------------------------------------------------------
| Inicio de sesión
|--------------------------------------------------------------------------
*/

/**
 * Abre el formulario de inicio de sesión.
 */
export function openLogin(context = "", copy = "") {

    const body = showAccountModal(
        "Iniciar sesión",
        "login-template",
        {
            eyebrow: "TU CUENTA",
            className: "auth-modal"
        }
    );

    if (!body) return;

    const form = body.querySelector("#login-form");
    const copyElement = body.querySelector("[data-auth-copy]");

    const authMessage = [context, copy]
        .filter(Boolean)
        .join(" ");

    if (copyElement && authMessage) {
        copyElement.textContent = authMessage;
    }

    body.querySelector("[data-switch-register]")?.addEventListener(
        "click",
        () => requestModal("register")
    );

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const submitButton = form.querySelector('[type="submit"]');

        const email = form.elements.email.value.trim();
        const password = form.elements.password.value;

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Ingresando...";
        }

        try {

            const response = await apiJson("/auth/login", {
                method: "POST",
                auth: false,
                retry: false,
                body: {
                    email,
                    password
                }
            });

            saveSession(response);

            const user = await apiJson("/auth/me");

            closeModal();

            if (typeof accountHandlers.onAuthenticated === "function") {
                await accountHandlers.onAuthenticated(user);
            }

            toast("Sesión iniciada correctamente.", "success");

        } catch (error) {

            console.error("Error al iniciar sesión:", error);

            handleFormError(form, error);

        } finally {

            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = "Iniciar sesión";
            }
        }
    });
}

/*
|--------------------------------------------------------------------------
| Registro
|--------------------------------------------------------------------------
*/

/**
 * Abre el formulario de registro.
 */
export function openRegister() {

    const body = showAccountModal(
        "Crear cuenta",
        "register-template",
        {
            eyebrow: "ÚNETE A NOSOTROS",
            className: "auth-modal"
        }
    );

    if (!body) return;

    const form = body.querySelector("#register-form");

    body.querySelector("[data-switch-login]")?.addEventListener(
        "click",
        () => requestModal("login")
    );

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const submitButton = form.querySelector('[type="submit"]');

        const nombre = form.elements.nombre.value.trim();
        const email = form.elements.email.value.trim();
        const password = form.elements.password.value;

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Creando cuenta...";
        }

        try {

            await apiJson("/auth/registro", {
                method: "POST",
                auth: false,
                retry: false,
                body: {
                    nombre,
                    email,
                    password
                }
            });

            toast("Cuenta creada correctamente. Ahora inicia sesión.", "success");

            openLogin(
                "",
                "Tu cuenta fue creada. Ingresa con tus datos para continuar."
            );

        } catch (error) {

            console.error("Error al registrar usuario:", error);

            handleFormError(form, error);

        } finally {

            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = "Crear cuenta";
            }
        }
    });
}

/*
|--------------------------------------------------------------------------
| Carrito
|--------------------------------------------------------------------------
*/

/**
 * Obtiene y muestra los productos del carrito.
 */
export async function openCart() {

    const body = showAccountModal(
        "Mi carrito",
        "cart-template",
        {
            eyebrow: "TU COMPRA",
            wide: true
        }
    );

    if (!body) return;

    body.querySelector("#checkout")?.addEventListener(
        "click",
        checkout
    );

    await loadCartInto(body);
}

/**
 * Consulta el carrito y lo renderiza dentro del modal actual.
 */
async function loadCartInto(body) {

    if (!body) return;

    const itemsContainer = body.querySelector("#cart-items");

    if (itemsContainer) {
        itemsContainer.innerHTML = "<p>Cargando carrito...</p>";
    }

    try {

        const cart = await apiJson("/carrito");

        renderCart(body, cart);

    } catch (error) {

        console.error("Error al cargar el carrito:", error);

        handleOperationError(error);

        if (!itemsContainer) return;

        itemsContainer.innerHTML = `
            <div class="cart-error">
                <p>No fue posible cargar tu carrito.</p>
                <button type="button" class="btn btn--secondary" data-retry-cart>
                    Reintentar
                </button>
            </div>
        `;

        itemsContainer.querySelector("[data-retry-cart]")?.addEventListener(
            "click",
            () => loadCartInto(body)
        );
    }
}

/**
 * Determina si un producto del carrito todavía puede comprarse.
 */
function getItemAvailability(item) {

    const quantity = Number(item.cantidad) || 0;
    const stockValue = Number(item.stock);
    const hasStockInfo =
        item.stock !== null &&
        item.stock !== undefined &&
        Number.isFinite(stockValue);
    const stock = hasStockInfo ? Math.max(0, stockValue) : quantity;
    const active = item.activo !== false;

    if (!active || stock <= 0) {
        return {
            available: false,
            soldOut: true,
            label: "Agotado"
        };
    }

    if (quantity > stock) {
        return {
            available: false,
            soldOut: false,
            label: `Solo quedan ${stock}`
        };
    }

    return {
        available: true,
        soldOut: false,
        label: ""
    };
}

/**
 * Pinta los productos del carrito y el estado del botón de compra.
 */
function renderCart(body, cart) {

    const itemsContainer = body.querySelector("#cart-items");
    const totalElement = body.querySelector("#cart-total");
    const checkoutButton = body.querySelector("#checkout");
    const notice = body.querySelector("[data-checkout-notice]");

    if (!itemsContainer) return;

    const items = cart.items || [];

    itemsContainer.replaceChildren();

    if (items.length === 0) {

        itemsContainer.innerHTML = `
            <div class="cart-empty">
                <h3>Tu carrito está vacío</h3>
                <p>Agrega productos para comenzar tu compra.</p>
            </div>
        `;

        if (totalElement) {
            totalElement.textContent = formatMoney(0);
        }

        if (checkoutButton) {
            checkoutButton.disabled = true;
        }

        if (notice) {
            notice.hidden = true;
            notice.textContent = "";
        }

        return;
    }

    let total = 0;
    let unavailableCount = 0;

    items.forEach((item) => {

        itemsContainer.appendChild(createCartItem(item));

        total += Number(item.precio) * Number(item.cantidad);

        if (!getItemAvailability(item).available) {
            unavailableCount += 1;
        }
    });

    if (totalElement) {
        totalElement.textContent = formatMoney(total);
    }

    if (checkoutButton) {
        checkoutButton.disabled = unavailableCount > 0;
    }

    if (notice) {
        notice.hidden = unavailableCount === 0;
        notice.textContent = unavailableCount > 0
            ? "Hay productos agotados o sin stock suficiente. Elimínalos para continuar."
            : "";
    }
}

/**
 * Construye un elemento del carrito a partir de su plantilla.
 */
function createCartItem(item) {

    const fragment = cloneTemplate("cart-item-template");

    if (!fragment) {
        return document.createElement("div");
    }

    const element = fragment.querySelector(".cart-item");

    const name = item.nombre || "Producto";
    const quantity = Number(item.cantidad) || 0;
    const price = Number(item.precio) || 0;
    const subtotal = quantity * price;
    const availability = getItemAvailability(item);

    const initialsElement = element.querySelector("[data-item-initials]");
    const nameElement = element.querySelector("[data-item-name]");
    const quantityPriceElement = element.querySelector("[data-item-quantity-price]");
    const statusElement = element.querySelector("[data-item-status]");
    const subtotalElement = element.querySelector("[data-item-subtotal]");

    if (initialsElement) {
        initialsElement.textContent = name.substring(0, 1).toUpperCase();
    }

    if (nameElement) {
        nameElement.textContent = name;
    }

    if (quantityPriceElement) {
        quantityPriceElement.textContent =
            `${quantity} × ${formatMoney(price)}`;
    }

    if (!availability.available) {
        element.classList.add("is-unavailable");
        element.setAttribute("aria-disabled", "true");

        if (statusElement) {
            statusElement.hidden = false;
            statusElement.textContent = availability.label;
            statusElement.classList.toggle(
                "is-sold-out",
                availability.soldOut
            );
        }
    }

    if (subtotalElement) {
        subtotalElement.textContent = formatMoney(subtotal);
    }

    element.querySelector("[data-remove]")?.addEventListener(
        "click",
        () => removeCartItem(item.productoId)
    );

    return element;
}

/**
 * Elimina un producto del carrito.
 */
async function removeCartItem(productoId) {

    try {

        await apiJson(`/carrito/${productoId}`, {
            method: "DELETE"
        });

        toast("Producto eliminado del carrito.", "success");

        notifyCartUpdated();

        await openCart();

    } catch (error) {

        console.error("Error al eliminar producto:", error);

        handleOperationError(error);
    }
}

/**
 * Indica si el error de compra está relacionado con el stock.
 */
function isStockError(error) {

    const messages = [
        error?.message,
        ...(Array.isArray(error?.errors) ? error.errors : [])
    ]
        .filter(Boolean)
        .map((message) => String(message).toLowerCase());

    return messages.some((message) =>
        message.includes("stock") ||
        message.includes("disponible") ||
        message.includes("agotado") ||
        message.includes("vacío") ||
        message.includes("vacio")
    );
}

/**
 * Finaliza la compra.
 */
async function checkout() {

    const body = getModalBody();
    const checkoutButton = body?.querySelector("#checkout");

    let stockError = false;

    if (checkoutButton) {
        checkoutButton.disabled = true;
        checkoutButton.textContent = "Procesando compra...";
    }

    try {

        await apiJson("/ordenes/checkout", {
            method: "POST"
        });

        closeModal();

        notifyCartUpdated();

        if (typeof accountHandlers.onProductsChanged === "function") {
            await accountHandlers.onProductsChanged();
        }

        toast("Compra realizada correctamente.", "success");

        requestModal("orders");

    } catch (error) {

        console.error("Error al realizar la compra:", error);

        handleOperationError(error);

        stockError = isStockError(error);

        if (stockError && body?.isConnected) {
            await loadCartInto(body);
        }

    } finally {

        if (checkoutButton && checkoutButton.isConnected) {
            checkoutButton.textContent = "Finalizar compra";

            if (!stockError) {
                checkoutButton.disabled = false;
            }
        }
    }
}

/*
|--------------------------------------------------------------------------
| Historial de compras
|--------------------------------------------------------------------------
*/

/**
 * Obtiene y muestra las órdenes del usuario.
 */
export async function openOrders() {

    const body = showAccountModal(
        "Mis compras",
        "orders-template",
        {
            eyebrow: "TU HISTORIAL",
            wide: true
        }
    );

    if (!body) return;

    const ordersContainer = body.querySelector("#orders");

    ordersContainer.innerHTML = "<p>Cargando compras...</p>";

    try {

        const orders = await apiJson("/ordenes");

        ordersContainer.replaceChildren();

        if (!orders || orders.length === 0) {

            ordersContainer.innerHTML = `
                <div class="orders-empty">
                    <h3>Aún no tienes compras</h3>
                    <p>Cuando realices una compra, aparecerá aquí.</p>
                </div>
            `;

            return;
        }

        orders.forEach((order) => {

            const orderElement = createOrderItem(order);

            ordersContainer.appendChild(orderElement);
        });

    } catch (error) {

        console.error("Error al cargar las compras:", error);

        handleOperationError(error);

        ordersContainer.innerHTML = `
            <div class="orders-error">
                <p>No fue posible cargar tu historial de compras.</p>

                <button type="button" class="btn btn--secondary" data-retry-orders>
                    Reintentar
                </button>
            </div>
        `;

        ordersContainer.querySelector("[data-retry-orders]")?.addEventListener(
            "click",
            openOrders
        );
    }
}

/**
 * Construye una orden utilizando su plantilla HTML.
 */
function createOrderItem(order) {

    const fragment = cloneTemplate("order-item-template");

    if (!fragment) {
        return document.createElement("div");
    }

    const element = fragment.querySelector(".order");

    const idElement = element.querySelector("[data-order-id]");
    const dateElement = element.querySelector("[data-order-date]");
    const statusElement = element.querySelector("[data-order-status]");
    const totalElement = element.querySelector("[data-order-total]");
    const detailsContainer = element.querySelector("[data-order-details]");

    if (idElement) {
        idElement.textContent = `Pedido #${order.id}`;
    }

    if (dateElement) {
        dateElement.textContent = formatDate(order.fecha);
    }

    if (statusElement) {
        statusElement.textContent = order.estado || "Sin estado";
    }

    if (totalElement) {
        totalElement.textContent = formatMoney(order.total);
    }

    if (detailsContainer) {

        detailsContainer.replaceChildren();

        (order.detalles || []).forEach((detail) => {

            const detailFragment = cloneTemplate("order-detail-template");

            if (!detailFragment) return;

            const detailElement = detailFragment.querySelector("[data-detail-product]");

            const priceElement = detailFragment.querySelector("[data-detail-price]");

            if (detailElement) {
                detailElement.textContent =
                    `${detail.producto} × ${detail.cantidad}`;
            }

            if (priceElement) {
                priceElement.textContent = formatMoney(
                    Number(detail.precioUnitario) * Number(detail.cantidad)
                );
            }

            detailsContainer.appendChild(detailFragment);
        });
    }

    return element;
}

/*
|--------------------------------------------------------------------------
| Perfil
|--------------------------------------------------------------------------
*/

/**
 * Obtiene y muestra la información del usuario.
 */
export async function openProfile() {

    const body = showAccountModal(
        "Mi perfil",
        "profile-template",
        { eyebrow: "TU INFORMACIÓN" }
    );

    if (!body) return;

    const loadingName = body.querySelector("[data-profile-name]");

    if (loadingName) {
        loadingName.textContent = "Cargando...";
    }

    try {

        const user = await apiJson("/auth/me");

        const avatar = body.querySelector("[data-profile-avatar]");
        const name = body.querySelector("[data-profile-name]");
        const email = body.querySelector("[data-profile-email]");
        const role = body.querySelector("[data-profile-role]");

        if (avatar) {
            avatar.textContent = (user.nombre || user.email || "U")
                .substring(0, 1)
                .toUpperCase();
        }

        if (name) {
            name.textContent = user.nombre || "";
        }

        if (email) {
            email.textContent = user.email || "";
        }

        if (role) {
            role.textContent = user.rol === "ADMIN" ? "Administrador" : "Cliente";
        }

        body.querySelector("[data-open-account]")?.addEventListener(
            "click",
            () => requestModal("account")
        );

        body.querySelector("[data-open-orders]")?.addEventListener(
            "click",
            () => requestModal("orders")
        );

    } catch (error) {

        console.error("Error al cargar el perfil:", error);

        if (error?.status === 401 || error?.status === 403) {
            return;
        }

        handleOperationError(error);

        if (loadingName) {
            loadingName.textContent = "No disponible";
        }

        body.querySelector("[data-retry-profile]")?.remove();

        const retryButton = document.createElement("button");

        retryButton.type = "button";
        retryButton.className = "btn btn--secondary";
        retryButton.dataset.retryProfile = "";
        retryButton.textContent = "Reintentar";
        retryButton.addEventListener("click", openProfile);

        const actions = body.querySelector(".profile__actions");

        if (actions) {
            actions.prepend(retryButton);
        } else {
            body.append(retryButton);
        }
    }
}

/*
|--------------------------------------------------------------------------
| Actualización de cuenta
|--------------------------------------------------------------------------
*/

/**
 * Abre el panel de configuración de la cuenta.
 */
export async function openAccount() {

    const body = showAccountModal(
        "Configuración de cuenta",
        "account-template",
        {
            eyebrow: "ADMINISTRA TU CUENTA",
            wide: true
        }
    );

    if (!body) return;

    currentAccountTab = "name";

    const tabs = body.querySelectorAll("[data-tab]");

    tabs.forEach((tab) => {

        tab.addEventListener("click", () => {

            const selectedTab = tab.dataset.tab;

            currentAccountTab = selectedTab;

            renderAccountPanel(body, selectedTab);
            updateAccountTabs(body, selectedTab);
        });
    });

    renderAccountPanel(body, currentAccountTab);
    updateAccountTabs(body, currentAccountTab);
}

/**
 * Actualiza visualmente la pestaña seleccionada.
 */
function updateAccountTabs(body, selectedTab) {

    body.querySelectorAll("[data-tab]").forEach((tab) => {

        const active = tab.dataset.tab === selectedTab;

        tab.classList.toggle("is-active", active);

        tab.setAttribute("aria-selected", String(active));
    });
}

/**
 * Renderiza el formulario correspondiente a cada pestaña.
 */
function renderAccountPanel(body, tabName) {

    const panelSlot = body.querySelector("#account-form-slot");

    if (!panelSlot) return;

    const fragment = cloneTemplate("account-panel-template");

    if (!fragment) return;

    const panel = fragment.querySelector(".account-panel");

    const title = panel.querySelector("[data-panel-title]");
    const hint = panel.querySelector("[data-panel-hint]");
    const fields = panel.querySelector("[data-panel-fields]");

    const configurations = {

        name: {
            title: "Actualizar nombre",
            hint: "Modifica el nombre asociado a tu cuenta.",
            fields: [
                {
                    name: "nombre",
                    label: "Nuevo nombre",
                    type: "text",
                    placeholder: "Ingresa tu nombre"
                }
            ]
        },

        email: {
            title: "Actualizar correo electrónico",
            hint: "Actualiza el correo electrónico de tu cuenta.",
            fields: [
                {
                    name: "email",
                    label: "Nuevo correo electrónico",
                    type: "email",
                    placeholder: "correo@ejemplo.com"
                }
            ]
        },

        password: {
            title: "Cambiar contraseña",
            hint: "Introduce tu contraseña actual y después la nueva.",
            fields: [
                {
                    name: "passwordActual",
                    label: "Contraseña actual",
                    type: "password",
                    placeholder: "Contraseña actual"
                },
                {
                    name: "passwordNueva",
                    label: "Nueva contraseña",
                    type: "password",
                    placeholder: "Nueva contraseña"
                }
            ]
        }
    };

    const configuration = configurations[tabName];

    if (!configuration) return;

    title.textContent = configuration.title;
    hint.textContent = configuration.hint;

    fields.replaceChildren();

    configuration.fields.forEach((field) => {

        const label = document.createElement("label");
        label.className = "form-field";

        const labelText = document.createElement("span");
        labelText.textContent = field.label;

        const input = document.createElement("input");
        input.type = field.type;
        input.name = field.name;
        input.placeholder = field.placeholder;
        input.required = true;
        input.autocomplete = "off";

        label.append(labelText, input);

        fields.appendChild(label);
    });

    panel.addEventListener("submit", async (event) => {
        event.preventDefault();

        await updateAccount(tabName, panel);
    });

    panelSlot.replaceChildren(fragment);
}

/**
 * Envía al backend la actualización correspondiente.
 */
async function updateAccount(tabName, form) {

    const submitButton = form.querySelector('[type="submit"]');

    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Guardando...";
    }

    try {

        if (tabName === "name") {

            const nombre = form.elements.nombre.value.trim();

            await apiJson("/auth/actualizar-nombre", {
                method: "PUT",
                body: {
                    nombre
                }
            });

            if (typeof accountHandlers.onProfileUpdated === "function") {
                await accountHandlers.onProfileUpdated();
            }

            toast("Información actualizada correctamente.", "success");

            closeModal();
            requestModal("profile");

            return;
        }

        if (tabName === "email") {

            const email = form.elements.email.value.trim();

            await apiJson("/auth/actualizar-email", {
                method: "PUT",
                body: {
                    email
                }
            });

            clearSession();

            document.dispatchEvent(new CustomEvent("session:logout"));

            openLogin(
                "",
                "Tu correo cambió. Inicia sesión de nuevo para continuar."
            );

            return;
        }

        if (tabName === "password") {

            const passwordActual = form.elements.passwordActual.value;
            const passwordNueva = form.elements.passwordNueva.value;

            await apiJson("/auth/actualizar-contraseña", {
                method: "PUT",
                body: {
                    passwordActual,
                    passwordNueva
                }
            });

            clearSession();

            document.dispatchEvent(new CustomEvent("session:logout"));

            openLogin(
                "",
                "Tu contraseña cambió. Inicia sesión de nuevo para continuar."
            );

            return;
        }

    } catch (error) {

        console.error("Error al actualizar la cuenta:", error);

        handleFormError(form, error);

    } finally {

        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = "Guardar cambios";
        }
    }
}