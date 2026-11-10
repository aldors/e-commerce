function buildPageItems(currentPage, totalPages) {
    const pages = new Set([
        1,
        totalPages,
        currentPage,
        currentPage - 1,
        currentPage + 1
    ]);

    if (currentPage <= 3) {
        pages.add(2);
        pages.add(3);
    }

    if (currentPage >= totalPages - 2) {
        pages.add(totalPages - 1);
        pages.add(totalPages - 2);
    }

    const sorted = [...pages]
        .filter((page) => page >= 1 && page <= totalPages)
        .sort((a, b) => a - b);

    const items = [];
    let previous = 0;

    sorted.forEach((page) => {
        if (page - previous > 1) {
            items.push("ellipsis");
        }

        items.push(page);
        previous = page;
    });

    return items;
}

function createNavigationButton(label, disabled, onClick) {
    const button = document.createElement("button");

    button.type = "button";
    button.className = "btn btn--secondary pagination__nav";
    button.textContent = label;
    button.disabled = disabled;
    button.addEventListener("click", onClick);

    return button;
}

export function renderPageControls(container, { currentPage, totalPages, onPageChange }) {
    if (!container) return;

    container.replaceChildren();

    if (!Number.isFinite(totalPages) || totalPages <= 1) {
        container.hidden = true;
        return;
    }

    container.hidden = false;

    const current = Math.min(
        Math.max(Number(currentPage) || 0, 0),
        totalPages - 1
    );

    const previousButton = createNavigationButton(
        "Anterior",
        current <= 0,
        () => onPageChange(current - 1)
    );

    container.appendChild(previousButton);

    buildPageItems(current + 1, totalPages).forEach((item) => {
        if (item === "ellipsis") {
            const ellipsis = document.createElement("span");

            ellipsis.className = "pagination__ellipsis";
            ellipsis.setAttribute("aria-hidden", "true");
            ellipsis.textContent = "…";

            container.appendChild(ellipsis);
            return;
        }

        const pageNumber = item;
        const button = document.createElement("button");

        button.type = "button";
        button.className = "pagination__page";
        button.textContent = String(pageNumber);
        button.setAttribute("aria-label", `Página ${pageNumber}`);

        if (pageNumber === current + 1) {
            button.classList.add("is-active");
            button.setAttribute("aria-current", "page");
        }

        button.addEventListener("click", () => {
            if (pageNumber !== current + 1) {
                onPageChange(pageNumber - 1);
            }
        });

        container.appendChild(button);
    });

    const nextButton = createNavigationButton(
        "Siguiente",
        current >= totalPages - 1,
        () => onPageChange(current + 1)
    );

    container.appendChild(nextButton);
}
