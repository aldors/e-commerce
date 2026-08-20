document.addEventListener("DOMContentLoaded", () => {
    const contenedorMensajes = document.createElement("div");
    contenedorMensajes.id = "mensajes_carrito";
    document.body.insertBefore(contenedorMensajes, document.body.firstChild);

    function mostrarMensaje(texto, tipo = "error") {
        contenedorMensajes.innerHTML = `<p style="color:${tipo === "success" ? "green" : "red"};">${texto}</p>`;
    }

    let accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    async function refrescarToken() {
        try {
            const respuesta = await fetch("http://localhost:8080/auth/refresh-token", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken })
            });
            if (respuesta.ok) {
                const resultado = await respuesta.json();
                accessToken = resultado.data.accessToken;
                localStorage.setItem("accessToken", accessToken);
                return true;
            } else {
                mostrarMensaje("Sesión expirada, inicia sesión de nuevo");
                return false;
            }
        } catch {
            mostrarMensaje("Error de conexión al refrescar token");
            return false;
        }
    }

    async function fetchConToken(url, options) {
        options.headers = {
            ...options.headers,
            "Authorization": `Bearer ${accessToken}`
        };
        let respuesta = await fetch(url, options);
        if (respuesta.status === 401) {
            const ok = await refrescarToken();
            if (ok) {
                options.headers["Authorization"] = `Bearer ${accessToken}`;
                respuesta = await fetch(url, options);
            }
        }
        return respuesta;
    }

    // =======================
    // Obtener carrito
    // =======================
    async function obtenerCarrito() {
        try {
            const respuesta = await fetchConToken("http://localhost:8080/carrito", { method: "GET" });
            const resultado = await respuesta.json();

            const itemsContainer = document.querySelector(".carrito-items");
            const totalContainer = document.querySelector(".carrito-total h3");

            itemsContainer.innerHTML = "";

            if (respuesta.ok && resultado.success) {
                resultado.data.items.forEach(item => {
                    const tarjeta = document.createElement("div");
                    tarjeta.classList.add("item-tarjeta");
                    tarjeta.innerHTML = `
                        <div class="item-info">
                            <h3>${item.nombre}</h3>
                            <p><strong>Cantidad:</strong> ${item.cantidad}</p>
                            <p><strong>Precio:</strong> $${item.precio}</p>
                        </div>
                        <button class="btn-eliminar" data-id="${item.productoId}">Eliminar</button>
                    `;
                    itemsContainer.appendChild(tarjeta);
                });

                totalContainer.textContent = `Total: $${resultado.data.total}`;
            } else {
                mostrarMensaje(resultado.message || "Error al obtener carrito");
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al obtener carrito");
            console.error(error);
        }
    }

    // =======================
    // Eliminar producto del carrito
    // =======================
    document.querySelector(".carrito-items").addEventListener("click", async (e) => {
        if (e.target.classList.contains("btn-eliminar")) {
            const productoId = e.target.dataset.id;
            try {
                const respuesta = await fetchConToken(`http://localhost:8080/carrito/${productoId}`, {
                    method: "DELETE"
                });

                if (respuesta.status === 204) {
                    mostrarMensaje("Producto eliminado del carrito", "success");
                    obtenerCarrito(); // refrescar vista
                } else {
                    const resultado = await respuesta.json();
                    mostrarMensaje(resultado.message || "Error al eliminar producto");
                }
            } catch (error) {
                mostrarMensaje("Error de conexión al eliminar producto");
                console.error(error);
            }
        }
    });

    // =======================
    // Checkout
    // =======================
    document.querySelector(".btn-checkout").addEventListener("click", async () => {
        try {
            const respuesta = await fetchConToken("http://localhost:8080/ordenes/checkout", {
                method: "POST"
            });

            const resultado = await respuesta.json();

            if (respuesta.ok && resultado.success) {
                mostrarMensaje("Checkout realizado correctamente", "success");

                // 🔽 Aquí puedes refrescar el carrito para que se vea vacío después del checkout
                obtenerCarrito();

                // Opcional: mostrar detalles de la orden creada
                console.log("Orden creada:", resultado.data);
            } else {
                if (resultado.errors && Array.isArray(resultado.errors)) {
                    mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
                } else {
                    mostrarMensaje(resultado.message || "Error al realizar checkout");
                }
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al realizar checkout");
            console.error(error);
        }
    });


    // Inicializar
    obtenerCarrito();
});
