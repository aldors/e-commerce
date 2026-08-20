const botonRegistro = document.getElementById("boton-registro");

//Errores
const contenedorMensajes = document.createElement("div");
contenedorMensajes.id = "mensaje-registro";
document.querySelector(".registro-contenedor").appendChild(contenedorMensajes);

botonRegistro.addEventListener("click", async () => {

    const nombre = document.getElementById("nombre-registro").value;
    const email = document.getElementById("email-registro").value;
    const password = document.getElementById("contraseña-registro").value;

    const datosRegistro = {
        nombre: nombre,
        email: email,
        password: password
    };

    try {
        const respuesta = await fetch(
                                    "http://localhost:8080/auth/registro",{
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "application/json"
                                    },
                                    body: JSON.stringify(datosRegistro)
                                });

                                const resultado = await respuesta.json();

                                // Errores
                                contenedorMensajes.innerHTML = "";

                                if (respuesta.ok && resultado.success) {
                                    contenedorMensajes.innerHTML = `<p style="color:green;">${resultado.message}</p>`;
                                    setTimeout(() => window.location.href = "login.html", 1500);
                                } else {

                                    if(resultado.errors && Array.isArray(resultado.errors)){
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
        contenedorMensajes.innerHTML = `<p style="color:red;">Error de conexión con el servidor</p>`;
        console.error("Error en la petición:", error);
    }
});