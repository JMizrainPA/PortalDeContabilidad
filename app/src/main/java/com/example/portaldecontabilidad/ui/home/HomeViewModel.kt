package com.example.portaldecontabilidad.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Este es el `ViewModel` para nuestra `HomeScreen`. Es el "cerebro" de la pantalla principal.
 * En MVVM, el ViewModel se encarga de toda la lógica de negocio y de preparar los datos
 * para que la Vista (la pantalla) solo tenga que mostrarlos.
 */
class HomeViewModel : ViewModel() {

    // `_uiState` es un `MutableStateFlow`. Es "mutable" porque podemos cambiar su valor aquí dentro del ViewModel.
    // Contiene toda la información que la pantalla necesita para dibujarse.
    private val _uiState = MutableStateFlow(HomeUiState())

    // `uiState` es la versión "inmutable" o de solo lectura que exponemos a la Vista (HomeScreen).
    // La Vista "observa" este `StateFlow` y se actualiza automáticamente cuando su valor cambia.
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Esta es nuestra conexión con la capa de datos (el "Modelo").
    // El ViewModel pide los datos al Repositorio, que se encarga de obtenerlos (de la base de datos, de internet, etc.).
    private val taskRepository = TaskRepository()

    // Guardamos una copia de todas las tareas aquí para no tener que pedirlas a la base de datos cada vez que filtramos.
    private var allTasks: List<Task> = emptyList()

    // El bloque `init` se ejecuta en cuanto se crea el ViewModel.
    init {
        // Usamos `viewModelScope.launch` para iniciar una corrutina. Esto es importante para no bloquear
        // la interfaz de usuario mientras esperamos a que la base de datos nos devuelva las tareas.
        viewModelScope.launch {
            // Nos "suscribimos" a los cambios en las tareas desde el repositorio.
            // `collect` hará que este bloque se ejecute cada vez que haya un cambio en la lista de tareas (añadir, borrar, etc.).
            taskRepository.getTasks().collect { tasks ->
                // Actualizamos nuestra copia local de las tareas.
                allTasks = tasks
                // Aplicamos el filtro que esté seleccionado actualmente.
                // Al inicio, será el filtro por defecto "Todas".
                filterTasks(uiState.value.selectedStatus)
            }
        }
    }

    /**
     * Esta función la llama la Vista cuando el usuario selecciona un nuevo filtro de estado (ej. "Pendientes").
     */
    fun filterTasks(status: TaskStatus) {
        // Filtramos la lista de tareas `allTasks` según el estado seleccionado.
        val filteredTasks = if (status == TaskStatus.ALL) {
            allTasks // Si el filtro es "Todas", mostramos la lista completa.
        } else {
            allTasks.filter { it.status == status } // Si no, filtramos por el estado.
        }
        // `update` es la forma segura de cambiar el estado. Creamos una copia del estado actual (`it.copy()`)
        // con los nuevos datos. Esto notifica a la Vista para que se vuelva a dibujar con la lista filtrada.
        _uiState.update { it.copy(tasks = filteredTasks, selectedStatus = status) }
    }

    /**
     * Esta función la llama la Vista cuando el usuario quiere eliminar una tarea.
     */
    fun deleteTask(task: Task) {
        // Iniciamos otra corrutina para la operación de borrado.
        viewModelScope.launch {
            // Le decimos al repositorio que borre la tarea por su ID.
            // Como estamos suscritos a los cambios, una vez que se borre, el `collect` en el `init`
            // se disparará y la lista en la UI se actualizará automáticamente.
            taskRepository.deleteTask(task.id)
        }
    }
}

/**
 * Esta es una "clase de estado" (UiState). Su única misión es contener toda la información
 * que la `HomeScreen` necesita para mostrarse en un momento dado. Es como una foto de la pantalla.
 * Tenerlo en una `data class` nos asegura que el estado sea inmutable, lo que es bueno para evitar errores.
 */
data class HomeUiState(
    val tasks: List<Task> = emptyList(), // La lista de tareas que se está mostrando actualmente.
    val selectedStatus: TaskStatus = TaskStatus.ALL // El filtro de estado que está seleccionado.
)
