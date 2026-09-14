# IronMind System Map

IronMind is an Android application designed to enforce deep work sessions (Missions) through a strict, multi-layered protection system. It prevents the user from abandoning their work, blocking distracting applications and enforcing consequences for breaking the rules.

## Core Domain

*   **Task (Mission)**: The foundational unit of work. Scheduled for a specific time window.
*   **Protection Readiness**: A prerequisite state. The system must verify that all required permissions (Accessibility, Overlay, Notifications) are granted *before* a mission can start.
*   **WorkLock (Spring Protocol)**: The psychological and technical barrier preventing early exit. It imposes a simulated delay and an XP penalty if broken.
*   **Pomodoro Engine**: The timer dictating periods of focus vs. rest.
*   **FocusSession**: The foreground service and notification ensuring Android does not kill the app process while a mission is active.
*   **RuntimePolicy**: The in-memory map of what is blocked and allowed right now.
*   **IronMindAccessibilityService**: The enforcer. It reads the RuntimePolicy and physically overlays a blocking screen on top of forbidden apps.

---

## Component Breakdown

### 1. Startup & Onboarding
*   **Responsibility**: Initialize the app, check permissions, and onboard the user.
*   **Dependencies**: `PrefManager`, `PermissionHelper`.
*   **Classification**: **REQUIRED**

### 2. Mission Creation (`TaskBuilderScreen`)
*   **Responsibility**: Allow the user to define a task, its time window, and importance.
*   **Outputs**: Saves a `Task` to `IronMindDatabase`. Schedules Android Alarms via `AlarmScheduler`.
*   **Classification**: **REQUIRED**

### 3. Mission Start (`MissionExecutionService.startMission`)
*   **Responsibility**: Transition a scheduled task into an active, protected mission.
*   **Inputs**: `taskId`, `allowedPackages` (selected context apps).
*   **Outputs**: Transitions Task to `isInProgress = true`. Arms WorkLock, Pomodoro, FocusSession, and RuntimePolicy.
*   **Failure Behavior**: If runtime setup fails, rolls back the database state to prevent "zombie" active tasks with no real protection.
*   **Classification**: **REQUIRED**

### 4. Protection Readiness (`ProtectionReadinessService`)
*   **Responsibility**: Determines whether a mission can genuinely start as a protected mission (i.e. are the necessary Android permissions and services enabled?).
*   **Inputs**: System permissions state (Accessibility, Overlay, Notification).
*   **Outputs**: `ProtectionCapability` map and a `READY` or `NOT_READY` decision.
*   **Classification**: **REQUIRED**

### 5. Protection Enforcer (`IronMindAccessibilityService`)
*   **Responsibility**: Monitors foreground window changes. If a forbidden app is opened, it launches `ShieldShatterOverlay` or block screens.
*   **Inputs**: `RuntimePolicyController` broadcasts.
*   **Classification**: **REQUIRED** (The engine of the app).

### 6. WorkLock (`WorkLockManager`)
*   **Responsibility**: Enforces the "Spring Protocol". Prevents trivial exits.
*   **Persistent State**: Saved in `PrefManager` (`workLockActive`, `workLockEndsAt`).
*   **Classification**: **REQUIRED**

### 7. Pomodoro (`PomodoroEngine`)
*   **Responsibility**: Manages focus/rest cycles.
*   **Persistent State**: Saved in `PrefManager` (`pomodoroActive`, `pomodoroStage`).
*   **Classification**: **OPTIONAL** (Users can disable it in settings).

### 8. FocusSession (`FocusSessionService`)
*   **Responsibility**: Android Foreground Service. Displays the ongoing notification and prevents process death.
*   **Runtime State**: Keeps the process alive.
*   **Classification**: **REQUIRED**

### 9. RuntimePolicy (`RuntimePolicyController` & `RuntimePolicyRepository`)
*   **Responsibility**: Determines the dynamic list of blocked/allowed apps based on the active mission context.
*   **Inputs**: `AppClassificationRepository` (for system-wide apps) + Mission's `allowedPackages`.
*   **Classification**: **REQUIRED**

### 10. Completion & Failure (`MissionExecutionService`)
*   **Responsibility**: Ends the mission.
*   **Completion**: Saves `isCompleted = true`, awards XP, clears protection.
*   **Failure (Skip)**: Generates `FailureEvidence`, applies penalty, breaks streak, records failure in `FailureService`.
*   **Classification**: **REQUIRED**

### 11. Recovery & Retry (`RecoveryExecutionService`)
*   **Responsibility**: Analyzes `FailureEvidence` and creates a recovery plan. Allows restarting the exact same mission context (`MissionExecutionService.retryMission`).
*   **Classification**: **REQUIRED**

### 12. Runtime Reconciliation (`RuntimeReconciliationService`)
*   **Responsibility**: Boot receiver / app startup check. If the app process died while a mission was active, this service repairs the state (either resuming protection or failing the mission if it expired).
*   **Classification**: **REQUIRED**
