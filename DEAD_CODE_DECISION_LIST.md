# IronMind Dead Code Decision List

This document classifies weakly integrated files and features found during the reachability audit.

Decision labels:
- `DELETE` = safe candidate for removal because it is unused, redundant, or replaced
- `KEEP_INTERNAL` = not directly routed, but still legitimately used as an internal component
- `WIRE_OR_DELETE` = feature slice exists but is not connected to the current product flow
- `KEEP_FOR_NOW` = low value to remove immediately, but not a priority risk

## 1. Delete

### Empty / abandoned navigation placeholders

- `DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/navigation/IronMindNavGraph.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/navigation/IronMindNavGraph.kt)
  Reason: zero-byte file, no implementation, no callers.
  Status: deleted

- `DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/navigation/NavGraph.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/navigation/NavGraph.kt)
  Reason: zero-byte file, no implementation, no callers.
  Status: deleted

### Redundant wrapper / legacy type

- `DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/navigation/OnboardingNavHost.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/navigation/OnboardingNavHost.kt)
  Reason: only forwards directly to `OnboardingRoot` and is not referenced anywhere else.
  Status: deleted

- `DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/psychology/AppScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/psychology/AppScreen.kt)
  Reason: enum is not used anywhere in current navigation or screen orchestration.
  Status: deleted

### Superseded onboarding screen

- `DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/DistractionScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/DistractionScreen.kt)
  Reason: no callers, no route, and the current distraction setup flow is handled by `AppSelectorScreen`.
  Status: deleted

## 2. Keep Internal

### Subscreens that are used indirectly

- `KEEP_INTERNAL` [`app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/WebsiteSelectorScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/WebsiteSelectorScreen.kt)
  Reason: not directly routed, but it is actively launched from `AppSelectorScreen` as an internal subflow for blocked websites.

- `KEEP_INTERNAL` [`app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionsScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionsScreen.kt)
  Reason: `PermissionsCheckBanner` is used from `HomeScreen`, so this file is not dead code even though it has no nav route.

## 3. Wire Or Delete

These are not simple garbage files. They look like unfinished or disconnected feature slices.

### Voice check-in

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceCheckInScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceCheckInScreen.kt)
  Reason: the screen records and stores `VoiceLog` data, and `VoiceLogsScreen` is already reachable from the app. That suggests the product intended users to create voice entries, but there is no entry point into the recorder itself.
  Recommendation: wire from `HomeScreen`, `StatsScreen`, or a task reflection flow if voice journaling is still strategic. Otherwise remove the feature slice cleanly.

### Diagnostic reveal flow

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/psychology/ProfileRevealScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/psychology/ProfileRevealScreen.kt)
  Reason: `DiagnosticScreen` completes with a `UserProfile`, which makes this screen look like a previously intended reveal step, but the current flow skips straight onward.
  Recommendation: either insert it after diagnostic completion or remove it to keep the psychology flow honest.
  Status: wired after diagnostic completion and before onboarding.

### Skip consequence UX

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/psychology/SkipResponseScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/psychology/SkipResponseScreen.kt)
  Reason: the supporting models and copy still exist (`SkipResponse`, `SkipResponseData`, adaptive skip behavior), but there is no current caller into the UI.
  Recommendation: trace current task-skip handling. If the product wants adaptive post-skip experiences, wire this screen into that flow. If not, remove the entire skip-response UI slice together.

### Cold-start ceremony

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/coldstart/ColdStartCountdown.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/coldstart/ColdStartCountdown.kt)
  Reason: no callers at all. This looks like a ceremonial start animation for a focus/task experience that never got integrated.
  Recommendation: wire into `WorkStartActivity` or Pomodoro launch only if that ritual is still product-relevant.

### Rescheduling feature slice

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/RescheduleBottomSheet.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/RescheduleBottomSheet.kt)
  Reason: no callers. Full UI exists.

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/PatternSuggestionDialog.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/PatternSuggestionDialog.kt)
  Reason: no callers. Suggestion UI exists.

- `WIRE_OR_DELETE` [`app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/RescheduleManager.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/scheduling/RescheduleManager.kt)
  Reason: underlying logic exists, but there is no connected user flow using it. This is a coherent but dormant mini-feature.
  Recommendation: treat these three files as one decision. Either wire the whole reschedule experience into task management, or remove the slice together.

## 4. Keep For Now

These are weakly justified architecturally, but not urgent removal targets.

- `KEEP_FOR_NOW` [`app/src/main/java/com/sanket_satpute_20/ironmind/navigation/OnboardingRoot.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/navigation/OnboardingRoot.kt)
  Reason: current onboarding is too thin, but the file is active and should stay until the onboarding redesign is done.

- `KEEP_FOR_NOW` [`app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/AppSelectorScreen.kt`](/c:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/AppSelectorScreen.kt)
  Reason: this file is large and multi-purpose, but it is actively used and appears to have replaced older setup screens.

## 5. Recommended Execution Order

### Safe deletes first

1. Delete empty nav files.
2. Delete `OnboardingNavHost`.
3. Delete `AppScreen`.
4. Delete `DistractionScreen`.

### Product decision group

1. Decide the future of voice check-in.
2. Decide whether diagnostic should include `ProfileRevealScreen`.
3. Decide whether adaptive skip-response UX is part of the product.
4. Decide whether task rescheduling is an active feature or backlog residue.

### Important note

Not every file without a route is dead code.
Some files are internal subflows or overlays and should not be deleted just because they are not in `MainNavHost`.
