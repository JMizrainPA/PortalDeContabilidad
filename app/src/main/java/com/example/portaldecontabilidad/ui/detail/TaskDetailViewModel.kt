package com.example.portaldecontabilidad.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * El `ViewModel` para la pantalla de detalle de una tarea (`TaskDetailScreen`).
 * Como en toda arquitectura MVVM, este es el cerebro que maneja la lógica y los datos
 * para una tarea específica.
 */
class TaskDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // `savedStateHandle` es un mecanismo para recibir los argumentos que se pasaron a esta pantalla (como el ID de la tarea).
    // Aquí, obtenemos el `taskId` de los argumentos de navegación. `checkNotNull` asegura que nunca sea nulo.
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    private val repository = TaskRepository() // Nuestra conexión con la capa de datos (el Modelo).

    // `_uiState` contiene el estado de la pantalla. En este caso, es la tarea que estamos viendo.
    // Es de tipo `Task?` (nulable) porque al inicio, mientras carga, puede que aún no tengamos la tarea.
    private val _uiState = MutableStateFlow<Task?>(null)

    // Exponemos el estado como `StateFlow` de solo lectura a la Vista (la pantalla).
    // La pantalla observará este `uiState` y se re-dibujará cuando la tarea cargue o cambie.
    val uiState: StateFlow<Task?> = _uiState.asStateFlow()

    // El bloque `init` se ejecuta cuando se crea el ViewModel.
    init {
        // Usamos una corrutina para buscar la tarea sin bloquear la interfaz.
        viewModelScope.launch {
            // Le pedimos al repositorio que nos de la tarea por su ID.
            // `collect` se suscribe a los cambios. Si la tarea se actualiza en la base de datos,
            // este bloque se volverá a ejecutar y la UI se actualizará sola.
            repository.getTaskById(taskId).collect {
                _uiState.value = it // Actualizamos el estado con la tarea encontrada.
            }
        }
    }

    /**
     * Esta función la llama la Vista cuando el usuario cambia el estado de la tarea (ej. de "Pendiente" a "En Progreso").
     */
    fun updateStatus(status: TaskStatus) {
        // `_uiState.value?.let` es una forma segura de ejecutar código solo si la tarea no es nula (ya cargó).
        _uiState.value?.let { task ->
            // Creamos una copia de la tarea actual, pero con el nuevo estado.
            val updatedTask = task.copy(status = status)
            // Lanzamos una corrutina para actualizar la tarea en la base de datos.
            viewModelScope.launch {
                repository.updateTask(updatedTask)
            }
        }
    }
}
