# CampusConnectPlus – Midterm Checklist (Filled from Codebase Review)

**Date:** March 4, 2026

---

## III. Current Development Status (Check all that apply)

- [x] **App concept finalized**
- [x] **Architecture planned (MVVM)**
- [x] **Local database implemented**
- [x] **Core UI screens completed**
- [x] **Features partially working**
- [x] **Testing in progress** *(ViewModel unit tests: StudentEventsViewModel, AdminEventsViewModel; instrumented UI/navigation test; Fake repos)*

**Overall Progress Status:**

- [x] **On Track**
- [ ] Slightly Delayed
- [ ] Needs Instructor Support

---

## IV. Feature Implementation Checklist (Midterm Scope)

### A. Architecture & Structure

| Feature | Status |
|--------|--------|
| MVVM architecture implemented | **☑ Done** ☐ Partial ☐ Not Started |
| Repository pattern used | **☑ Done** ☐ Partial ☐ Not Started |
| ViewModel for UI logic | **☑ Done** ☐ Partial ☐ Not Started |
| Clean separation of UI & data | **☑ Done** ☐ Partial ☐ Not Started |

**Evidence:** `AppContainer` + repository interfaces; ViewModels in `feature_admin` (Auth, Dashboard, Events, Media, Announcements, Users) and `feature_student` (Home, Events, Media, Saved, Announcements); UI in `ui/student` and `ui/admin`; data in `data/repository`, `data/local` (Room DAOs, entities, Room*Repository).

---

### B. Local Data Persistence (Room)

| Feature | Status |
|--------|--------|
| Room Database configured | **☑ Done** ☐ Partial ☐ Not Started |
| Entity classes created | **☑ Done** ☐ Partial ☐ Not Started |
| DAO with CRUD operations | **☑ Done** ☐ Partial ☐ Not Started |
| Data persists after app restart | **☑ Done** ☐ Partial ☐ Not Started |

**Evidence:** `AppDatabase` (entities: Event, Media, Announcement, User, Favorite); `DatabaseProvider` with `campusconnectplus.db`; DAOs with `@Query` (observe), `@Upsert`, `@Query` (delete); `ServiceLocatorSeed` runs once and writes to Room (data survives restarts).

---

### C. Jetpack Compose UI

| Feature | Status |
|--------|--------|
| Main screen implemented | **☑ Done** ☐ Partial ☐ Not Started |
| Secondary screens implemented | **☑ Done** ☐ Partial ☐ Not Started |
| Navigation between screens | **☑ Done** ☐ Partial ☐ Not Started |
| Material Design components used | **☑ Done** ☐ Partial ☐ Not Started |

**Evidence:** `MainActivity` uses single `NavHost`. **Student:** Home, Events, Media, Saved, and **student/announcements** (read-only announcements from Home quick link). **Admin:** Login → role-based redirect; Dashboard, Events, Media, Announcements, Users, Settings with **role-based bottom tabs** (`tabsForRole`: ADMIN = all 6 tabs; ORGANIZER = Events, Announcements, Settings; MEDIA_TEAM = Media, Settings; else = Settings). Material3 throughout (`MaterialTheme`, `NavigationBar`, `Card`, `OutlinedTextField`, `FilterChip`, `AssistChip`, dialogs, etc.).

---

### D. State & User Interaction

| Feature | Status |
|--------|--------|
| State-based UI (State, Flow, StateFlow, remember, mutableStateOf) | **☑ Done** ☐ Partial ☐ Not Started |
| User input handled correctly | **☑ Done** ☐ Partial ☐ Not Started |
| Empty/loading states handled | **☑ Done** ☐ Partial ☐ Not Started |

**Evidence:** ViewModels use `StateFlow` + `stateIn()`; screens use `collectAsState()`, `remember`, `mutableStateOf`; search/filter/favorites and admin forms handle input. Empty states: `EmptyAdminPanel`, “No events/media/announcements available”, `EmptyState` on Saved. Loading: `StudentEventsScreen` uses `UiState` (Loading/Success/Error) with `CircularProgressIndicator`; refresh shows loading state. **All interactive controls wired:** notifications (Toast), View Details (navigate to Events), Export/Reset (snackbar + confirmation dialog), Filter (snackbar), **full Edit dialogs** for Admin Events and Announcements (pre-filled, update via ViewModel upsert), Create/Delete/Activate/Deactivate/Delete account (full behavior).

---

### E. Early MCO 2 Preparation (Optional)

| Planning Item | Status |
|---------------|--------|
| Cloud/API integration planned | **☑ Yes** ☐ No |
| Offline-first sync strategy planned | **☑ Yes** ☐ No |
| Animation / Canvas UI idea | **☑ Yes** ☐ No |

**Evidence:** README recommends “Backend Integration (Firebase or REST API)” and “synchronize data”; app is already offline-first (Room + local repos). **Canvas:** `StatRingCanvas` (core/ui/components)—custom-drawn engagement ring on Student Home. **Animation:** NavHost enter/exit transitions for admin/login; Event Highlights carousel on Home.

---

## V. Demonstration Readiness

- [x] **App launches without crash**
- [x] **Data can be added and retrieved** *(seed + repository upsert/observe; Room persistence)*
- [x] **UI responds to user actions** *(navigation, search, filter, favorites, admin CRUD; role-based admin tabs; delete user with confirmation; student announcements; all buttons give feedback)*
- [x] **Group can explain architecture and database** *(MVVM, repositories, Room, DAOs, entities, tabsForRole)*

---

## Summary & Suggested Next Steps

- **Strong:** MVVM, Room (entities + DAOs + persistence), Compose navigation and screens, Material3, StateFlow/Compose state, empty/loading handling, role-based admin access, student read-only announcements, user management (create, role, activate, delete account), **full Edit flows** for Admin Events and Announcements (edit dialogs with pre-filled data and upsert), and consistent button/UX feedback.
- **Testing:** Unit tests: `StudentEventsViewModelTest` (events state, favorites, snackbar), `AdminEventsViewModelTest` (events flow, delete, upsert create/update, snackbar). Instrumented: `MainActivityNavigationTest` (launches MainActivity, asserts “CampusConnect+” and “Welcome back, Student!” visible). Run with Android Studio: *Run → Run Tests* or `./gradlew :app:testDebugUnitTest` and `./gradlew :app:connectedDebugAndroidTest`.
- **Optional next:** More ViewModel or repository unit tests; additional UI tests for navigation (e.g. tap Events tab, assert list or empty state).

Use this file as the filled midterm checklist and adjust checkboxes if your instructor uses different criteria.
