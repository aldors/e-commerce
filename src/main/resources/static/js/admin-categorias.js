// admin.js

// 1. Contenedor de mensajes
const contenedorMensajes = document.createElement("div");
contenedorMensajes.id = "mensajes_admin";
document.body.insertBefore(contenedorMensajes, document.body.firstChild);

// 2. Función para mostrar mensajes
function mostrarMensaje(texto, tipo = "error") {
    contenedorMensajes.innerHTML = `<p style="color:${tipo === "success" ? "green" : "red"};">${texto}</p>`;
}

// 3. Obtener tokens desde localStorage
let accessToken = localStorage.getItem("accessToken");
const refreshToken = localStorage.getItem("refreshToken");

// 4. Función para refrescar el accessToken
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
    } catch (error) {
        mostrarMensaje("Error de conexión al refrescar token");
        return false;
    }
}

// 5. Función genérica para peticiones con retry
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
// 8. Listar categorias
// =======================
async function listarCategorias() {
    try {
        const respuesta = await fetchConToken("http://localhost:8080/categorias/listar", { method: "GET" });
        const resultado = await respuesta.json();

        const tablaBody = document.querySelector(".tabla tbody");
        tablaBody.innerHTML = "";

        if (respuesta.ok && resultado.success) {
            resultado.data.forEach(categoria => {
                const fila = document.createElement("tr");
                fila.innerHTML = `
                    <td>${categoria.nombre}</td>
                    <td>
                        <button class="btn-actualizar" data-id="${categoria.id}">Actualizar</button>
                        <button class="btn-eliminar" data-id="${categoria.id}">Eliminar</button>
                    </td>
                `;
                tablaBody.appendChild(fila);
            });
        } else {
            mostrarMensaje(resultado.message || "Error al listar categorias");
        }
    } catch {
        mostrarMensaje("Error de conexión al listar categorias");
    }
}


// =======================
// 7. Crear o actualizar categorias con el mismo formulario
// =======================
let idcategoriaEditando = null;

document.getElementById("formCategoria").addEventListener("submit", async (e) => {
    e.preventDefault();

    const categoria = {
        nombre: document.getElementById("nombre").value
    };

    try {
        let respuesta;
        if (idcategoriaEditando) {
            // Actualizar
            respuesta = await fetchConToken(`http://localhost:8080/categorias/actualizar/${idcategoriaEditando}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(categoria)
            });
        } else {
            // Crear
            respuesta = await fetchConToken("http://localhost:8080/categorias/crear", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(categoria)
            });
        }

        const resultado = await respuesta.json();
        contenedorMensajes.innerHTML = "";

        if (respuesta.ok && resultado.success) {
            mostrarMensaje(resultado.message, "success");
            listarCategorias(); // refrescar tabla
            e.target.reset(); // limpiar formulario
            idcategoriaEditando = null;
        } else {
            if (resultado.errors && Array.isArray(resultado.errors)) {
                // Mostrar todos los errores de validación concatenados
                mostrarMensaje(`${resultado.message}: ${resultado.errors.join(", ")}`);
            } else {
                mostrarMensaje(resultado.message || "Error desconocido");
            }
        }
    } catch {
        mostrarMensaje("Error de conexión con el servidor");
    }
});

// =======================
// 8. Eliminar categoria
// =======================
document.querySelector(".tabla").addEventListener("click", async (e) => {
    if (e.target.classList.contains("btn-eliminar")) {
        const id = e.target.dataset.id;
        try {
            const respuesta = await fetchConToken(`http://localhost:8080/categorias/eliminar/${id}`, {
                method: "DELETE"
            });

            if (respuesta.status === 204) {
                mostrarMensaje("Producto eliminado correctamente", "success");
                listarCategorias();
            } else {
                const resultado = await respuesta.json();
                mostrarMensaje(resultado.message || "Error al eliminar categoria");
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al eliminar categoria");
        }
    }
});

// =======================
// 9. preparar actualización de categorias
// =======================
document.querySelector(".tabla").addEventListener("click", async (e) => {
    if (e.target.classList.contains("btn-actualizar")) {
        const id = e.target.dataset.id;

        const fila = e.target.closest("tr");
        document.getElementById("nombre").value = fila.children[0].textContent;

        idcategoriaEditando = id;
    }
});

// =======================
// 10. Inicializar funcion para listar categorias
// =======================

listarCategorias();