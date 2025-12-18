package com.example.portaldecontabilidad.ui.newtask

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portaldecontabilidad.data.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    newTaskViewModel: NewTaskViewModel = viewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by newTaskViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handles showing snackbar messages
    LaunchedEffect(key1 = uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            newTaskViewModel.onUserMessageShown() // Consume the message
        }
    }

    // Handles navigation on success
    LaunchedEffect(key1 = uiState.taskCreated) {
        if (uiState.taskCreated) {
            onNavigateBack()
            newTaskViewModel.onNavigated() // Consume the navigation event
        }
    }

    Scaffold(
        topBar = { NewTaskTopBar(onNavigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { newTaskViewModel.onTitleChange(it) },
                label = { Text("Título de la tarea") },
                placeholder = { Text("Ej. Revisión trimestral IVA") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
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
            DepartmentDropdown(uiState.department) { newTaskViewModel.onDepartmentChange(it) }
            PriorityDropdown(uiState.priority) { newTaskViewModel.onPriorityChange(it) }

            OutlinedTextField(
                value = uiState.dueDateString,
                onValueChange = { newTaskViewModel.onDateChangeString(it) },
                label = { Text("Fecha de vencimiento (dd/MM/yyyy)") },
                placeholder = { Text("Ej. 25/12/2024") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        newTaskViewModel.createTask()
                    }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentDropdown(selectedDepartment: String, onDepartmentSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val departments = listOf("Contabilidad General", "Cuentas por Pagar", "Fiscal", "Recursos Humanos")

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedDepartment,
            onValueChange = {}, // readonly
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
                        onDepartmentSelected(department)
                        expanded = false
                    }
                )
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NewTaskScreenPreview() {
    NewTaskScreen(onNavigateBack = {})
}
