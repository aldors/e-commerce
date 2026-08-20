document.addEventListener("DOMContentLoaded", () => {

    // Contenedor de mensajes
    const contenedorMensajes = document.createElement("div");
    contenedorMensajes.id = "mensajes_historial";
    document.body.insertBefore(contenedorMensajes, document.body.firstChild);

    function mostrarMensaje(texto, tipo = "error") {
        contenedorMensajes.innerHTML = `<p style="color:${tipo === "success" ? "green" : "red"};">${texto}</p>`;
    }

    const contenedor = document.getElementById("historial-contenedor");

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

    async function obtenerHistorial() {
        try {
            const respuesta = await fetchConToken("http://localhost:8080/ordenes", { method: "GET" });
            const resultado = await respuesta.json();

            contenedor.innerHTML = `<h2>📜 Historial de Compras</h2>`;

            if (respuesta.ok && resultado.success) {
                resultado.data.forEach(orden => {
                    const ordenCard = document.createElement("div");
                    ordenCard.classList.add("orden-tarjeta");

                    // Fecha formateada
                    const fecha = new Date(orden.fecha).toLocaleString("es-MX");

                    ordenCard.innerHTML = `
                        <div class="orden-info">
                            <p><strong>Total:</strong> $${orden.total}</p>
                            <p><strong>Estado:</strong> ${orden.estado}</p>
                            <p><strong>Fecha:</strong> ${fecha}</p>
                        </div>
                        <div class="orden-productos">
                            <h3>Productos:</h3>
                            <ul>
                                ${orden.detalles.map(det => `
                                    <li>
                                        <span class="producto-nombre">${det.producto}</span>
                                        <span class="producto-detalle">Cantidad: ${det.cantidad} | Precio: $${det.precioUnitario}</span>
                                    </li>
                                `).join("")}
                            </ul>
                        </div>
                    `;

                    contenedor.appendChild(ordenCard);
                });
            } else {
                mostrarMensaje(resultado.message || "Error al obtener historial");
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al obtener historial");
            console.error(error);
        }
    }

    obtenerHistorial();
});
