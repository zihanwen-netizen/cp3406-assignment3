# StudyLab

StudyLab is a CP3406 educational app for university learners. It uses short multiple-choice retrieval-practice sessions to help students check recall in Computer Science, Science and Nature, and General Knowledge. The home screen also lets a learner save a study-place name manually; it does not show maps or live space availability.

## Core screens

- **Home:** Start a five-question session, review a small progress summary, and save a study-place label locally.
- **Practice:** Fetch questions on demand, select an answer, review immediate feedback, and finish the session.
- **Progress:** View local session history, questions attempted, correct answers, and aggregate accuracy.
- **Settings:** Choose a topic and difficulty or turn on larger text.

## Architecture

- Kotlin, Jetpack Compose and Material 3
- Navigation Compose for the four core destinations
- `StudyLabViewModel` owns UI state and coordinates user actions
- `StudyRepository` separates remote questions and local persistence from the UI
- Retrofit calls the Open Trivia Database API
- Room stores session totals, preferences, and the manually entered study-place label
- `AppContainer` provides manual dependency injection
- `QuizRules` holds pure scoring logic with JVM unit tests

## Data and privacy choices

StudyLab does not create accounts, request location, or include analytics SDKs. The only manifest permission is Internet access for retrieving quiz questions. Preferences, the study-place label, and session totals stay in the Room database on the device. Android app backup is disabled so those records are not included in device backup by this app. The user can clear them by clearing app storage in Android settings.

The study-place field is a private note, not a location search or campus directory. The app provides no live availability and asks the learner to check campus notices before travelling. If a future version adds a location-based shortcut, it should be optional, foreground-only, explained in context, and leave manual entry available after denial.

Quiz items come from the user-contributed Open Trivia Database API. The database describes its content as CC BY-SA 4.0. Questions are not a verified JCU syllabus resource; students should check important concepts against their unit material. Source: <https://opentdb.com/api_config.php>.

## Build and test

1. Open this folder in Android Studio and allow Gradle sync.
2. Run the `app` configuration on an emulator or Android device.
3. Use a network connection to load a question set.
4. Run JVM unit tests with `./gradlew test`. Run Compose navigation and Room persistence instrumentation tests on an emulator with `./gradlew connectedDebugAndroidTest`.

The Android SDK location is machine-specific; create `local.properties` on the development machine rather than committing it.

## Assessment scope

This project targets CP3406. It includes external API access, Room persistence, four Compose screens, modern navigation, repository/ViewModel separation, and non-GUI unit tests. CP5307-only WorkManager and runtime location permissions are not included. The app deliberately avoids location access because the core learning and study-planning tasks do not require it.
