package com.example.portaldecontabilidad.ui.newtask

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portaldecontabilidad.data.Priority

/**
 * Esta es nuestra "Vista" en el patrón MVVM.
 * Su trabajo es simplemente mostrar en pantalla la información que le proporciona el `NewTaskViewModel`
 * y notificarle sobre las acciones del usuario, como escribir en un campo de texto.
 * No contiene ninguna lógica de negocio (como guardar una tarea), solo se encarga de lo visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    // Inyectamos el ViewModel, que actúa como el cerebro de esta pantalla.
    newTaskViewModel: NewTaskViewModel = viewModel(),
    // Esta es una función que nos pasan para poder navegar hacia atrás.
    onNavigateBack: () -> Unit,
) {
    // Aquí "escuchamos" el estado (la información) que el ViewModel nos quiere mostrar.
    // `collectAsState()` es clave: cada vez que el `uiState` en el ViewModel cambie,
    // esta pantalla se "re-dibujará" automáticamente para reflejar esos cambios.
    val uiState by newTaskViewModel.uiState.collectAsState()

    // Herramientas de Compose para manejar la UI.
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- MANEJO DE EFECTOS SECUNDARIOS ---
    // `LaunchedEffect` se usa para ejecutar código que no es parte de la UI, como mostrar un mensaje
    // o navegar, cuando un estado específico cambia.

    // Este efecto se dispara cada vez que `uiState.userMessage` cambia.
    LaunchedEffect(key1 = uiState.userMessage) {
        // Si el ViewModel nos manda un mensaje (ej. "Tarea creada"), lo mostramos en un Snackbar.
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            // Le avisamos al ViewModel que ya mostramos el mensaje, para que no lo vuelva a mandar.
            newTaskViewModel.onUserMessageShown()
        }
    }

    // Este efecto se dispara cuando `uiState.taskCreated` cambia a `true`.
    LaunchedEffect(key1 = uiState.taskCreated) {
        if (uiState.taskCreated) {
            // Si la tarea se creó con éxito, llamamos a la función para volver a la pantalla anterior.
            onNavigateBack()
            // Le avisamos al ViewModel que ya navegamos para que reinicie el estado.
            newTaskViewModel.onNavigated()
        }
    }

    // `Scaffold` es como un esqueleto para la pantalla, nos permite poner una barra superior, contenido, etc.
    Scaffold(
        topBar = { NewTaskTopBar(onNavigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // --- CUERPO DE LA PANTALLA ---
        // Una columna que organiza los elementos de forma vertical.
        Column(
            modifier = Modifier
                .padding(paddingValues) // Padding para que el contenido no quede debajo de la TopBar.
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre cada elemento.
        ) {

            // Campo para el título de la tarea.
            OutlinedTextField(
                value = uiState.title, // El texto a mostrar viene del ViewModel.
                onValueChange = { newTaskViewModel.onTitleChange(it) }, // Cuando el usuario escribe, notificamos al ViewModel.
                label = { Text("Título de la tarea") },
                placeholder = { Text("Ej. Revisión trimestral IVA") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next) // El botón "Enter" del teclado será "Siguiente".
            )

            // Campo para la descripción.
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { newTaskViewModel.onDescriptionChange(it) },
                label = { Text("Descripción") },
                placeholder = { Text("Añadir detalles sobre la tarea...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Menús desplegables para Departamento y Prioridad.
            DepartmentDropdown(uiState.department) { newTaskViewModel.onDepartmentChange(it) }
            PriorityDropdown(uiState.priority) { newTaskViewModel.onPriorityChange(it) }

            // Campo para la fecha de vencimiento.
            OutlinedTextField(
                value = uiState.dueDateString, // El valor viene del ViewModel.
                onValueChange = { newTaskViewModel.onDateChangeString(it) }, // Cuando el usuario escribe, notificamos al ViewModel.
                label = { Text("Fecha de vencimiento (dd/MM/yyyy)") },
                placeholder = { Text("Ej. 25/12/2024") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // El botón "Enter" del teclado será "Listo".
                keyboardActions = KeyboardActions(
                    onDone = { // Cuando el usuario presiona "Listo"...
                        keyboardController?.hide() // Escondemos el teclado.
                        newTaskViewModel.createTask() // Le decimos al ViewModel que intente crear la tarea.
                    }
                )
            )
        }
    }
}

/**
 * Un componente reutilizable para mostrar un menú desplegable con los departamentos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentDropdown(selectedDepartment: String, onDepartmentSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val departments = listOf("Contabilidad General", "Cuentas por Pagar", "Fiscal", "Recursos Humanos")

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedDepartment,
            onValueChange = {}, // No se puede cambiar escribiendo.
            label = { Text("Departamento") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            departments.forEach { department ->
                DropdownMenuItem(
                    text = { Text(department) },
                    onClick = {
                        onDepartmentSelected(department) // Notifica el departamento seleccionado.
                        expanded = false // Cierra el menú.
                    }
                )
            }
        }
    }
}

/**
 * Un componente reutilizable para mostrar un menú desplegable con las prioridades.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityDropdown(selectedPriority: Priority, onPrioritySelected: (Priority) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val priorities = Priority.values().filterNot { it == Priority.MEDIUM }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedPriority.displayName,
            onValueChange = {},
            label = { Text("Prioridad") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            priorities.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.displayName) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * La barra de navegación superior de la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) { // Llama a la función para navegar hacia atrás.
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
    )
}

/**
 * Una vista previa para ver cómo se ve nuestro Composable en Android Studio sin tener que correr la app.
 */
@Preview(showBackground = true)
@Composable
fun NewTaskScreenPreview() {
    NewTaskScreen(onNavigateBack = {})
}
