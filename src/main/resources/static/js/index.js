document.addEventListener("DOMContentLoaded", () => {

    // 1. Contenedor de mensajes
    const contenedorMensajes = document.createElement("div");
    contenedorMensajes.id = "mensajes_index";
    document.body.insertBefore(contenedorMensajes, document.body.firstChild);

    // 2. Función para mostrar mensajes
    function mostrarMensaje(texto, tipo = "error") {
        contenedorMensajes.innerHTML = `<p style="color:${tipo === "success" ? "green" : "red"};">${texto}</p>`;
    }

    const contenedor = document.getElementById("productos-contenedor");

    let accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    async function mostrarNombreDinamico(){

        // Para que en el inicio aparezca Biendenido, {usuario} dinamicamente
        const mensajeBienvenida = document.getElementById("mensaje-bienvenida");

        if(accessToken){
            // Decodificar JWT
            const payloadBase64 = accessToken.split(".")[1];
            const payloadJson = atob(payloadBase64);
            const payload = JSON.parse(payloadJson);

            // Aquí depende de cómo tu backend lo guarde
            const nombreUsuario = payload.nombre || payload.sub || "Usuario";

            const roles = (payload.roles || []).map(r => r.authority);

                if (roles.includes("ROLE_ADMIN")) {
                    const mensajePersonalizado = "administrador hijo de puta"
                    mensajeBienvenida.textContent = `Bienvenido(a), ${mensajePersonalizado}`;
                } else{
                    // Actualizar mensaje
                    mensajeBienvenida.textContent = `Bienvenido(a), ${nombreUsuario}`;
                }

        } else {
            mensajeBienvenida.textContent = "Bienvenido";
        }
    }

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

    async function listarProductos() {
        try {
            const respuesta = await fetchConToken("http://localhost:8080/productos/listar", { method: "GET" });
            const resultado = await respuesta.json();

            contenedor.innerHTML = "";

            if (respuesta.ok && resultado.success) {
                resultado.data.content.forEach(prod => {
                    // Crear tarjeta
                    const tarjeta = document.createElement("div");
                    tarjeta.classList.add("tarjeta", "tarjeta-producto");

                    tarjeta.innerHTML = `
                        <h3>${prod.nombre}</h3>
                        <p><strong>Descripción:</strong> ${prod.descripcion}</p>
                        <p><strong>Stock:</strong> ${prod.stock}</p>
                        <p><strong>Precio:</strong> $${prod.precio}</p>
                        <button class="btn-agregar" data-id="${prod.id}">Agregar al Carrito</button>

                        <div class="cantidad-control">
                          <button class="btn-menos">-</button>
                          <span class="cantidad">1</span>
                          <button class="btn-mas">+</button>
                        </div>
                    `;

                    contenedor.appendChild(tarjeta);
                });
            } else {
                mostrarMensaje(resultado.message || "Error al listar productos");
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al listar productos");
            console.error(error);
        }
    }

    // =======================
    // Manejo de cantidad y agregar al carrito
    // =======================
    contenedor.addEventListener("click", async (e) => {
        const tarjeta = e.target.closest(".tarjeta-producto");
        if (!tarjeta) return;

        const cantidadSpan = tarjeta.querySelector(".cantidad");

        if (e.target.classList.contains("btn-mas")) {
            cantidadSpan.textContent = parseInt(cantidadSpan.textContent) + 1;
        }

        if (e.target.classList.contains("btn-menos")) {
            const actual = parseInt(cantidadSpan.textContent);
            if (actual > 1) cantidadSpan.textContent = actual - 1;
        }

        if (e.target.classList.contains("btn-agregar")) {
            const productoId = e.target.dataset.id;
            const cantidad = parseInt(cantidadSpan.textContent);

            try {
                const respuesta = await fetchConToken("http://localhost:8080/carrito", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ productoId, cantidad })
                });

                const resultado = await respuesta.json();

                if (respuesta.ok && resultado.success) {
                    mostrarMensaje("Producto agregado al carrito correctamente", "success");
                    // 🔽 Aquí es donde refrescarías el carrito si quieres que se actualice en tiempo real
                    // window.location.href = "carrito.html"; // opcional: redirigir al carrito
                } else {
                    if (resultado.errors && Array.isArray(resultado.errors)) {
                        // Si tu backend devuelve un array de errores
                        mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
                    } else {
                        // Si solo devuelve un mensaje (como tu excepción de stock insuficiente)
                        mostrarMensaje(resultado.message || "Error al agregar producto");
                    }
                }
            } catch (error) {
                mostrarMensaje("Error de conexión al agregar producto");
                console.error(error);
            }
        }
    });


    mostrarNombreDinamico();
    listarProductos();
});
