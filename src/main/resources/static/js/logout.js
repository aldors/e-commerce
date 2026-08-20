document.addEventListener("DOMContentLoaded", () => {
    // --- LOGOUT ---
    const botonLogout = document.getElementById("logout");
    const refreshTokenValue = localStorage.getItem("refreshToken");

    const contenedorMensajes = document.createElement("div");
    contenedorMensajes.id = "mensaje-logout";
    document.body.appendChild(contenedorMensajes);

    botonLogout.addEventListener("click", async () => {
        const refreshTokenParaLogout = { refreshToken: refreshTokenValue };

        try {
            const respuesta = await fetch("http://localhost:8080/auth/logout", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(refreshTokenParaLogout)
            });

            contenedorMensajes.innerHTML = "";

            if (respuesta.ok) {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");

                contenedorMensajes.innerHTML = `<p style="color:green;">Sesión cerrada correctamente</p>`;
                setTimeout(() => window.location.href = "login.html", 1500);
            } else {
                const resultado = await respuesta.json();
                contenedorMensajes.innerHTML = `<p style="color:red;">${resultado.message || "Error desconocido"}</p>`;
            }
        } catch (error) {
            contenedorMensajes.innerHTML = `<p style="color:red;">Error de conexión con el servidor</p>`;
            console.error("Error en la petición:", error);
        }
    });

    // --- PANEL ADMIN ---
    const accessToken = localStorage.getItem("accessToken");
    if (accessToken) {
        try {
            const payloadBase64 = accessToken.split(".")[1];
            const payloadJson = JSON.parse(atob(payloadBase64));

            console.log("Payload JWT:", payloadJson);
            
            const roles = (payloadJson.roles || []).map(r => r.authority);

            if (roles.includes("ROLE_ADMIN")) {
                const adminLink = document.createElement("a");
                adminLink.href = "panel-admin-agregar-productos.html";
                adminLink.textContent = "Panel Admin";
                adminLink.classList.add("admin-link");

                document.getElementById("admin-contenedor").appendChild(adminLink);
            }
        } catch (error) {
            console.error("Error al decodificar el token:", error);
        }
    }
});
