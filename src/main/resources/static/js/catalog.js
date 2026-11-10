
import {
    apiJson,
    escapeHtml,
    formatMoney,
    getSession,
    hasSession,
    notifyError,
    toast
} from "./app.js";

const grid = document.getElementById("products-grid");
const meta = document.getElementById("catalog-meta");
const pagination = document.getElementById("catalog-pagination");

const PAGE_SIZE = 10;

let currentPage = 0;
let totalPages = 0;

let catalogHandlers = {
    onOpenModal: null,
    onOperationError: null
};

export function setCatalogHandlers(handlers = {}) {
    catalogHandlers = {
        ...catalogHandlers,
        ...handlers
    };
}

export async function loadProducts(page = currentPage) {
    if (!grid) {
        console.error("No se encontró el contenedor #products-grid.");
        return;
    }

    grid.innerHTML = `
        <div class="catalog-loading">
            <p>Cargando productos...</p>
        </div>`;

    if (pagination) {
        pagination.hidden = true;
        pagination.replaceChildren();
    }

    try {
        const esAdmin = getSession()?.isAdmin === true;
        const endpoint = esAdmin
            ? "/admin/productos/listar"
            : "/productos/listar";

        const response = await apiJson(
            `${endpoint}?page=${page}&size=${PAGE_SIZE}`
        );

        const products = response.content || [];
        const totalElements = Number(response.totalElements) || 0;
        const pages = Number(response.totalPages) || 0;

        currentPage = Number(response.number) || 0;
        totalPages = pages;

        if (meta) {
            meta.textContent = `${totalElements} productos disponibles`;
        }

        if (products.length === 0) {

            if (pages > 0 && page > pages - 1) {
                return loadProducts(pages - 1);
            }

            grid.innerHTML = `
                <div class="catalog-empty">
                    <h3>No hay productos disponibles</h3>
                    <p>Vuelve más tarde para descubrir nuestros productos.</p>
                </div>`;

            renderPagination();

            return;
        }

        grid.replaceChildren();

        products.forEach((product) => {
            grid.appendChild(createProductCard(product));
        });

        renderPagination();

    } catch (error) {
        console.error("Error al cargar los productos:", error);

        if (typeof catalogHandlers.onOperationError === "function") {
            catalogHandlers.onOperationError(error);
        } else {
            notifyError(error);
        }

        if (pagination) {
            pagination.hidden = true;
            pagination.replaceChildren();
        }

        grid.innerHTML = `
            <div class="catalog-error">
                <h3>No fue posible cargar los productos</h3>
                <p>Intenta nuevamente dentro de un momento.</p>
                <button type="button" class="btn btn--primary" data-retry-products>
                    Reintentar
                </button>
            </div>`;

        grid.querySelector("[data-retry-products]")?.addEventListener(
            "click",
            () => loadProducts(currentPage)
        );
    }
}

/**
 * Pinta los controles de paginación del catálogo.
 */
function renderPagination() {

    if (!pagination) return;

    pagination.replaceChildren();

    if (totalPages <= 1) {
        pagination.hidden = true;
        return;
    }

    pagination.hidden = false;

    const previousButton = document.createElement("button");

    previousButton.type = "button";
    previousButton.className = "btn btn--secondary";
    previousButton.textContent = "Anterior";
    previousButton.disabled = currentPage <= 0;
    previousButton.addEventListener("click", () => loadProducts(currentPage - 1));

    const status = document.createElement("span");

    status.className = "pagination__status";
    status.setAttribute("aria-live", "polite");
    status.textContent = `Página ${currentPage + 1} de ${totalPages}`;

    const nextButton = document.createElement("button");

    nextButton.type = "button";
    nextButton.className = "btn btn--secondary";
    nextButton.textContent = "Siguiente";
    nextButton.disabled = currentPage >= totalPages - 1;
    nextButton.addEventListener("click", () => loadProducts(currentPage + 1));

    pagination.append(previousButton, status, nextButton);
}

/**
 * Indica si el error está relacionado con stock o disponibilidad.
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
        message.includes("agotado")
    );
}

/**
 * Obtiene hasta dos iniciales a partir del nombre del producto.
 * Ejemplos:
 * "Café Orgánico" -> "CO"
 * "Laptop" -> "L"
 */
function getProductInitials(name) {
    const words = String(name || "Mercado Prime")
        .trim()
        .split(/\s+/)
        .filter(Boolean);

    return words
        .slice(0, 2)
        .map((word) => word.charAt(0).toUpperCase())
        .join("") || "MP";
}

function createProductCard(product) {
    const card = document.createElement("article");
    card.className = "product-card";

    const stock = Math.max(0, Number(product.stock) || 0);

    const inactivo = product.activo === false;

    const available =
        !inactivo &&
        product.disponible !== false &&
        stock > 0;

    const category =
        typeof product.categoria === "object"
            ? product.categoria?.nombre
            : product.categoria;

    const productName = product.nombre || "Producto";
    const initials = getProductInitials(productName);

    card.innerHTML = `
        <div class="product-card__art" aria-hidden="true">
            <span class="product-card__initials">${escapeHtml(initials)}</span>
        </div>

        <div class="product-card__content">
            <span class="product-card__category">
                ${escapeHtml(category || "General")}
            </span>

            <h3 class="product-card__title">
                ${escapeHtml(productName)}
            </h3>

            <p class="product-card__description">
                ${escapeHtml(product.descripcion || "Sin descripción disponible.")}
            </p>

            <p class="product-card__stock ${available ? "is-available" : "is-unavailable"}">
                ${inactivo ? "Inactivo" : available ? `Disponibles: ${stock}` : "Agotado"}
            </p>

            <div class="product-card__footer">
                <div class="product-card__price">
                    ${formatMoney(product.precio)}
                </div>
            </div>

            <div class="product-card__actions">
                <div
                    class="product-card__quantity"
                    role="group"
                    aria-label="Cantidad de ${escapeHtml(productName)}"
                >
                    <button
                        type="button"
                        data-qty-decrease
                        aria-label="Disminuir cantidad"
                        ${!available ? "disabled" : ""}
                    >
                        &minus;
                    </button>

                    <output
                        data-qty-value
                        aria-live="polite"
                        aria-label="Cantidad seleccionada"
                    >1</output>

                    <button
                        type="button"
                        data-qty-increase
                        aria-label="Aumentar cantidad"
                        ${!available ? "disabled" : ""}
                    >
                        +
                    </button>
                </div>

                <button
                    type="button"
                    class="btn btn--dark product-card__add"
                    data-add-cart
                    ${!available ? "disabled" : ""}
                >
                    Agregar al carrito
                </button>
            </div>
        </div>`;

    const decreaseButton = card.querySelector("[data-qty-decrease]");
    const increaseButton = card.querySelector("[data-qty-increase]");
    const quantityValue = card.querySelector("[data-qty-value]");
    const addButton = card.querySelector("[data-add-cart]");

    let quantity = 1;
    const maxQuantity = Math.max(1, stock);

    function updateQuantity(nextQuantity) {
        quantity = Math.min(maxQuantity, Math.max(1, nextQuantity));

        if (quantityValue) {
            quantityValue.textContent = String(quantity);
        }

        if (decreaseButton) {
            decreaseButton.disabled = !available || quantity <= 1;
        }

        if (increaseButton) {
            increaseButton.disabled = !available || quantity >= maxQuantity;
        }
    }

    updateQuantity(quantity);

    decreaseButton?.addEventListener("click", () => {
        updateQuantity(quantity - 1);
    });

    increaseButton?.addEventListener("click", () => {
        updateQuantity(quantity + 1);
    });

    addButton?.addEventListener("click", async () => {
        document.dispatchEvent(new CustomEvent("app:clear-operation-error"));

        if (!hasSession()) {
            openModalFromCatalog("login", {
                context: "Inicia sesión para agregar productos al carrito.",
                copy: "Si aún no tienes cuenta, puedes crear una."
            });
            return;
        }

        const cantidad = quantity;

        if (cantidad > stock) {
            toast("La cantidad supera el stock disponible.", "error");
            updateQuantity(stock);
            return;
        }

        addButton.disabled = true;
        addButton.textContent = "Agregando...";

        let stockError = false;

        try {
            await apiJson("/carrito", {
                method: "POST",
                body: {
                    productoId: product.id,
                    cantidad
                }
            });

            toast("Producto agregado al carrito.", "success");

            document.dispatchEvent(new CustomEvent("cart:updated"));

            updateQuantity(1);

        } catch (error) {
            console.error("Error al agregar producto al carrito:", error);

            if (typeof catalogHandlers.onOperationError === "function") {
                catalogHandlers.onOperationError(error);
            } else {
                notifyError(error);
            }

            stockError = isStockError(error);

            if (stockError) {
                await loadProducts(currentPage);
            }

        } finally {

            if (addButton.isConnected) {
                addButton.disabled = false;
                addButton.textContent = "Agregar al carrito";
            }
        }
    });

    return card;
}

function openModalFromCatalog(name, options) {
    if (typeof catalogHandlers.onOpenModal === "function") {
        catalogHandlers.onOpenModal(name, options);
    }
}
