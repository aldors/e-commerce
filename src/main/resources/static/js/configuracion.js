document.addEventListener("DOMContentLoaded", () => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    // Contenedor de mensajes
    const contenedorMensajes = document.createElement("div");
    contenedorMensajes.id = "mensajes_config";
    document.body.insertBefore(contenedorMensajes, document.body.firstChild);

    function mostrarMensaje(texto, tipo = "error") {
        contenedorMensajes.innerHTML = `<p style="color:${tipo === "success" ? "green" : "red"};">${texto}</p>`;
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
                localStorage.setItem("accessToken", resultado.data.accessToken);
                return resultado.data.accessToken;
            } else {
                mostrarMensaje("Sesión expirada, inicia sesión de nuevo");
                return null;
            }
        } catch {
            mostrarMensaje("Error de conexión al refrescar token");
            return null;
        }
    }

    async function fetchConToken(url, options) {
        let token = localStorage.getItem("accessToken");
        options.headers = {
            ...options.headers,
            "Authorization": `Bearer ${token}`
        };
        let respuesta = await fetch(url, options);
        if (respuesta.status === 401) {
            token = await refrescarToken();
            if (token) {
                options.headers["Authorization"] = `Bearer ${token}`;
                respuesta = await fetch(url, options);
            }
        }
        return respuesta;
    }

    // =======================
    // Cambiar nombre
    // =======================
    document.getElementById("form-usuario").addEventListener("submit", async e => {
        e.preventDefault();
        const nuevoNombre = e.target.querySelector("input").value;

        try {
            const respuesta = await fetchConToken("http://localhost:8080/auth/actualizar-nombre", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ nombre: nuevoNombre })
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                mostrarMensaje("Nombre actualizado correctamente", "success");
                localStorage.clear();
                setTimeout(() => window.location.href = "../html/login.html", 2000);
            } else {
                if (resultado.errors && Array.isArray(resultado.errors)) {
                    // Si tu backend devuelve un array de errores
                    mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
                } else {
                    // Si solo devuelve un mensaje (como tu excepción de stock insuficiente)
                    mostrarMensaje(resultado.message || "Error al actualizar nombre");
                }
                
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al actualizar nombre");
            console.error(error);
        }
    });

    // =======================
    // Cambiar correo
    // =======================
    document.getElementById("form-correo").addEventListener("submit", async e => {
        e.preventDefault();
        const nuevoCorreo = e.target.querySelector("input").value;

        try {
            const respuesta = await fetchConToken("http://localhost:8080/auth/actualizar-email", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: nuevoCorreo })
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                mostrarMensaje("Correo actualizado correctamente. Debes volver a iniciar sesión.", "success");
                // 🔽 Logout automático para refrescar JWT
                localStorage.clear();
                setTimeout(() => window.location.href = "../html/login.html", 2000);
            } else {

                if(resultado.errors && Array.isArray(resultado.errors)){
                    mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
                } else {
                    mostrarMensaje(resultado.message || "Error al actualizar correo");
                }
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al actualizar correo");
            console.error(error);
        }
    });

    // =======================
    // Cambiar contraseña
    // =======================
    document.getElementById("form-password").addEventListener("submit", async e => {
        e.preventDefault();
        const inputs = e.target.querySelectorAll("input");
        const passwordActual = inputs[0].value;
        const passwordNueva = inputs[1].value;

        try {
            const respuesta = await fetchConToken("http://localhost:8080/auth/actualizar-contraseña", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    passwordActual,
                    passwordNueva
                })
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                mostrarMensaje("Contraseña actualizada correctamente. Debes volver a iniciar sesión.", "success");
                // 🔽 Logout automático para refrescar JWT
                localStorage.clear();
                setTimeout(() => window.location.href = "../html/login.html", 2000);
            } else {

                if(resultado.errors && Array.isArray(resultado.errors)){
                    mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
                } else {
                    mostrarMensaje(resultado.message || "Error al actualizar contraseña");
                }
                
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al actualizar contraseña");
            console.error(error);
        }
    });
});
