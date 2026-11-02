const backdrop = document.getElementById("modal-backdrop");
const modal = document.getElementById("app-modal");
const modalTitle = document.getElementById("modal-title");
const modalEyebrow = document.getElementById("modal-eyebrow");
const modalBody = document.getElementById("modal-body");
const closeButton = document.getElementById("modal-close");

let lastFocus = null;
let modalOpen = false;

/**
 * Abre el modal e inserta el contenido de una plantilla HTML.
 *
 * @param {Object} options
 * @param {string} options.title - Título del modal.
 * @param {string} options.templateId - ID de la plantilla HTML.
 * @param {string} options.eyebrow - Texto pequeño sobre el título.
 * @param {boolean} options.wide - Indica si el modal debe ser más ancho.
 * @param {string} options.className - Clase CSS adicional.
 */
export function openModal({
    title = "",
    templateId,
    eyebrow = "",
    wide = false,
    className = ""
} = {}) {

    const template = document.getElementById(templateId);

    if (!template || !(template instanceof HTMLTemplateElement)) {
        console.error(`No se encontró la plantilla HTML: ${templateId}`);
        return null;
    }

    if (!backdrop || !modal || !modalBody || !modalTitle) {
        console.error("No se encontró la estructura principal del modal en el HTML.");
        return null;
    }

    if (!modalOpen) {
        lastFocus = document.activeElement;
    }

    modalTitle.textContent = title;
    modalEyebrow.textContent = eyebrow;

    modalEyebrow.hidden = !eyebrow;

    modal.className = "modal";

    if (wide) {
        modal.classList.add("modal--wide");
    }

    if (className) {
        modal.classList.add(...className.split(" ").filter(Boolean));
    }

    modalBody.replaceChildren(
        template.content.cloneNode(true)
    );

    modalBody.scrollTop = 0;

    backdrop.hidden = false;
    document.body.classList.add("is-modal-open");

    modalOpen = true;

    requestAnimationFrame(() => {
        modal.focus();
    });

    return modalBody;
}

/**
 * Cierra el modal y limpia su contenido.
 */
export function closeModal() {

    if (!modalOpen || !backdrop || !modalBody) {
        return;
    }

    backdrop.hidden = true;

    modalBody.replaceChildren();

    modalTitle.textContent = "";
    modalEyebrow.textContent = "";
    modalEyebrow.hidden = true;

    modal.className = "modal";

    modalBody.scrollTop = 0;

    document.body.classList.remove("is-modal-open");

    modalOpen = false;

    if (lastFocus instanceof HTMLElement && lastFocus.isConnected) {
        lastFocus.focus();
    }

    lastFocus = null;
}

/**
 * Devuelve el elemento principal del modal.
 */
export function getModal() {
    return modal;
}

/**
 * Devuelve el contenedor donde se inserta el contenido.
 */
export function getModalBody() {
    return modalBody;
}

/**
 * Indica si el modal está abierto.
 */
export function isModalOpen() {
    return modalOpen;
}

/* Cerrar al pulsar el botón X */
closeButton?.addEventListener("click", closeModal);

/* Cerrar al pulsar fuera del contenido */
backdrop?.addEventListener("click", (event) => {
    if (event.target === backdrop) {
        closeModal();
    }
});

/* Cerrar con Escape y mantener el foco dentro del modal */
document.addEventListener("keydown", (event) => {

    if (!modalOpen) return;

    if (event.key === "Escape") {
        closeModal();
        return;
    }

    if (event.key !== "Tab") return;

    const focusable = Array.from(
        modal.querySelectorAll(
            'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
        )
    ).filter((element) => element.offsetParent !== null);

    if (focusable.length === 0) {
        event.preventDefault();
        modal.focus();
        return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (!modal.contains(document.activeElement)) {
        event.preventDefault();
        first.focus();
        return;
    }

    if (event.shiftKey && (document.activeElement === first || document.activeElement === modal)) {
        event.preventDefault();
        last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
    }
});