# IronMind Project Audit Checklist

This checklist converts the initial project scan into a practical cleanup plan.
Use it as the working backlog for stabilization, wiring, and quality improvements.

Status key:
- `[ ]` Not started
- `[~]` In progress
- `[x]` Done
- `[!]` Needs product decision

## 1. Stabilize Core App Flow

### Onboarding and first-run flow
- [ ] Map the intended onboarding journey from first launch to first usable home screen.
- [ ] Decide which onboarding screens are still part of the product and which are legacy.
- [x] Fix onboarding so selected intensity is actually persisted to the active `PrefManager`.
- [x] Restore `ProfileRevealScreen` as a real post-diagnostic step instead of keeping it as an orphan test screen.
- [ ] Confirm whether onboarding should include distraction setup, website setup, identity setup, and contract setup.
- [ ] Remove or wire the currently orphaned onboarding-era screens:
  - [ ] `DistractionScreen`
  - [ ] `WebsiteSelectorScreen` if it should be first-run
  - [ ] any other setup screen that exists but is only reachable later through settings
- [ ] Replace the current one-step onboarding implementation with the intended sequence, or intentionally simplify it and delete the unused pieces.
- [ ] Add acceptance criteria for onboarding:
  - [ ] language selection persists
  - [ ] diagnostic completion persists
  - [ ] intensity persists
  - [ ] onboarding completion only happens after the intended setup is complete

### Navigation architecture
- [ ] Review the app navigation structure and document the real source of truth.
- [ ] Delete or repurpose empty navigation files:
  - [x] `app/src/main/java/com/sanket_satpute_20/ironmind/navigation/IronMindNavGraph.kt`
  - [x] `app/src/main/java/com/sanket_satpute_20/ironmind/navigation/NavGraph.kt`
- [x] Decide whether `OnboardingNavHost` should stay as a wrapper or be removed.
- [ ] Audit all routes in `MainNavHost` and ensure each route has at least one legitimate entry point.
- [ ] Audit all major product screens and ensure each one is either:
  - [ ] reachable from UX
  - [ ] intentionally background/internal only
  - [ ] removed

## 2. Remove Duplicate and Confusing State Layers

### Preference management cleanup
- [x] Consolidate around a single `PrefManager`.
- [ ] Remove or migrate the legacy duplicate:
  - [x] `app/src/main/java/com/sanket_satpute_20/ironmind/utils/PrefManager.kt`
- [ ] Search for any future accidental imports of the wrong `PrefManager`.
- [ ] Write a short note in code or docs explaining where app state is stored now.

### Duplicate / stray file cleanup
- [ ] Delete the typo-path duplicate file:
  - [x] `app/src/main/java/com/sket_satpute_20/ironmind/ui/components/OrbitalTimePicker.kt`
- [ ] Scan for other typo-package or duplicate source files.
- [ ] Confirm there are no shadow classes in wrong packages that can confuse IDE search/refactors.

## 3. Reachability and Dead Code Audit

### Screens/components that appear unintegrated
- [x] Create a dead-code decision list with keep/wire/delete classifications.
- [ ] Decide whether to wire or remove `VoiceCheckInScreen`.
- [x] Decide whether to wire or remove `ProfileRevealScreen`.
- [ ] Decide whether to wire or remove `SkipResponseScreen`.
- [ ] Decide whether to wire or remove `ColdStartCountdown`.
- [ ] Decide whether to wire or remove `RescheduleBottomSheet`.
- [ ] Decide whether to wire or remove `PatternSuggestionDialog`.
- [ ] Review other helper/overlay screens for similar dead-code status.
- [x] Remove `DistractionScreen` after confirming it was superseded by `AppSelectorScreen`.

### Feature-by-feature reachability pass
- [ ] Make a feature inventory and mark each one:
  - [ ] Home and tasks
  - [ ] Focus guard / accessibility blocker
  - [ ] Sleep Lock
  - [ ] Morning Launch
  - [ ] Night Decision
  - [ ] Crucible
  - [ ] Club Chat
  - [ ] Iron Circle / social
  - [ ] Health / sleep analytics
  - [ ] Accountability
  - [ ] Artifacts / rewards
  - [ ] Boss Mode
  - [ ] Autopsy / reports
  - [ ] Detox
- [ ] For each feature, verify:
  - [ ] user entry point exists
  - [ ] state can be configured
  - [ ] happy path completes
  - [ ] user can return safely
  - [ ] background components are actually scheduled/called

## 4. Product Decisions That Need Explicit Direction

- [!] Should onboarding be minimal, or should it force the user through discipline setup before `home`?
- [!] Is intensity still a core product mechanic, or legacy copy that no longer affects behavior enough?
- [!] Should voice check-in be a real feature in the product roadmap or be removed?
- [!] Should rescheduling intelligence be a real feature or should those UI pieces be deleted?
- [!] Is Club Chat intended for production soon, or should it be hidden behind a feature flag until backend is complete?
- [!] Which features are “experimental” and should not appear in primary navigation yet?

## 5. Club Chat and Backend Readiness

### Stream Chat production readiness
- [ ] Replace development token flow with server-issued tokens.
- [ ] Deploy and configure the Firebase Cloud Function in `firebase/functions/index.js`.
- [ ] Set `STREAM_TOKEN_ENDPOINT` for app builds.
- [ ] Confirm release builds do not rely on `devToken()`.
- [ ] Add a graceful UX path when chat backend is unavailable.
- [ ] Decide whether Club Chat should be hidden until production auth is ready.

### Auth and account flow
- [ ] Review anonymous-auth-to-real-account linking flow for edge cases.
- [ ] Verify account linking does not duplicate local/cloud task data.
- [ ] Verify logout / account switch behavior, if supported.
- [ ] Confirm what happens when Firebase auth is unavailable offline.

## 6. Permission, Privacy, and Policy Review

### Manifest and policy audit
- [ ] Review and justify each high-risk permission in the manifest:
  - [ ] `QUERY_ALL_PACKAGES`
  - [ ] `PACKAGE_USAGE_STATS`
  - [ ] `SYSTEM_ALERT_WINDOW`
  - [ ] `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
  - [ ] `SEND_SMS`
  - [ ] `READ_CONTACTS`
  - [ ] `RECORD_AUDIO`
  - [ ] `WRITE_EXTERNAL_STORAGE`
  - [ ] Health Connect sleep permission
- [ ] Verify each permission has a user-facing reason and request flow.
- [ ] Identify permissions that can be removed entirely.
- [ ] Check Play Store policy risk for app visibility, accessibility, contacts, SMS, and overlay usage.

### Data protection
- [ ] Review whether `android:allowBackup="true"` is acceptable for the app’s data sensitivity.
- [ ] Review backup rules against actual stored data: identity, contacts, habits, health, task history, streaks.
- [ ] Decide whether some data should never be backed up.
- [ ] Document what user data is stored locally vs remotely.

## 7. Background Components and Reliability

### Alarm / boot / scheduler audit
- [ ] Verify `BootReceiver` restores all intended alarms and only those alarms.
- [ ] Verify exact alarms still behave correctly on target SDK 36.
- [ ] Verify Sleep Lock reschedules correctly after reboot.
- [ ] Verify Night Decision reschedules correctly after reboot.
- [ ] Verify challenge alarms reschedule correctly after reboot.
- [ ] Verify detox schedules restore correctly after reboot.
- [ ] Review whether duplicate alarms can be created.

### Lock-screen and recovery loops
- [ ] Audit all lock-style activities for accidental self-relaunch loops.
- [ ] Verify users can recover from interrupted flows without being trapped.
- [ ] Verify back button, recents exclusion, and task affinity behavior are intentional.
- [ ] Review interactions between:
  - [ ] `SleepLockActivity`
  - [ ] `BedtimeIncomingActivity`
  - [ ] `MorningLaunchActivity`
  - [ ] `MorningUnlockReviewActivity`
  - [ ] `NightDecisionActivity`
  - [ ] `WorkLockActivity`
  - [ ] `PunishmentActivity`

## 8. Feature Quality Passes

### Sleep Lock
- [ ] Trace full Sleep Lock lifecycle: setup, alarm, incoming warning, active lock, sound service, morning handoff.
- [ ] Verify emergency app handling works as intended.
- [ ] Verify notification policy changes are always restored safely.
- [ ] Verify service/activity state survives process death reasonably.

### Morning Launch
- [ ] Trace morning launch test mode versus real mode.
- [ ] Verify review screen, launch flow, and day board transitions.
- [ ] Check whether monk flow and related models are all still actively used.

### Focus / Pomodoro / Work Lock
- [ ] Verify task reminder -> WorkStart -> WorkLock -> summary flow.
- [ ] Confirm whether Pomodoro setup/chamber/summary are all intended product surfaces.
- [ ] Verify accessibility blocker interactions do not conflict with Work Lock or punishments.

### Social / contacts
- [ ] Verify contact discovery permission flow.
- [ ] Review phone normalization/hashing/privacy assumptions.
- [ ] Verify social features degrade safely when backend data is missing.

### Health features
- [ ] Verify Health Connect permission rationale flow.
- [ ] Verify health dashboard and analytics work when no data is available.
- [ ] Verify fallback behavior when Health Connect is not installed.

## 9. Code Health and Maintainability

- [ ] Audit oversized files and split where needed.
- [ ] Reduce wildcard imports and cleanup unused imports in major navigation/UI files.
- [ ] Identify god classes and managers that hold too many responsibilities.
- [ ] Standardize package ownership and naming conventions.
- [ ] Add brief code comments only where flows are non-obvious.
- [ ] Remove stale comments that describe old architecture.

## 10. Build, Repo, and Project Hygiene

- [ ] Confirm the project root should be a git repo; currently this workspace scan did not find `.git`.
- [ ] Add or verify ignore rules for build artifacts and generated files.
- [ ] Confirm local/generated files are not being treated like source of truth.
- [ ] Review whether secrets/config values should move to safer configuration channels.
- [ ] Review release readiness settings:
  - [ ] `versionCode`
  - [ ] `versionName`
  - [ ] release minification/obfuscation
  - [ ] crash logging / monitoring plan

## 11. Testing Backlog

### Immediate test gaps
- [ ] Replace boilerplate sample tests with real tests.
- [ ] Add unit tests for:
  - [ ] onboarding state decisions
  - [ ] intensity persistence
  - [ ] alarm scheduling helpers
  - [ ] permission helper logic
  - [ ] key managers with pure logic
- [ ] Add integration/instrumentation coverage for:
  - [ ] first-run flow
  - [ ] Sleep Lock activation
  - [ ] Morning Launch trigger
  - [ ] Night Decision trigger
  - [ ] Club Chat auth fallback path

### Manual QA checklist
- [ ] Fresh install walkthrough
- [ ] Reboot device and verify restored alarms
- [ ] Revoke critical permissions and verify recovery UX
- [ ] Test offline mode
- [ ] Test account link flow
- [ ] Test one full day lifecycle:
  - [ ] task reminders
  - [ ] focus session
  - [ ] night decision
  - [ ] sleep lock
  - [ ] morning launch

## 12. Recommended Execution Order

### Phase 1: Safe cleanup
- [x] Delete duplicate files and empty nav placeholders.
- [x] Consolidate `PrefManager`.
- [x] Mark dead/unwired features clearly.

### Phase 2: Core product flow
- [~] Fix onboarding flow and persistence.
- [~] Audit route reachability.
- [ ] Stabilize Sleep Lock, Morning Launch, Night Decision, and Work Lock handoffs.

### Phase 3: Backend and policy
- [ ] Finish Club Chat server token setup.
- [ ] Review manifest permissions and privacy posture.
- [ ] Decide which experimental features stay.

### Phase 4: Quality
- [ ] Add tests around the most fragile flows.
- [ ] Do a full regression pass after cleanup.

## 13. First Suggested Work Items

If we want the best momentum, start here:

- [ ] Task 1: Remove duplicate `PrefManager` and duplicate `OrbitalTimePicker` residue.
- [ ] Task 2: Fix onboarding so intensity is saved and the intended first-run flow is explicit.
- [ ] Task 3: Produce a dead-code decision list for unintegrated screens/components.
- [ ] Task 4: Review Sleep Lock end-to-end because it is central and state-heavy.
- [ ] Task 5: Decide whether Club Chat is production-blocked or hidden behind a feature gate.
