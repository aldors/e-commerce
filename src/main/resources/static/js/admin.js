import {
    apiJson,
    confirmAction,
    formatMoney,
    toast,
    notifyError
} from "./app.js";

import {
    openModal
} from "./modals.js";

import { renderPageControls } from "./pagination.js";

/*
|--------------------------------------------------------------------------
| Configuración
|--------------------------------------------------------------------------
*/

let adminHandlers = {
    onOperationError: null,
    onFormError: null,
    onProductsChanged: null
};

const ADMIN_PAGE_SIZE = 10;

let currentAdminTab = "products";
let productsCache = [];
let categoriesCache = [];
let productsPage = 0;
let productsTotalPages = 0;
let productsTotalElements = 0;

/**
 * Recibe las funciones del controlador principal.
 */
export function setAdminHandlers(handlers = {}) {
    adminHandlers = {
        ...adminHandlers,
        ...handlers
    };
}

/*
|--------------------------------------------------------------------------
| Funciones auxiliares
|--------------------------------------------------------------------------
*/

/**
 * Clona una plantilla HTML.
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
 * Obtiene los elementos de una respuesta paginada o de una lista.
 */
function getResponseItems(response) {

    if (Array.isArray(response)) {
        return response;
    }

    return response?.content || [];
}

/**
 * Notifica errores de operaciones.
 */
function handleOperationError(error) {

    if (typeof adminHandlers.onOperationError === "function") {
        adminHandlers.onOperationError(error);
    } else {
        notifyError(error);
    }
}

/**
 * Notifica errores de formularios.
 */
function handleFormError(form, error) {

    if (typeof adminHandlers.onFormError === "function") {
        adminHandlers.onFormError(form, error);
    } else {
        handleOperationError(error);
    }
}

/**
 * Notifica que los productos cambiaron.
 */
function notifyProductsChanged() {

    if (typeof adminHandlers.onProductsChanged === "function") {
        adminHandlers.onProductsChanged();
    }
}

/**
 * Convierte un valor a número o null.
 */
function numberOrNull(value) {

    if (value === "" || value === null || value === undefined) {
        return null;
    }

    const number = Number(value);

    return Number.isNaN(number) ? null : number;
}

/*
|--------------------------------------------------------------------------
| Apertura del panel de administración
|--------------------------------------------------------------------------
*/

/**
 * Abre el panel principal de administración.
 */
export async function openAdmin() {

    const body = openModal({
        title: "Administración",
        templateId: "admin-template",
        eyebrow: "GESTIÓN DE LA TIENDA",
        wide: true
    });

    if (!body) return;

    currentAdminTab = "products";
    productsPage = 0;

    const tabs = body.querySelectorAll("[data-admin-tab]");

    tabs.forEach((tab) => {

        tab.addEventListener("click", async () => {

            const selectedTab = tab.dataset.adminTab;

            currentAdminTab = selectedTab;

            updateAdminTabs(body, selectedTab);

            if (selectedTab === "products") {
                await renderProductsPanel(body);
            }

            if (selectedTab === "categories") {
                await renderCategoriesPanel(body);
            }
        });
    });

    updateAdminTabs(body, currentAdminTab);

    await renderProductsPanel(body);
}

/**
 * Actualiza visualmente la pestaña seleccionada.
 */
function updateAdminTabs(body, selectedTab) {

    body.querySelectorAll("[data-admin-tab]").forEach((tab) => {

        const active = tab.dataset.adminTab === selectedTab;

        tab.classList.toggle("is-active", active);

        tab.setAttribute("aria-selected", String(active));
    });
}

/*
|--------------------------------------------------------------------------
| Administración de productos
|--------------------------------------------------------------------------
*/

/**
 * Carga y muestra el panel de productos.
 */
async function renderProductsPanel(body, page = productsPage) {

    const container = body.querySelector("#admin-content");

    if (!container) return;

    container.innerHTML = "<p>Cargando productos...</p>";

    try {

        const [productsResponse, categoriesResponse] = await Promise.all([
            apiJson(`/admin/productos/listar?page=${page}&size=${ADMIN_PAGE_SIZE}`),
            apiJson("/categorias/listar")
        ]);

        productsCache = getResponseItems(productsResponse);
        categoriesCache = getResponseItems(categoriesResponse);

        const pages = Number(productsResponse.totalPages) || 0;

        if (productsCache.length === 0 && pages > 0 && page > pages - 1) {
            return renderProductsPanel(body, pages - 1);
        }

        productsPage = Number(productsResponse.number) || 0;
        productsTotalPages = pages;
        productsTotalElements = Number(productsResponse.totalElements) || 0;

        const fragment = cloneTemplate("admin-products-template");

        if (!fragment) return;

        container.replaceChildren(fragment);

        const panel = container.querySelector("#product-form");

        if (!panel) {
            console.error("No se encontró #product-form.");
            return;
        }

        populateCategorySelect(panel);

        renderProductRows(container);
        renderProductsPagination(container, body);

        panel.addEventListener("submit", async (event) => {
            event.preventDefault();

            await saveProduct(panel, body);
        });

        panel.querySelector("[data-reset-product]")?.addEventListener(
            "click",
            () => resetProductForm(panel)
        );

    } catch (error) {

        console.error("Error al cargar la administración de productos:", error);

        handleOperationError(error);

        container.innerHTML = `
            <div class="admin-error">
                <p>No fue posible cargar los productos.</p>

                <button
                    type="button"
                    class="btn btn--secondary"
                    data-retry-admin-products
                >
                    Reintentar
                </button>
            </div>
        `;

        container.querySelector("[data-retry-admin-products]")?.addEventListener(
            "click",
            () => renderProductsPanel(body, productsPage)
        );
    }
}

/**
 * Llena el select de categorías del formulario de productos.
 */
function populateCategorySelect(form) {

    const select = form.elements.categoriaId;

    if (!select) return;

    select.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = "Selecciona una categoría";

    select.appendChild(defaultOption);

    categoriesCache.forEach((category) => {

        const option = document.createElement("option");

        option.value = category.id;
        option.textContent = category.nombre;

        select.appendChild(option);
    });
}

/**
 * Renderiza las filas de productos.
 */
function renderProductRows(container) {

    const list = container.querySelector("#admin-products-list");

    const count = container.querySelector("[data-products-count]");

    if (!list) return;

    list.replaceChildren();

    if (count) {
        count.textContent = String(productsTotalElements);
    }

    if (productsCache.length === 0) {

        const empty = document.createElement("tr");

        empty.innerHTML = `
            <td colspan="5" class="admin-empty">
                No hay productos registrados.
            </td>
        `;

        list.appendChild(empty);

        return;
    }

    productsCache.forEach((product) => {

        const row = createProductRow(product);

        if (row) {
            list.appendChild(row);
        }
    });
}

/**
 * Pinta los controles de paginación de productos.
 */
function renderProductsPagination(container, body) {

    const pagination = container.querySelector("#admin-products-pagination");

    if (!pagination) return;

    renderPageControls(pagination, {
        currentPage: productsPage,
        totalPages: productsTotalPages,
        onPageChange: (page) => renderProductsPanel(body, page)
    });
}

/**
 * Construye una fila de producto.
 */
function createProductRow(product) {

    const fragment = cloneTemplate("admin-product-row-template");

    if (!fragment) return null;

    const row = fragment.firstElementChild;

    if (!row) return null;

    const categoryName = typeof product.categoria === "object"
        ? product.categoria?.nombre
        : product.categoria;

    const productActive = product.activo !== false;

    const values = {
        "[data-product-name]": product.nombre || "Sin nombre",
        "[data-product-category]": categoryName || "Sin categoría",
        "[data-product-price]": formatMoney(product.precio),
        "[data-product-stock]": String(product.stock ?? 0),
        "[data-product-status]": productActive ? "Activo" : "Inactivo"
    };

    Object.entries(values).forEach(([selector, value]) => {

        const element = row.querySelector(selector);

        if (element) {
            element.textContent = value;
        }
    });

    const statusElement = row.querySelector("[data-product-status]");

    statusElement?.classList.toggle("is-active", productActive);
    statusElement?.classList.toggle("is-inactive", !productActive);

    const editButton = row.querySelector("[data-edit-product]");
    const deleteButton = row.querySelector("[data-delete-product]");

    editButton?.addEventListener("click", () => {
        editProduct(product);
    });

    deleteButton?.addEventListener("click", () => {
        deleteProduct(product);
    });

    return row;
}

/**
 * Prepara el formulario para editar un producto.
 */
function editProduct(product) {

    const body = document.getElementById("modal-body");

    const form = body?.querySelector("#product-form");

    if (!form) return;

    form.elements.id.value = product.id;
    form.elements.nombre.value = product.nombre || "";
    form.elements.descripcion.value = product.descripcion || "";
    form.elements.precio.value = product.precio ?? "";
    form.elements.stock.value = product.stock ?? "";

    const categoryName = typeof product.categoria === "object"
        ? product.categoria?.nombre
        : product.categoria;

    const category = categoriesCache.find(
        (item) => item.nombre === categoryName
    );

    form.elements.categoriaId.value = category?.id
        ?? product.categoriaId
        ?? "";

    form.elements.activo.checked = product.activo !== false;

    const submitButton = form.querySelector('[type="submit"]');

    if (submitButton) {
        submitButton.textContent = "Actualizar producto";
    }

    form.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

/**
 * Limpia el formulario de productos.
 */
function resetProductForm(form) {

    form.reset();

    form.elements.id.value = "";

    const submitButton = form.querySelector('[type="submit"]');

    if (submitButton) {
        submitButton.textContent = "Guardar producto";
    }
}

/**
 * Crea o actualiza un producto.
 */
async function saveProduct(form, modalBody) {

    const submitButton = form.querySelector('[type="submit"]');

    const id = form.elements.id.value;

    const payload = {
        nombre: form.elements.nombre.value.trim(),
        descripcion: form.elements.descripcion.value.trim(),
        precio: numberOrNull(form.elements.precio.value),
        stock: numberOrNull(form.elements.stock.value),
        categoriaId: numberOrNull(form.elements.categoriaId.value),
        activo: Boolean(form.elements.activo.checked)
    };

    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Guardando...";
    }

    try {

        if (id) {

            await apiJson(`/admin/productos/actualizar/${id}`, {
                method: "PUT",
                body: payload
            });

            toast("Producto actualizado correctamente.", "success");

        } else {

            await apiJson("/admin/productos/crear", {
                method: "POST",
                body: payload
            });

            toast("Producto creado correctamente.", "success");
        }

        await renderProductsPanel(modalBody, id ? productsPage : 0);

        notifyProductsChanged();

    } catch (error) {

        console.error("Error al guardar el producto:", error);

        handleFormError(form, error);

    } finally {

        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = id
                ? "Actualizar producto"
                : "Guardar producto";
        }
    }
}

/**
 * Elimina un producto.
 */
async function deleteProduct(product) {

    const confirmed = await confirmAction({
        title: "Desactivar producto",
        message: `¿Desactivar el producto "${product.nombre}"? Ya no aparecerá en el catálogo.`,
        confirmText: "Desactivar",
        cancelText: "Cancelar"
    });

    if (!confirmed) return;

    try {

        await apiJson(`/admin/productos/eliminar/${product.id}`, {
            method: "DELETE"
        });

        toast("Producto desactivado correctamente.", "success");

        const body = document.getElementById("modal-body");

        await renderProductsPanel(body, productsPage);

        notifyProductsChanged();

    } catch (error) {

        console.error("Error al eliminar el producto:", error);

        handleOperationError(error);
    }
}

/*
|--------------------------------------------------------------------------
| Administración de categorías
|--------------------------------------------------------------------------
*/

/**
 * Carga y muestra el panel de categorías.
 */
async function renderCategoriesPanel(body) {

    const container = body.querySelector("#admin-content");

    if (!container) return;

    container.innerHTML = "<p>Cargando categorías...</p>";

    try {

        const response = await apiJson("/categorias/listar");

        categoriesCache = getResponseItems(response);

        const fragment = cloneTemplate("admin-categories-template");

        if (!fragment) return;

        container.replaceChildren(fragment);

        const form = container.querySelector("#category-form");

        if (!form) {
            console.error("No se encontró #category-form.");
            return;
        }

        renderCategoryRows(container);

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            await saveCategory(form, body);
        });

        form.querySelector("[data-reset-category]")?.addEventListener(
            "click",
            () => resetCategoryForm(form)
        );

    } catch (error) {

        console.error("Error al cargar las categorías:", error);

        handleOperationError(error);

        container.innerHTML = `
            <div class="admin-error">
                <p>No fue posible cargar las categorías.</p>

                <button
                    type="button"
                    class="btn btn--secondary"
                    data-retry-admin-categories
                >
                    Reintentar
                </button>
            </div>
        `;

        container.querySelector("[data-retry-admin-categories]")?.addEventListener(
            "click",
            () => renderCategoriesPanel(body)
        );
    }
}

/**
 * Renderiza las categorías registradas.
 */
function renderCategoryRows(container) {

    const list = container.querySelector("#admin-categories-list");

    const count = container.querySelector("[data-categories-count]");

    if (!list) return;

    list.replaceChildren();

    if (count) {
        count.textContent = String(categoriesCache.length);
    }

    if (categoriesCache.length === 0) {

        const empty = document.createElement("p");

        empty.className = "admin-empty";
        empty.textContent = "No hay categorías registradas.";

        list.appendChild(empty);

        return;
    }

    categoriesCache.forEach((category) => {

        const fragment = cloneTemplate("admin-category-item-template");

        if (!fragment) return;

        const element = fragment.firstElementChild;

        if (!element) return;

        const name = element.querySelector("[data-category-name]");

        if (name) {
            name.textContent = category.nombre || "Sin nombre";
        }

        element.querySelector("[data-edit-category]")?.addEventListener(
            "click",
            () => editCategory(category)
        );

        element.querySelector("[data-delete-category]")?.addEventListener(
            "click",
            () => deleteCategory(category)
        );

        list.appendChild(fragment);
    });
}

/**
 * Prepara el formulario para editar una categoría.
 */
function editCategory(category) {

    const body = document.getElementById("modal-body");

    const form = body?.querySelector("#category-form");

    if (!form) return;

    form.elements.id.value = category.id;
    form.elements.nombre.value = category.nombre || "";

    const submitButton = form.querySelector('[type="submit"]');

    if (submitButton) {
        submitButton.textContent = "Actualizar categoría";
    }

    form.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

/**
 * Limpia el formulario de categorías.
 */
function resetCategoryForm(form) {

    form.reset();

    form.elements.id.value = "";

    const submitButton = form.querySelector('[type="submit"]');

    if (submitButton) {
        submitButton.textContent = "Guardar categoría";
    }
}

/**
 * Crea o actualiza una categoría.
 */
async function saveCategory(form, modalBody) {

    const submitButton = form.querySelector('[type="submit"]');

    const id = form.elements.id.value;

    const payload = {
        nombre: form.elements.nombre.value.trim()
    };

    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Guardando...";
    }

    try {

        if (id) {

            await apiJson(`/categorias/actualizar/${id}`, {
                method: "PUT",
                body: payload
            });

            toast("Categoría actualizada correctamente.", "success");

        } else {

            await apiJson("/categorias/crear", {
                method: "POST",
                body: payload
            });

            toast("Categoría creada correctamente.", "success");
        }

        await renderCategoriesPanel(modalBody);

        notifyProductsChanged();

    } catch (error) {

        console.error("Error al guardar la categoría:", error);

        handleFormError(form, error);

    } finally {

        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = id
                ? "Actualizar categoría"
                : "Guardar categoría";
        }
    }
}

/**
 * Elimina una categoría.
 */
async function deleteCategory(category) {

    const confirmed = await confirmAction({
        title: "Eliminar categoría",
        message: `¿Eliminar la categoría "${category.nombre}"?`,
        confirmText: "Eliminar",
        cancelText: "Cancelar"
    });

    if (!confirmed) return;

    try {

        await apiJson(`/categorias/eliminar/${category.id}`, {
            method: "DELETE"
        });

        toast("Categoría eliminada correctamente.", "success");

        const body = document.getElementById("modal-body");

        await renderCategoriesPanel(body);

        notifyProductsChanged();

    } catch (error) {

        console.error("Error al eliminar la categoría:", error);

        handleOperationError(error);
    }
}