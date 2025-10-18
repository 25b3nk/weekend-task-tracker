I want to build a native Android app called "Weekend Task Tracker" with Google ML Kit for natural language processing.

## App Purpose
A task management app specifically for weekend personal projects and house tasks. Users can create tasks for the weekend using natural language, mark them complete, and move uncompleted ones to a master list for future weekends.

## Core Features
1. Natural language task input using Google ML Kit (e.g., "Clean garage next Saturday at 2pm")
2. Three task lists:
   - Weekend Tasks (active tasks for current/upcoming weekend)
   - Master List (backlog of uncompleted tasks)
   - Completed Tasks (archive)
3. Task operations: create, complete, move to master list, pull from master to weekend, delete, edit
4. Weekend is defined as Friday evening to Sunday night
5. Auto-suggest moving uncompleted tasks to master list on Monday

## Technical Requirements
- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Database: Room (local SQLite, offline-first)
- NLP: Google ML Kit for natural language understanding
- Additional NLP: Natty Date Parser or custom regex for date/time extraction
- Architecture: MVVM (Model-View-ViewModel)
- Min SDK: 24 (Android 7.0)

## Google ML Kit Integration
Use ML Kit's capabilities for:
1. **Entity Extraction** - Extract dates, times, and task descriptions from user input
2. **On-device processing** - All NLP happens locally, no internet required
3. **Language detection** - Optional: support multiple languages

Example ML Kit usage:
- Input: "Buy groceries tomorrow at 3pm"
- ML Kit extracts: entities = [date: tomorrow, time: 3pm], text: "Buy groceries"
- Output: Task{title="Buy groceries", dueDate=tomorrow 3pm, status=WEEKEND}

## Data Model
Task entity with:
- id (unique identifier, UUID)
- title (string)
- description (optional string)
- status (enum: WEEKEND, MASTER, COMPLETED)
- createdDate (timestamp)
- completedDate (optional timestamp)
- dueDate (optional timestamp, extracted from NLP)
- dueTime (optional time, extracted from NLP)
- priority (optional: LOW, MEDIUM, HIGH)

## UI Structure
1. Main screen with three tabs: Weekend, Master List, Completed
2. Floating action button to add new task
3. Task input screen with:
   - Natural language text field with hint: "e.g., Clean garage Saturday afternoon"
   - Real-time NLP parsing preview showing extracted task and date/time
   - Manual override fields if NLP parsing is incorrect
   - Save button
4. Each task card shows:
   - Task title
   - Due date and time (if set)
   - Priority indicator (color-coded)
   - Checkbox to mark complete
   - Long-press or swipe menu for: Edit, Delete, Move to Master/Weekend, Change Priority

## Key User Flows
1. **Adding a task:** 
   - User types "Paint fence next Sunday at 10am"
   - App shows preview: "Task: Paint fence | Date: Sunday [date] | Time: 10:00 AM"
   - User confirms → Saves to Weekend list
   
2. **Completing a task:** 
   - User checks task → Moves to Completed with timestamp
   
3. **End of weekend:** 
   - Monday morning: App shows notification/dialog with uncompleted weekend tasks
   - User can bulk move to Master list or delete
   
4. **Planning next weekend:** 
   - User browses Master list
   - Taps tasks to move to Weekend list
   - Can also use NLP: "Move painting to this weekend"

## Required Google Libraries

### ML Kit Dependencies:
```gradle
// Google ML Kit - Entity Extraction (for dates, times, addresses)
implementation("com.google.mlkit:entity-extraction:16.0.0-beta5")

// Optional: Smart Reply (could be useful for task suggestions)
implementation("com.google.mlkit:smart-reply:17.0.3")

// Optional: Language ID (if supporting multiple languages)
implementation("com.google.mlkit:language-id:17.0.5")
```

### Additional NLP Libraries:
```gradle
// For enhanced date/time parsing
implementation("com.joestelmach:natty:0.13")

// Or use Android's built-in DateFormat and patterns
```

### Core Android Dependencies:
```gradle
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.activity:activity-compose:1.8.2")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## NLP Implementation Details

Create a `NaturalLanguageProcessor.kt` class that:

1. **Initializes ML Kit Entity Extractor:**
```kotlin
private val entityExtractor = EntityExtraction.getClient(
    EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
        .build()
)
```

2. **Processes user input:**
```kotlin
suspend fun parseTaskInput(input: String): ParsedTask {
    // Download model if needed (first time only)
    entityExtractor.downloadModelIfNeeded()
    
    // Extract entities (dates, times)
    val entities = entityExtractor.annotate(input).await()
    
    // Parse dates and times
    val dateTime = extractDateTime(entities)
    
    // Extract task text (remove date/time words)
    val taskTitle = extractTaskTitle(input, entities)
    
    return ParsedTask(
        title = taskTitle,
        dueDate = dateTime.date,
        dueTime = dateTime.time
    )
}
```

3. **Handle various input patterns:**
- "task tomorrow" → extracts tomorrow's date
- "task next Saturday" → extracts next Saturday's date
- "task at 3pm" → extracts time
- "task Saturday 3pm" → extracts both date and time
- "task this weekend" → defaults to upcoming Saturday
- "every Saturday" → creates recurring task (optional feature)

## Important Implementation Notes

1. **ML Kit Model Download:**
   - First time app runs, ML Kit needs to download entity extraction model (~10MB)
   - Show progress indicator during download
   - Cache model for offline use afterward
   - Handle download failures gracefully

2. **Fallback Parsing:**
   - If ML Kit fails, fall back to Natty Date Parser
   - If both fail, let user manually select date/time
   - Always show parsing preview before saving

3. **Offline-First:**
   - All data stored locally in Room database
   - ML Kit models work offline after initial download
   - No internet required for app functionality

4. **Error Handling:**
   - Handle ambiguous dates ("Saturday" - which Saturday?)
   - Handle invalid input ("asdfghjkl")
   - Show helpful error messages
   - Allow manual correction

## UI/UX Requirements

1. **Task Input Screen:**
   - Large text field for natural language input
   - Real-time parsing preview below input
   - Edit buttons for parsed date/time if incorrect
   - Examples shown: "Try: 'Water plants Saturday morning'"

2. **Visual Feedback:**
   - Show loading spinner during ML Kit processing
   - Highlight extracted entities in input text
   - Color-code parsed date/time preview

3. **Accessibility:**
   - Support TalkBack
   - Proper content descriptions
   - Large touch targets (48dp minimum)

4. **Material Design 3:**
   - Use dynamic color theming
   - Smooth animations
   - Proper elevation and shadows

## Project Structure
```
com.weekendtasks.app/
├── data/
│   ├── local/
│   │   ├── TaskDatabase.kt
│   │   ├── TaskDao.kt
│   │   └── TaskEntity.kt
│   ├── repository/
│   │   └── TaskRepository.kt
│   └── model/
│       └── Task.kt
├── domain/
│   ├── usecase/
│   │   ├── AddTaskUseCase.kt
│   │   ├── CompleteTaskUseCase.kt
│   │   └── MoveTaskUseCase.kt
│   └── nlp/
│       ├── NaturalLanguageProcessor.kt
│       ├── EntityExtractor.kt
│       └── DateTimeParser.kt
├── ui/
│   ├── screens/
│   │   ├── main/
│   │   │   ├── MainScreen.kt
│   │   │   └── MainViewModel.kt
│   │   ├── addtask/
│   │   │   ├── AddTaskScreen.kt
│   │   │   └── AddTaskViewModel.kt
│   │   ├── weekend/
│   │   │   └── WeekendScreen.kt
│   │   ├── master/
│   │   │   └── MasterListScreen.kt
│   │   └── completed/
│   │       └── CompletedScreen.kt
│   ├── components/
│   │   ├── TaskCard.kt
│   │   ├── TaskInputField.kt
│   │   └── NLPParsePreview.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── navigation/
│       └── NavGraph.kt
└── MainActivity.kt
```

## Testing Scenarios

Include example test cases for NLP:
- "Clean garage" → Task without date (defaults to next weekend)
- "Clean garage tomorrow" → Task with tomorrow's date
- "Clean garage next Saturday" → Task with next Saturday's date
- "Clean garage Saturday at 2pm" → Task with date and time
- "Mow lawn every Sunday" → Recurring task
- "Fix sink this weekend" → Task for upcoming weekend
- "Buy groceries tomorrow morning" → Task with relative time

## Additional Features (Optional)

1. **Task Suggestions:**
   - Use ML Kit Smart Reply to suggest common weekend tasks
   - "You might want to: Clean garage, Mow lawn, Grocery shopping"

2. **Voice Input:**
   - Integrate Android's Speech-to-Text
   - Long-press FAB to speak task instead of typing

3. **Notifications:**
   - Remind user of weekend tasks on Friday evening
   - Remind about due tasks at specified times
   - Weekly summary notification

4. **Statistics:**
   - Track completion rate
   - Show most productive weekends
   - Display task completion trends

## Important Notes

- App must work 100% offline after initial ML Kit model download
- No cloud storage, no authentication, no internet dependency
- Gracefully handle ML Kit model download on first launch
- Provide clear feedback during model download
- Cache all ML Kit models locally
- Use modern Android best practices (Kotlin Flows, StateFlow, etc.)
- Follow Material Design 3 guidelines strictly
- Implement proper dependency injection (can use simple factory pattern or Hilt)
- Add proper logging for debugging NLP parsing
- Include error analytics (locally, no crash reporting services)

## Deliverables

Please generate a complete, production-ready Android project including:

1. Complete project structure with proper package organization
2. build.gradle.kts files with all Google ML Kit and other dependencies
3. Room database setup with entities, DAOs, and migrations
4. Repository pattern for data access
5. ViewModels with proper state management using StateFlow
6. Complete Jetpack Compose UI with Material 3
7. Full ML Kit integration for entity extraction
8. Date/time parsing with fallback mechanisms
9. Navigation between screens using Compose Navigation
10. Proper error handling and loading states
11. Unit tests for NLP parsing logic
12. README with setup instructions and ML Kit model download info

Make the code clean, well-documented, and following Android best practices. Include comments explaining ML Kit integration and NLP logic.
```

---

## Key Differences from Previous Prompt

### Google ML Kit Specific Features:

1. **Entity Extraction API** - The main ML Kit feature for your use case
   - Extracts dates, times, addresses, phone numbers, etc.
   - Works on-device (offline after model download)
   - Supports multiple languages

2. **Model Management**
   - First-time model download handling
   - Progress indication during download
   - Offline capability after download

3. **Enhanced NLP Capabilities**
   - Better date/time extraction than simple regex
   - Understands context better
   - Handles various date formats naturally

### Additional Considerations:
```
Follow-up questions to ask Claude Code after initial generation:

1. "Add progress indicator for ML Kit model download on first launch"
2. "Implement fallback to manual date picker if ML Kit parsing fails"
3. "Add voice input support using Android Speech Recognition"
4. "Show highlighted entities in the input text as they're recognized"
5. "Add support for recurring tasks (every Saturday, weekly, etc.)"
6. "Implement smart suggestions using ML Kit Smart Reply"