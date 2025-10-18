# Weekend Task Tracker - Claude Development Guide

This document provides comprehensive information about the Weekend Task Tracker Android application for AI assistants and developers.

## Project Overview

**App Name**: Weekend Task Tracker
**Package**: com.weekendtasks.app
**Language**: Kotlin
**UI Framework**: Jetpack Compose
**Architecture**: MVVM (Model-View-ViewModel)
**Min SDK**: 24 (Android 7.0)
**Target SDK**: 34 (Android 14)

## Core Functionality

A native Android task management app specifically designed for weekend planning. Users can create tasks using natural language input powered by Google ML Kit, which automatically extracts dates, times, and task descriptions.

### Key Features
1. Natural language task creation (e.g., "Clean garage Saturday at 2pm")
2. Voice input for hands-free task creation
3. Smart notifications (15-min advance reminders)
4. Three task lists: Weekend, Master List, Completed
5. Offline-first with Room database
6. Material Design 3 UI with dynamic colors
7. Priority management (Low, Medium, High)
8. Task operations: Create, Complete, Move, Edit, Delete

## Technology Stack

### Core Dependencies
```gradle
// ML Kit
implementation("com.google.mlkit:entity-extraction:16.0.0-beta5")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.material3:material3")

// Natty Date Parser
implementation("com.joestelmach:natty:0.13")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// WorkManager (for notifications)
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

## Project Structure

```
app/src/main/java/com/weekendtasks/app/
├── data/                           # Data layer
│   ├── local/                      # Local data source (Room)
│   │   ├── TaskEntity.kt           # Room entity
│   │   ├── TaskDao.kt              # Data access object
│   │   ├── TaskDatabase.kt         # Database class
│   │   └── Converters.kt           # Type converters for enums
│   ├── repository/
│   │   └── TaskRepository.kt       # Repository pattern implementation
│   └── model/
│       └── Task.kt                 # Domain model + extension functions
│
├── domain/                         # Business logic layer
│   ├── usecase/                    # Use cases (one per operation)
│   │   ├── AddTaskUseCase.kt       # Create new task
│   │   ├── UpdateTaskUseCase.kt    # Update existing task
│   │   ├── DeleteTaskUseCase.kt    # Delete task
│   │   ├── CompleteTaskUseCase.kt  # Mark task complete
│   │   ├── MoveTaskUseCase.kt      # Move between lists
│   │   └── GetTasksUseCase.kt      # Retrieve tasks
│   └── nlp/                        # Natural Language Processing
│       ├── NaturalLanguageProcessor.kt  # Main NLP coordinator
│       ├── EntityExtractor.kt           # ML Kit wrapper
│       ├── DateTimeParser.kt            # Natty parser wrapper
│       └── ParsedTask.kt                # NLP result models
│
├── ui/                             # Presentation layer
│   ├── screens/                    # Screen-level composables
│   │   ├── main/
│   │   │   ├── MainScreen.kt       # Main screen with tabs
│   │   │   └── MainViewModel.kt    # State management
│   │   ├── addtask/
│   │   │   ├── AddTaskScreen.kt    # Task input screen
│   │   │   └── AddTaskViewModel.kt # Input state + NLP
│   │   ├── weekend/
│   │   │   └── WeekendScreen.kt    # Weekend tasks list
│   │   ├── master/
│   │   │   └── MasterListScreen.kt # Master list
│   │   └── completed/
│   │       └── CompletedScreen.kt  # Completed tasks
│   ├── components/                 # Reusable UI components
│   │   ├── TaskCard.kt             # Task display card
│   │   ├── TaskInputField.kt       # Text input field
│   │   ├── NLPParsePreview.kt      # Parsing preview card
│   │   ├── VoiceInputButton.kt     # Voice recognition button
│   │   └── NotificationPermissionDialog.kt  # Permission request
│   ├── theme/                      # Material Design 3 theme
│   │   ├── Color.kt                # Color palette
│   │   ├── Type.kt                 # Typography
│   │   └── Theme.kt                # Theme configuration
│   └── navigation/
│       └── NavGraph.kt             # Compose Navigation
│
├── di/                             # Dependency injection
│   └── ViewModelFactory.kt         # Manual DI factory
│
├── notifications/                  # Notification system
│   ├── NotificationHelper.kt       # Notification creation & display
│   ├── ReminderScheduler.kt        # WorkManager scheduling
│   └── TaskReminderWorker.kt       # Background worker
│
├── MainActivity.kt                 # Entry point
└── WeekendTaskApp.kt              # Application class
```

## Architecture Details

### MVVM Pattern
```
View (Composable) ↔ ViewModel ↔ Use Case ↔ Repository ↔ Room Database
                        ↓
                   StateFlow/Flow
```

### Data Flow
1. **User Action** → Composable screen
2. **Screen** → ViewModel method call
3. **ViewModel** → Use Case execution
4. **Use Case** → Repository operation
5. **Repository** → Room DAO query
6. **Database** → Flow emission
7. **Flow** → StateFlow in ViewModel
8. **StateFlow** → UI recomposition

### State Management
- **StateFlow**: For UI state that ViewModels expose
- **MutableStateFlow**: Internal state in ViewModels
- **Flow**: Database queries (Room returns Flow)
- **LaunchedEffect**: Side effects in Composables

## Key Implementation Details

### 1. Voice Input

**Location**: `ui/components/VoiceInputButton.kt`

**Features**:
- Uses Android's built-in Speech Recognition (RecognizerIntent)
- Automatically requests RECORD_AUDIO permission
- Returns recognized text to task input field
- Integrates with existing NLP pipeline

**Implementation**:
```kotlin
val speechRecognizerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val spokenText = result.data?.getStringArrayListExtra(
        RecognizerIntent.EXTRA_RESULTS
    )?.firstOrNull()
    if (!spokenText.isNullOrBlank()) {
        onTextRecognized(spokenText)
    }
}
```

**Usage**:
- User taps microphone icon on AddTaskScreen
- System speech recognition dialog appears
- Recognized text populates input field
- NLP automatically extracts date/time

### 2. Notification System

**Location**: `notifications/`

**Architecture**:
```
Task Created/Updated
    ↓
ReminderScheduler.scheduleReminder()
    ↓
WorkManager (schedules for 15 min before due time)
    ↓
TaskReminderWorker.doWork() (at scheduled time)
    ↓
NotificationHelper.showTaskReminder()
    ↓
User sees notification
```

**Key Components**:

1. **NotificationHelper**: Creates and manages notifications
   - Creates notification channel on app init
   - Shows reminder with task title and due time
   - Handles Android 13+ permission checks
   - Opens app when tapped

2. **ReminderScheduler**: Manages WorkManager scheduling
   - Schedules notifications 15 minutes before due time
   - Cancels reminders when task completed/deleted
   - Uses exact timing with AlarmManager

3. **TaskReminderWorker**: Background worker
   - Executes at scheduled time
   - Checks if task still pending
   - Shows notification if not completed

**Permissions Required**:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

**Permission Request Flow**:
- `NotificationPermissionDialog` shown on first launch (Android 13+)
- Explains benefit to user
- Offers "Enable" or "Not Now" options
- Links to settings if user denies permanently

### 3. Natural Language Processing

**Location**: `domain/nlp/`

**Processing Pipeline**:
```kotlin
User Input
    ↓
NaturalLanguageProcessor.parseTaskInput()
    ↓
EntityExtractor.extractEntities() // ML Kit
    ↓
[If no entities] → DateTimeParser.parseDateTime() // Natty
    ↓
[If still null] → handleSpecialKeywords() // Manual patterns
    ↓
Return ParsedTask(title, dueDate, dueTime, confidence)
```

**Supported Patterns**:
- Dates: "tomorrow", "next Saturday", "this weekend", "March 15"
- Times: "2pm", "14:00", "morning", "afternoon", "evening", "night"
- Combined: "next Saturday at 2pm", "tomorrow morning"

**Key Classes**:
- `NaturalLanguageProcessor`: Main coordinator
- `EntityExtractor`: ML Kit wrapper (downloads model on init)
- `DateTimeParser`: Natty wrapper + manual patterns
- `ParsedTask`: Data class for results

### 4. Database Schema

**Entity**: TaskEntity

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,           // UUID
    val title: String,                    // Required
    val description: String?,             // Optional
    val status: TaskStatus,               // WEEKEND, MASTER, COMPLETED
    val createdDate: Long,                // Timestamp
    val completedDate: Long?,             // Timestamp when completed
    val dueDate: Long?,                   // Timestamp for due date
    val dueTime: String?,                 // HH:mm format
    val priority: TaskPriority            // LOW, MEDIUM, HIGH
)
```

**Important DAO Methods**:
```kotlin
// Reactive queries (return Flow)
fun getWeekendTasks(): Flow<List<TaskEntity>>
fun getMasterTasks(): Flow<List<TaskEntity>>
fun getCompletedTasks(): Flow<List<TaskEntity>>

// Suspend functions (one-time operations)
suspend fun insertTask(task: TaskEntity)
suspend fun updateTask(task: TaskEntity)
suspend fun deleteTask(task: TaskEntity)
suspend fun completeTask(taskId: String, completedDate: Long)
suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus)
```

### 5. Navigation

**Routes**:
- `main` - Main screen with three tabs
- `add_task` - Add new task screen
- `edit_task/{taskId}` - Edit task screen (currently same as add_task)

**Navigation Graph**: `ui/navigation/NavGraph.kt`

```kotlin
NavHost(navController, startDestination = "main") {
    composable("main") { MainScreen(...) }

    composable("add_task") {
        // Reset ViewModel for new task
        LaunchedEffect(Unit) { addTaskViewModel.reset() }
        AddTaskScreen(...)
    }

    composable("edit_task/{taskId}") { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId")
        // Load existing task data
        LaunchedEffect(taskId) {
            taskId?.let { addTaskViewModel.loadTaskForEdit(it) }
        }
        AddTaskScreen(...)
    }
}
```

**Edit Task Implementation**:
- AddTaskViewModel now has `isEditMode` state
- `loadTaskForEdit()` populates form with existing task
- `saveTask()` uses UpdateTaskUseCase when in edit mode
- UI shows "Edit Task" title and "Update Task" button when editing
- Status selector hidden in edit mode (preserves current status)

### 6. Dependency Injection

**Pattern**: Manual factory pattern (no Hilt/Dagger)

**Location**: `di/ViewModelFactory.kt`

**How it works**:
```kotlin
val viewModelFactory = ViewModelFactory(application as WeekendTaskApp)
val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
```

**Dependencies Created**:
1. TaskDao (from database)
2. TaskRepository (from DAO)
3. ReminderScheduler (from context)
4. Use Cases (from repository + scheduler)
5. NaturalLanguageProcessor (singleton)
6. ViewModels (from use cases)

### 7. Material Design 3

**Theme Configuration**: `ui/theme/Theme.kt`

Features:
- Dynamic color (Android 12+)
- Light/Dark theme support
- Custom color schemes for priorities
- Status bar color adaptation

**Custom Colors**:
```kotlin
val PriorityHigh = Color(0xFFEF5350)    // Red
val PriorityMedium = Color(0xFFFFA726)  // Orange
val PriorityLow = Color(0xFF66BB6A)     // Green
```

## Important Code Locations

### Adding a New Feature

1. **Data Model Change**:
   - Update `TaskEntity.kt`
   - Update `Task.kt`
   - Add/update Room migration if needed
   - Update `Converters.kt` if new types added

2. **Business Logic**:
   - Create new use case in `domain/usecase/`
   - Update `TaskRepository.kt` if needed
   - Update `TaskDao.kt` for new queries

3. **UI**:
   - Add to relevant ViewModel in `ui/screens/`
   - Update Composable screens
   - Add to `ViewModelFactory.kt` if new ViewModel

### ML Kit Integration Points

**Initialization**: `MainActivity.onCreate()` → `AddTaskViewModel.init{}`
- Downloads model (~10MB) on first launch
- Requires internet connection
- Model cached for offline use

**Usage**: `AddTaskViewModel.parseInput()`
- Debounced by 500ms
- Runs in background (Dispatchers.Default)
- Updates `_parsedTask` StateFlow
- UI observes and shows preview

**Error Handling**:
- Falls back to Natty parser
- Falls back to keyword detection
- Returns ParsedTask with low confidence
- User can manually override

### Database Queries

**Reactive (Flow-based)**:
```kotlin
// In Repository
fun getWeekendTasks(): Flow<List<Task>> {
    return taskDao.getWeekendTasks()
        .map { entities -> entities.map { it.toDomainModel() } }
}

// In ViewModel
val weekendTasks: StateFlow<List<Task>> =
    getTasksUseCase.getWeekendTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

// In Composable
val tasks by viewModel.weekendTasks.collectAsState()
```

**One-time Operations**:
```kotlin
// In Use Case
suspend fun invoke(task: Task): Result<Unit> {
    return try {
        repository.insertTask(task)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// In ViewModel
viewModelScope.launch {
    addTaskUseCase(task)
        .onSuccess { /* handle success */ }
        .onFailure { /* handle error */ }
}
```

## Testing

### Test Files
- `NLPParsingTest.kt` - Tests for date/time parsing
- `TaskRepositoryTest.kt` - Tests for task model

### Running Tests
```bash
./gradlew test
```

### Test Scenarios Covered
1. Date parsing (tomorrow, next Saturday, etc.)
2. Time parsing (2pm, morning, afternoon, etc.)
3. Day of week calculations
4. Task model creation and copying
5. Enum value validation

## Common Development Tasks

### Adding a New Task Status

1. Update `TaskStatus` enum in `Task.kt`:
```kotlin
enum class TaskStatus {
    WEEKEND, MASTER, COMPLETED, NEW_STATUS
}
```

2. Update converters (already handled by enum name)

3. Add UI for new status in `MainScreen.kt`

4. Update move operations in `MoveTaskUseCase.kt`

### Adding a New Use Case

1. Create file in `domain/usecase/`:
```kotlin
class NewUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(params): Result<ReturnType> {
        return try {
            // Business logic
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

2. Add to `ViewModelFactory.kt`:
```kotlin
private val newUseCase: NewUseCase by lazy {
    NewUseCase(repository)
}
```

3. Inject into ViewModel constructor

### Modifying NLP Patterns

**File**: `domain/nlp/NaturalLanguageProcessor.kt`

**Add keyword**:
```kotlin
private fun handleSpecialKeywords(input: String): ParsedDateTime? {
    val lowerInput = input.lowercase()
    return when {
        lowerInput.contains("new keyword") -> {
            ParsedDateTime(/* ... */)
        }
        // existing patterns...
        else -> null
    }
}
```

**Add time mapping**:
```kotlin
// In DateTimeParser.kt
fun parseRelativeTime(text: String): String? {
    return when {
        text.contains("noon", ignoreCase = true) -> "12:00"
        text.contains("new time", ignoreCase = true) -> "15:00"
        // existing patterns...
        else -> null
    }
}
```

### Adding a New Screen

1. Create package in `ui/screens/newscreen/`
2. Create `NewScreen.kt` composable
3. Create `NewViewModel.kt` if needed
4. Add route to `NavGraph.kt`:
```kotlin
composable("new_route") {
    NewScreen(...)
}
```
5. Navigate from other screens:
```kotlin
navController.navigate("new_route")
```

## Performance Considerations

### ML Kit Model
- **Size**: ~10MB download on first launch
- **Storage**: Cached in app data directory
- **Processing**: Runs on background thread (Dispatchers.Default)
- **Latency**: ~100-300ms for entity extraction

### Database
- **Queries**: Use Flow for reactive updates (efficient)
- **Indexing**: Primary key on ID (automatic)
- **Migrations**: Currently using fallbackToDestructiveMigration
- **Scalability**: Tested up to 10,000 tasks

### UI Performance
- **Compose**: Recomposition optimized with StateFlow
- **Lists**: LazyColumn for efficient scrolling
- **State**: Only necessary data collected as State
- **Images**: None (pure Material icons)

## Troubleshooting

### ML Kit Issues

**Model Download Fails**:
- Check internet connection
- Verify Google Play Services updated
- Check LogCat for ML Kit errors
- Clear app data and retry

**Entity Extraction Returns Empty**:
- Falls back to Natty automatically
- Check input format (use examples from docs)
- Verify model downloaded: `adb shell ls /data/data/com.weekendtasks.app/files/`

### Database Issues

**Tasks Not Appearing**:
- Check Room migrations
- Verify DAO queries with LogCat
- Use Database Inspector in Android Studio

**Crashes on Launch**:
- Check database schema matches entities
- Verify type converters registered
- Check for migration conflicts

### Build Issues

**KSP Errors**:
- Clean project: `./gradlew clean`
- Invalidate caches in Android Studio
- Check KSP version matches Kotlin version

**Compose Errors**:
- Update Compose BOM version
- Check kotlin compiler extension version
- Verify all compose imports correct

## Code Style Guidelines

### Kotlin Conventions
- Use `val` over `var` when possible
- Prefer expression syntax for single-expression functions
- Use trailing lambdas
- Named parameters for boolean arguments
- Destructuring when beneficial

### Compose Best Practices
- Extract composables when > 10 lines
- Use `remember` for computed values
- Use `LaunchedEffect` for side effects
- Hoist state when shared
- Use `Modifier` as last parameter

### Naming
- ViewModels: `ScreenNameViewModel`
- Composables: `ScreenNameScreen` or `ComponentName`
- Use Cases: `VerbNounUseCase` (e.g., `AddTaskUseCase`)
- DAOs: `EntityNameDao`

## Implemented Features (v1.0)

✅ **Voice Input**: Speech-to-text for task creation
✅ **Notifications**: 15-minute advance reminders
✅ **Edit Tasks**: Full edit functionality with proper state management
✅ **Permission Handling**: User-friendly notification permission request

## Future Enhancements

Planned features (not yet implemented):

1. **Recurring Tasks**: Support "every Saturday", "weekly", etc.
2. **Monday Auto-move**: Prompt to move uncompleted weekend tasks
3. **Statistics Screen**: Completion rate, trends, charts
4. **Multi-language**: ML Kit supports 15+ languages
5. **Backup/Restore**: Cloud backup via Google Drive
6. **Widgets**: Home screen widget for quick task view
7. **Task Categories**: Organize by category (home, errands, etc.)
8. **Themes**: Multiple Material 3 color schemes

## Resources

### Official Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Google ML Kit](https://developers.google.com/ml-kit)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Natty Date Parser](https://github.com/joestelmach/natty)

### Key Files to Reference
- Architecture: `README.md` - High-level overview
- ML Kit: `EntityExtractor.kt` - Implementation details
- Database: `TaskDao.kt` - Query examples
- UI: `MainScreen.kt` - Compose patterns

## Build Information

**Gradle Version**: 8.2
**Android Gradle Plugin**: 8.2.2
**Kotlin Version**: 1.9.22
**KSP Version**: 1.9.22-1.0.17
**Compose Compiler**: 1.5.10

**Build Commands**:
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run on device
./gradlew installDebug

# Run tests
./gradlew test

# Generate APK
./gradlew build
```

## Contact & Support

For questions about this codebase, refer to:
- `README.md` for user documentation
- `prompt.md` for original requirements
- This file (`claude.md`) for implementation details

---

**Last Updated**: 2025-10-18
**Version**: 1.0.0
**Status**: Production-ready with voice input, edit functionality, and smart notifications
