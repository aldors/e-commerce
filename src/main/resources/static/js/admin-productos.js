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
// 6. Llenar categorías
// =======================
async function cargarCategorias() {
    try {
        const respuesta = await fetchConToken("http://localhost:8080/categorias/listar", { method: "GET" });
        const resultado = await respuesta.json();

        const select = document.getElementById("categoria");
        select.innerHTML = "";

        if (respuesta.ok && resultado.success) {
            resultado.data.forEach(cat => {
                const option = document.createElement("option");
                option.value = cat.id;
                option.textContent = cat.nombre;
                select.appendChild(option);
            });
        } else {
            mostrarMensaje(resultado.message || "Error al cargar categorías");
        }
    } catch (error) {
        mostrarMensaje("Error de conexión al cargar categorías");
    }
}

// =======================
// 7. Crear o actualizar producto con el mismo formulario
// =======================
let idProductoEditando = null;

document.getElementById("formProducto").addEventListener("submit", async (e) => {
    e.preventDefault();

    const producto = {
        nombre: document.getElementById("nombre").value,
        descripcion: document.getElementById("descripcion").value,
        precio: parseFloat(document.getElementById("precio").value),
        stock: parseInt(document.getElementById("stock").value),
        categoriaId: parseInt(document.getElementById("categoria").value),
        activo: document.getElementById("activo").checked
    };

    try {
        let respuesta;
        if (idProductoEditando) {
            // Actualizar
            respuesta = await fetchConToken(`http://localhost:8080/productos/actualizar/${idProductoEditando}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(producto)
            });
        } else {
            // Crear
            respuesta = await fetchConToken("http://localhost:8080/productos/crear", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(producto)
            });
        }

        const resultado = await respuesta.json();
        contenedorMensajes.innerHTML = "";

        if (respuesta.ok && resultado.success) {
            mostrarMensaje(resultado.message, "success");
            listarProductos(); // refrescar tabla
            e.target.reset(); // limpiar formulario
            idProductoEditando = null;
            document.getElementById("activoContainer").style.display = "none"; // ocultar checkbox
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
// 8. Listar productos
// =======================
async function listarProductos() {
    try {
        const respuesta = await fetchConToken("http://localhost:8080/productos/listar", { method: "GET" });
        const resultado = await respuesta.json();

        const tablaBody = document.querySelector(".tabla tbody");
        tablaBody.innerHTML = "";

        if (respuesta.ok && resultado.success) {
            resultado.data.content.forEach(prod => {
                const fila = document.createElement("tr");
                fila.innerHTML = `
                    <td>${prod.nombre}</td>
                    <td>${prod.descripcion}</td>
                    <td>${prod.precio}</td>
                    <td>${prod.stock}</td>
                    <td>${prod.categoria}</td>
                    <td class="disponible">${prod.activo ? "Sí" : "No"}</td>
                    <td>
                        <button class="btn-actualizar" data-id="${prod.id}">Actualizar</button>
                        <button class="btn-eliminar" data-id="${prod.id}">Eliminar</button>
                    </td>
                `;
                tablaBody.appendChild(fila);
            });
        } else {
            mostrarMensaje(resultado.message || "Error al listar productos");
        }
    } catch {
        mostrarMensaje("Error de conexión al listar productos");
    }
}

// =======================
// 8. Eliminar producto
// =======================
document.querySelector(".tabla").addEventListener("click", async (e) => {
    if (e.target.classList.contains("btn-eliminar")) {
        const id = e.target.dataset.id;
        try {
            const respuesta = await fetchConToken(`http://localhost:8080/productos/eliminar/${id}`, {
                method: "DELETE"
            });

            if (respuesta.status === 204) {
                mostrarMensaje("Producto eliminado correctamente", "success");
                // Actualizar columna "Disponible" a "No"
                e.target.closest("tr").querySelector(".disponible").textContent = "No";
            } else {
                const resultado = await respuesta.json();
                mostrarMensaje(resultado.message || "Error al eliminar producto");
            }
        } catch (error) {
            mostrarMensaje("Error de conexión al eliminar producto");
        }
    }
});

// =======================
// 9. preparar actualización
// =======================
document.querySelector(".tabla").addEventListener("click", async (e) => {
    if (e.target.classList.contains("btn-actualizar")) {
        const id = e.target.dataset.id;

        const fila = e.target.closest("tr");
        document.getElementById("nombre").value = fila.children[0].textContent;
        document.getElementById("descripcion").value = fila.children[1].textContent;
        document.getElementById("precio").value = fila.children[2].textContent;
        document.getElementById("stock").value = fila.children[3].textContent;
        document.getElementById("categoria").value = fila.children[4].textContent;
        document.getElementById("activo").checked = fila.querySelector(".disponible").textContent === "Sí";

        idProductoEditando = id;
        document.getElementById("activoContainer").style.display = "block"; // mostrar checkbox
    }
});

// =======================
// 10. Inicializar
// =======================
cargarCategorias();
listarProductos();
