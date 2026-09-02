# Sleep Lock Audit

This audit traces Sleep Lock end-to-end:

- setup UI
- persisted state
- alarm scheduling
- warning handoff
- active lock experience
- sound service
- notification policy handling
- boot recovery
- accessibility enforcement

## Overall Assessment

Sleep Lock is one of the more complete features in the app.
It has a real setup flow, alarm scheduling, warning handoff, active enforcement, emergency access, sound playback, boot restoration, and accessibility-service integration.

The main risks are not “feature missing entirely.”
They are consistency and recovery issues:

- UI and runtime behavior are not fully aligned
- some persisted fields are effectively unused
- a couple of flows silently no-op instead of clearly informing the user
- the sound download state can get stuck

## What Is Working Well

- Setup flow is substantial and user-facing:
  [`SleepLockSetupScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockSetupScreen.kt)

- State is persisted in a dedicated section of `PrefManager`:
  [`PrefManager.kt:341`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/data/PrefManager.kt#L341)

- Save logic schedules/cancels alarms and restores policy on disable:
  [`SleepLockViewModel.kt:129`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L129)

- Alarm scheduler cleanly separates warnings, start, and end:
  [`SleepLockAlarmScheduler.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockAlarmScheduler.kt)

- Receiver activates DND, starts the active screen, and restores state at end:
  [`SleepLockAlarmReceiver.kt:80`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockAlarmReceiver.kt#L80)

- Accessibility service respects Sleep Lock emergency allowlist:
  [`IronMindAccessibilityService.kt:184`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/blocker/IronMindAccessibilityService.kt#L184)

- Boot restore exists:
  [`BootReceiver.kt:50`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/alarm/BootReceiver.kt#L50)

## Findings

### 1. Hidden 20-minute warning path does not match setup UI

Severity: High

The setup screen presents only three warning controls:

- 30-minute warning
- 15-minute warning
- final warning

See:
[`SleepLockSetupScreen.kt:201`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockSetupScreen.kt#L201)

But the runtime logic always includes a separate 20-minute stage:

- stage calculation:
  [`SleepLockManager.kt:47`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockManager.kt#L47)
- scheduling:
  [`SleepLockManager.kt:90`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockManager.kt#L90)
- receiver launch:
  [`SleepLockAlarmReceiver.kt:45`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockAlarmReceiver.kt#L45)

Impact:

- users cannot configure or even see the 20-minute takeover behavior in setup
- product expectations and runtime behavior diverge
- audit/debugging becomes harder because “warning stages” are not fully represented in UI

Recommendation:

- either make the 20-minute handoff explicit in the UI
- or merge it into the 15/final flow and remove the hidden stage

### 2. Notification silence can appear enabled even when the app cannot actually apply it

Severity: High

The setup UI lets the user toggle “Silence normal notifications” regardless of DND access:
[`SleepLockSetupScreen.kt:240`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockSetupScreen.kt#L240)

But the policy manager only activates silence if DND permission is granted:
[`SleepLockPolicyManager.kt:16`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockPolicyManager.kt#L16)

Impact:

- the setting can be saved in a state the app cannot honor
- users may believe Sleep Lock will mute notifications when it actually will not
- this is especially risky for an overnight enforcement feature

Recommendation:

- gate the toggle behind DND access
- or allow the toggle but surface a persistent “will not apply until access is granted” state

### 3. Sound download state can get stuck in `downloading`

Severity: Medium

When a sound download starts, the state is set to `downloading = true`:
[`SleepLockViewModel.kt:109`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L109)

After download returns, the view model rebuilds state from previous UI state:
[`SleepLockViewModel.kt:116`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L116)

And the rebuilt state preserves `downloading` from the previous item:
[`SleepLockViewModel.kt:223`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L223)

Impact:

- failed downloads have no visible reset path
- UI can stay stuck in a loading state
- there is no surfaced error/result handling for network failures

Recommendation:

- make `downloadSound()` handle success/failure explicitly
- reset `downloading` after completion
- expose an error message when the download fails

### 4. Several persisted Sleep Lock fields are effectively unused or weakly justified

Severity: Medium

Sleep Lock stores a fairly large state surface:
[`PrefManager.kt:401`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/data/PrefManager.kt#L401)

These fields appear weakly integrated:

- `sleepLockDndPermissionAcknowledged`
- `sleepLockBypassUsedAt`
- `sleepLockOnboardingComplete`

`sleepLockOnboardingComplete` is written in save logic:
[`SleepLockViewModel.kt:150`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L150)

and read back into UI state:
[`SleepLockViewModel.kt:206`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockViewModel.kt#L206)

but it does not currently drive setup flow, prompting, or gating.

Impact:

- raises maintenance cost
- suggests unfinished sub-features
- makes future state migrations harder

Recommendation:

- either wire these fields into real behavior
- or remove them before more Sleep Lock complexity accumulates

### 5. Boot recovery restores policy and alarms, but not the active Sleep Lock surface

Severity: Medium

On boot, if Sleep Lock is enabled:

- alarms are rescheduled
- DND policy is restored if active

See:
[`BootReceiver.kt:50`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/alarm/BootReceiver.kt#L50)

What does not happen on boot:

- `SleepLockActivity` is not relaunched
- `BedtimeIncomingActivity` is not restored

Impact:

- after reboot during an active sleep window, policy may be restored but the user may not return to the overnight lock surface automatically
- the feature can recover into a “partially active” state

Recommendation:

- define intended reboot behavior explicitly
- if Sleep Lock should remain visible after reboot during active window, relaunch the active activity
- if not, document that the accessibility service becomes the primary guard after reboot

### 6. “Finish now” from bedtime warning does not route into a focused finish flow

Severity: Medium

At 20 minutes before bedtime, the user can choose `FINISH NOW`, which simply opens `MainActivity`:
[`BedtimeIncomingActivity.kt:121`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/BedtimeIncomingActivity.kt#L121)

Impact:

- the CTA sounds like it will take the user into a focused completion path
- in practice it just returns to the general app shell
- this is weaker than the copy suggests for a high-stakes pre-bedtime moment

Recommendation:

- decide what “finish now” should mean
- likely options:
  - route to `home`
  - route to today’s pending task view
  - route to a condensed “night wrap-up” flow

### 7. External sound hosting is operationally fragile

Severity: Low

Sleep sounds are downloaded from third-party public URLs:
[`SleepLockSoundCatalog.kt:10`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockSoundCatalog.kt#L10)

Impact:

- downloads can break if those URLs move or rate-limit
- no integrity or availability guarantees
- weak offline reliability for a core night feature

Recommendation:

- move these assets into app-bundled/raw assets or a controlled backend/storage path

## Recovery / State Notes

- The accessibility service is a strong backstop once `sleepLockActive` is set:
  [`IronMindAccessibilityService.kt:184`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/blocker/IronMindAccessibilityService.kt#L184)

- `SleepLockActivity` reopens itself when the user leaves unless the app intentionally launched an emergency app:
  [`SleepLockActivity.kt:141`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/SleepLockActivity.kt#L141)

- The bedtime incoming screen also aggressively relaunches if left prematurely:
  [`BedtimeIncomingActivity.kt:160`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/sleeplock/BedtimeIncomingActivity.kt#L160)

These are good enforcement behaviors, but they make correctness of state restoration even more important.

## Recommended Fix Order

### Phase 1: User-trust fixes

1. Align the warning model with the UI:
   remove or expose the 20-minute stage.
2. Fix notification-silence UX so the toggle cannot promise behavior the app cannot perform.
3. Fix sound download state and failure handling.

### Phase 2: State cleanup

1. Remove or wire unused Sleep Lock prefs.
2. Decide whether Sleep Lock has a real onboarding concept.
3. Simplify warning-stage bookkeeping if it is only for analytics/history.

### Phase 3: Recovery behavior

1. Define reboot-during-active-window behavior.
2. Decide whether boot should relaunch the active lock surface.
3. Make “finish now” route into a more focused bedtime action path.

## Suggested Next Implementation Task

Best next engineering task:

- fix the hidden 20-minute warning mismatch and make the warning model explicit in one place

That gives the biggest clarity win with relatively low code risk, because it touches setup, runtime stages, and user expectations at once.

