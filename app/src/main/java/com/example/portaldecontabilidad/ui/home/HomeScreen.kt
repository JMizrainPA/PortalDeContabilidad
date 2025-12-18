package com.example.portaldecontabilidad.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portaldecontabilidad.R
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskStatus

/**
 * Esta es nuestra "Vista" para la pantalla principal, siguiendo el patrón MVVM.
 * Su responsabilidad es mostrar los datos que el `HomeViewModel` le proporciona
 * y notificar al ViewModel sobre las acciones del usuario (clics, etc.).
 * Es una pantalla "tonta" en el buen sentido: no contiene lógica de negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // Inyectamos el ViewModel, que es el cerebro de esta pantalla.
    homeViewModel: HomeViewModel = viewModel(),
    // Funciones de navegación que nos vienen dadas desde fuera (el sistema de navegación de la app).
    onTaskClick: (String) -> Unit, // Para ir a la pantalla de detalle.
    onAddTaskClick: () -> Unit      // Para ir a la pantalla de crear nueva tarea.
) {
    // Nos "suscribimos" al estado del ViewModel. Cada vez que el `uiState` cambie,
    // Compose se encargará de "re-dibujar" esta pantalla con la nueva información.
    val uiState by homeViewModel.uiState.collectAsState()
    val primaryColor = Color(0xFF1b396a)

    // El esqueleto de la pantalla.
    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = { TopBar() },
        floatingActionButton = { // El botón flotante para añadir tareas.
            FloatingActionButton(
                onClick = onAddTaskClick, // Cuando se hace clic, llamamos a la función de navegación.
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Tarea")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Pestañas para filtrar por estado (Todas, Pendientes, etc.).
            TaskTabs(
                selectedStatus = uiState.selectedStatus, // El estado seleccionado viene del ViewModel.
                primaryColor = primaryColor,
                onStatusSelected = { status -> homeViewModel.filterTasks(status) } // Cuando el usuario selecciona una pestaña, se lo decimos al ViewModel.
            )
            FilterButtons() // Botones de filtro (aún sin implementar).
            // La lista de tareas.
            TaskList(
                tasks = uiState.tasks, // La lista de tareas a mostrar viene del ViewModel.
                onTaskClick = onTaskClick, // Pasamos la función de clic para que cada item sea navegable.
                onDeleteTask = { homeViewModel.deleteTask(it) } // Cuando se hace clic en borrar, se lo decimos al ViewModel.
            )
        }
    }
}

/**
 * Componentes pequeños y reutilizables que forman la pantalla.
 * Estos no necesitan saber nada sobre el ViewModel.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text("Mis Tareas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Perfil",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
            )
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun TaskTabs(selectedStatus: TaskStatus, primaryColor: Color, onStatusSelected: (TaskStatus) -> Unit) {
    val statuses = listOf(TaskStatus.ALL, TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
    val tabNames = listOf("Todas", "Pendientes", "En\nProgreso", "Completadas")
    val selectedTabIndex = statuses.indexOf(selectedStatus)

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = Color.Gray,
        indicator = { tabPositions ->
            if (selectedTabIndex in tabPositions.indices) {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = primaryColor,
                    width = 50.dp
                )
            }
        },
        divider = {}
    ) {
        statuses.forEachIndexed { index, status ->
            Tab(
                selected = status == selectedStatus,
                onClick = { onStatusSelected(status) },
                text = { Text(tabNames[index], textAlign = TextAlign.Center, lineHeight = 18.sp) },
                selectedContentColor = primaryColor,
                unselectedContentColor = Color.Gray
            )
        }
    }
}

@Composable
fun FilterButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip("Fecha", modifier = Modifier.weight(1f))
        FilterChip("Prioridad", modifier = Modifier.weight(1f))
        FilterChip("Departamento", modifier = Modifier.weight(1.3f))
    }
}

@Composable
fun FilterChip(text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { /* TODO */ },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        border = null
    ) {
        Text(text, color = Color.DarkGray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun TaskList(tasks: List<Task>, onTaskClick: (String) -> Unit, onDeleteTask: (Task) -> Unit) {
    // `LazyColumn` es eficiente para mostrar listas largas. Solo crea y muestra los elementos que caben en pantalla.
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // `items` es un constructor de `LazyColumn` que sabe cómo manejar una lista de datos.
        items(tasks) { task ->
            TaskItem(task = task, onTaskClick = onTaskClick, onDeleteTask = onDeleteTask)
        }
    }
}

@Composable
fun TaskItem(task: Task, onTaskClick: (String) -> Unit, onDeleteTask: (Task) -> Unit) {
    val primaryColor = Color(0xFF1b396a)
    val icon = when (task.department) {
        "Contabilidad General" -> Icons.Filled.AccountBalance
        "Cuentas por Pagar" -> Icons.Filled.ReceiptLong
        "Fiscal" -> Icons.Filled.Book
        "Recursos Humanos" -> Icons.Filled.AttachMoney
        else -> Icons.Filled.Work
    }

    val statusDetails = when (task.status) {
        TaskStatus.IN_PROGRESS -> Triple(Icons.Filled.Schedule, "Vence: Hoy", Color(0xFFF09A34))
        TaskStatus.PENDING -> Triple(Icons.Filled.CalendarToday, "Vence: Mañana", Color.Gray)
        TaskStatus.HIGH_PRIORITY -> Triple(Icons.Filled.Warning, "Vence: 2 días", Color.Red)
        TaskStatus.COMPLETED -> Triple(Icons.Filled.CheckCircle, "Finalizada", Color(0xFF4CAF50))
        else -> Triple(Icons.Filled.Info, "", Color.Gray)
    }

    Card(
        modifier = Modifier.clickable { onTaskClick(task.id) }, // Hacemos toda la tarjeta clickeable.
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F3F8))
                        ) {
                            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(task.department, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(task.status.color))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(task.status)
                    Spacer(modifier = Modifier.width(16.dp))
                    if (task.status != TaskStatus.ALL && task.status != TaskStatus.COMPLETED) {
                        Icon(statusDetails.first, contentDescription = null, tint = statusDetails.third, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusDetails.second, color = statusDetails.third, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    } else if (task.status == TaskStatus.COMPLETED) {
                        Icon(statusDetails.first, contentDescription = null, tint = statusDetails.third, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusDetails.second, color = statusDetails.third, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            IconButton(onClick = { onDeleteTask(task) }) { // Botón para borrar.
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar Tarea", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun StatusChip(status: TaskStatus) {
    val statusText = when(status) {
        TaskStatus.HIGH_PRIORITY -> "Alta Prioridad"
        TaskStatus.IN_PROGRESS -> "En Progreso"
        else -> status.displayName
    }
    val (backgroundColor, textColor) = when (status) {
        TaskStatus.PENDING -> Pair(Color(0xFFF0F0F0), Color.DarkGray)
        TaskStatus.HIGH_PRIORITY -> Pair(Color.Red.copy(alpha = 0.1f), Color.Red)
        TaskStatus.IN_PROGRESS -> Pair(Color(0xFF1b396a).copy(alpha = 0.1f), Color(0xFF1b396a))
        TaskStatus.COMPLETED -> Pair(Color(0xFF4CAF50).copy(alpha = 0.1f), Color(0xFF4CAF50))
        else -> Pair(Color.Transparent, Color.Transparent)
    }

    if (status != TaskStatus.ALL) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = statusText, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Vista previa para ver cómo se ve la pantalla en Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onTaskClick = {}, onAddTaskClick = {})
}
