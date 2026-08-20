document.addEventListener("DOMContentLoaded", () => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken) {
        try {
            const payloadBase64 = accessToken.split(".")[1];
            const payloadJson = JSON.parse(atob(payloadBase64));

            console.log("Payload JWT:", payloadJson);

            // Nombre del usuario (claim "nombre")
            const nombreUsuario = payloadJson.nombre || payloadJson.sub;
            document.getElementById("nombre-usuario").textContent = nombreUsuario;

            // Roles: mapear a texto amigable
            const roles = (payloadJson.roles || []).map(r => r.authority);

            let rolTexto = "Usuario"; // valor por defecto
            if (roles.includes("ROLE_ADMIN")) {
                rolTexto = "AMDINISTRADOR";
            } else if (roles.includes("ROLE_USER")) {
                rolTexto = "USUARIO";
            }

            document.getElementById("rol-usuario").textContent = rolTexto;
        } catch (error) {
            console.error("Error al decodificar el token:", error);
        }
    }
});
