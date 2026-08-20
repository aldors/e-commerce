// login.js

// 1. Creamos un contenedor para mostrar mensajes
const contenedorMensajes = document.createElement("div");
contenedorMensajes.id = "mensajes_login";
document.querySelector(".login-contenedor").appendChild(contenedorMensajes);

// 2. Seleccionamos el botón de login
const botonLogin = document.getElementById("login-button");

// 3. Escuchamos el evento click en el botón
botonLogin.addEventListener("click", async () => {
    // 4. Obtenemos los valores de los inputs
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;

    // 5. Creamos el objeto con los datos que espera el backend (LoginRequest)
    const datosLogin = { email, password };

    try {
        // 6. Enviamos la petición POST al backend
        const respuesta = await fetch("http://localhost:8080/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datosLogin)
        });

        // 7. Convertimos la respuesta a JSON
        const resultado = await respuesta.json();

        // 8. Limpiamos mensajes previos
        contenedorMensajes.innerHTML = "";

        // 9. Verificamos si el login fue exitoso
        if (respuesta.ok && resultado.success) {
            // 10. Extraemos los tokens del AuthResponse
            const { accessToken, refreshToken } = resultado.data;

            // 11. Guardamos los tokens en localStorage
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);

            // 12. Mostramos mensaje de éxito
            contenedorMensajes.innerHTML = `<p style="color:green;">${resultado.message}</p>`;

            // 13. Redirigimos al panel admin
            //setTimeout(() => window.location.href = "panel-admin-agregar-productos.html", 1500);
            setTimeout(() => window.location.href = "index.html", 1500);
        } else {
            // 14. Si hubo error, mostramos el mensaje
            if (resultado.errors) {
                contenedorMensajes.innerHTML = `
                    <p style="color:red;">${resultado.message}</p>
                    <ul style="color:red;">
                        ${resultado.errors.map(err => `<li>${err}</li>`).join("")}
                    </ul>
                `;
            } else {
                contenedorMensajes.innerHTML = `<p style="color:red;">${resultado.message || "Error desconocido"}</p>`;
            }
        }
    } catch (error) {
        // 15. Capturamos errores de red o servidor
        contenedorMensajes.innerHTML = `<p style="color:red;">Error de conexión con el servidor</p>`;
        console.error("Error en la petición:", error);
    }
});
