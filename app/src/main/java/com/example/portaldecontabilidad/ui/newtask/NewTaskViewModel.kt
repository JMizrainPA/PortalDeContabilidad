package com.example.portaldecontabilidad.ui.newtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Priority
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

/**
 * Este es el ViewModel para la pantalla `NewTaskScreen`. Es el cerebro que se encarga de la lógica
 * para crear una nueva tarea. La Vista solo le notifica las acciones del usuario y este ViewModel
 * hace todo el trabajo de verdad.
 */
class NewTaskViewModel : ViewModel() {

    // `_uiState` es el estado "privado" y "mutable" de nuestra pantalla. Aquí guardamos todo lo que
    // el usuario va escribiendo (título, descripción, etc.).
    private val _uiState = MutableStateFlow(NewTaskUiState())

    // `uiState` es la versión pública y de "solo lectura" del estado. Se la damos a la Vista
    // para que pueda mostrar la información. La Vista observa este `StateFlow` y se actualiza sola
    // cada vez que `_uiState` cambia.
    val uiState: StateFlow<NewTaskUiState> = _uiState.asStateFlow()

    // El repositorio es nuestra puerta de acceso a la capa de datos (el Modelo).
    // El ViewModel no sabe cómo se guardan los datos, solo le dice al repositorio "guarda esta tarea".
    private val repository = TaskRepository()

    // --- FUNCIONES LLAMADAS POR LA VISTA ---
    // Estas funciones son como los "mensajeros" que la Vista usa para decirle al ViewModel que algo pasó.

    fun onTitleChange(title: String) {
        // Cuando el usuario escribe en el campo de título, actualizamos el estado.
        // `update` y `copy` es la forma correcta y segura de cambiar un StateFlow en Kotlin.
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onDateChangeString(dateString: String) {
        _uiState.update { it.copy(dueDateString = dateString) }
    }

    fun onDepartmentChange(department: String) {
        _uiState.update { it.copy(department = department) }
    }

    fun onPriorityChange(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    /**
     * La función más importante: contiene la lógica para crear la tarea.
     * La Vista la llama cuando el usuario presiona "listo" en el teclado.
     */
    fun createTask() {
        // Lanzamos una corrutina porque guardar en la base de datos puede tardar y no queremos congelar la app.
        viewModelScope.launch {
            val state = uiState.value // Tomamos una foto del estado actual.
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dueDate: LocalDate?

            // 1. Validar la fecha.
            try {
                // Intentamos convertir el texto de la fecha a un objeto `LocalDate`.
                dueDate = LocalDate.parse(state.dueDateString, dateFormatter)
            } catch (e: DateTimeParseException) {
                // Si falla (ej. el formato es "25/13/2024"), actualizamos el estado con un mensaje de error
                // y detenemos la ejecución de esta función.
                _uiState.update { it.copy(userMessage = "Formato de fecha inválido. Usa dd/MM/yyyy.") }
                return@launch
            }

            // 2. Validar que los campos no estén vacíos.
            if (state.title.isNotBlank() && state.department.isNotBlank()) {
                // 3. Crear el objeto Tarea.
                val dueDateShortFormatter = DateTimeFormatter.ofPattern("d MMM")
                val dueDateFullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

                val newTask = Task(
                    id = UUID.randomUUID().toString(), // Generamos un ID único.
                    title = state.title,
                    description = state.description,
                    department = state.department,
                    priority = state.priority,
                    status = TaskStatus.PENDING, // Las tareas nuevas siempre empiezan como pendientes.
                    dueDate = dueDate.format(dueDateShortFormatter),
                    fullDueDate = dueDate.format(dueDateFullFormatter),
                    taskNumber = "#${state.department.substring(0, 3).uppercase()}-${UUID.randomUUID().toString().substring(0, 4).uppercase()}"
                )
                // 4. Guardar la tarea.
                repository.addTask(newTask)
                // 5. Notificar a la UI que todo salió bien.
                _uiState.update { it.copy(taskCreated = true, userMessage = "Tarea creada con éxito") }
            } else {
                // Si faltan campos, mandamos otro mensaje de error.
                _uiState.update { it.copy(userMessage = "Por favor, completa todos los campos obligatorios.") }
            }
        }
    }

    // --- FUNCIONES PARA GESTIONAR EVENTOS ---
    // Estas funciones ayudan a que los eventos (como mostrar un mensaje) ocurran solo una vez.

    /**
     * La Vista llama a esta función después de mostrar el Snackbar para "limpiar" el mensaje
     * y evitar que se muestre de nuevo si la pantalla se re-dibuja.
     */
    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /**
     * La Vista llama a esta función después de navegar hacia atrás para "limpiar" el estado `taskCreated`.
     * Esto evita que la app navegue hacia atrás infinitamente si el usuario vuelve a esta pantalla.
     */
    fun onNavigated() {
        _uiState.update { it.copy(taskCreated = false) }
    }
}

/**
 * Nuestra clase de estado para la pantalla de nueva tarea.
 * Es como una plantilla que contiene toda la información que necesita la pantalla para mostrarse.
 * Usar una `data class` nos garantiza que el estado sea inmutable, lo que es una buena práctica.
 */
data class NewTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDateString: String = "", // Guardamos la fecha como String porque el usuario la escribe.
    val department: String = "",
    val priority: Priority = Priority.LOW,
    val taskCreated: Boolean = false, // Una bandera para avisar a la UI que la tarea se creó y que debe navegar.
    val userMessage: String? = null // Para mandar mensajes a la UI (éxito, error, etc.). Es nulable.
)
