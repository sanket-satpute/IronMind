# IronMind Product Scope And Implementation Plan

Current product snapshot:

IronMind is being shaped into a proof-based discipline operating system. Its core job is to help people keep promises, protect focus, reduce phone waste, verify real execution, and recover with consequences when discipline breaks.

The current version should focus on one strong loop:

1. Plan a clear mission.
2. Lock distractions during the mission.
3. Capture mental distractions without leaving work.
4. Verify whether the promise was actually completed.
5. Reward clean execution or force recovery after failure.
6. Review the day honestly and adjust tomorrow.

This document separates:

- features that already exist but should be hidden or frozen for the current version
- important features that are partially built, buggy, or incomplete
- product plans for implementing the important features correctly

## 1. Current Version Product Rule

For this version, the app should not feel like a social network, game collection, analytics lab, or experimental psychology playground.

It should feel like:

- a mission planner
- a focus shield
- a proof checker
- a consequence and recovery system
- a daily integrity mirror

Any feature that does not strengthen this loop should be hidden from normal users for now. We are not deleting those features permanently. We are freezing them so they do not distract the product, confuse users, or create unfinished paths.

## 2. Features To Hide Or Freeze For Current Version

These features are not necessarily bad. They are just not core to the current mission. They can come back later if the main product loop becomes strong.

### 2.1 Club Chat

Current state:

- Exists as a 5 AM Club / chat feature.
- Depends on Stream Chat and Firebase token flow.
- `STREAM_TOKEN_ENDPOINT` is currently empty in build config.

Why it is not current-version core:

- The main product is personal execution, not community.
- Chat adds backend, auth, moderation, and reliability risk.
- A broken chat feature makes the app feel unfinished.

Decision:

- Hide from primary navigation and home surfaces.
- Keep code for future use.
- Do not promote chat until token backend, moderation model, and product role are clear.

Future condition to restore:

- Bring back only after the personal discipline loop works.
- Reintroduce as an accountability/community layer, not as a distraction.

### 2.2 Iron Circle, Contact Discovery, Friend Compare

Current state:

- Social profile, contact discovery, Firebase social repository, network screens, friend comparison exist.
- Requires contact permission and stores/uses social/accountability data.

Why it is not current-version core:

- Social comparison is not necessary for proof-based discipline.
- Contact permission increases privacy and Play Store policy sensitivity.
- It adds emotional/social pressure before the personal system is stable.

Decision:

- Hide Iron Circle, contact discovery, network, and friend compare from current user-facing navigation.
- Keep code as a future social/accountability module.

Future condition to restore:

- Reintroduce only when accountability partner workflows are stable and privacy copy is strong.

### 2.3 Shareable Cards

Current state:

- Generates shareable milestone/streak cards.

Why it is not current-version core:

- Public sharing does not help the user keep today's promise.
- It can make the product feel performative before the execution system is trustworthy.

Decision:

- Hide from the main experience.
- Keep as future growth/retention feature.

Future condition to restore:

- Restore after verified streaks exist, so shared progress means something real.

### 2.4 Artifacts And Relic Collection

Current state:

- Artifact unlocks and collection screens exist.

Why it is not current-version core:

- Gamification is helpful only after the core behavior loop is clean.
- Too many collectible/reward systems can make the app feel game-like instead of serious.

Decision:

- Hide artifact collection from primary surfaces.
- Keep simple XP, streak, and integrity score.

Future condition to restore:

- Restore when artifact unlocks are tied to verified proof, not only manual completion.

### 2.5 Heavy Psychology Persona Surface

Current state:

- Diagnostic, profile reveal, mode switcher, manual type, weekly check-in, adaptive copy, user types.

Why it is only partly core:

- Adaptive strictness is valuable.
- Too much persona UI can confuse the user before they understand the app.

Decision:

- Keep simple mode concept: Soft, Discipline, Iron.
- Hide or reduce advanced persona/profile screens from the main flow.
- Keep diagnostic only if it directly sets strictness and recovery style.

Future condition to restore:

- Restore deeper psychology after core proof/recovery system is working.

### 2.6 Confidence Ignition And Voice Motivation

Current state:

- Confidence ignition activity/history and voice check-in/logs exist.
- Voice "AI" appears keyword-based, not real AI verification.

Why it is not current-version core:

- It is motivational, but not essential to mission proof.
- It can become useful only if tied to focus start, recovery, or proof reflection.

Decision:

- Hide as standalone feature.
- Keep code for future recovery/reflection modules.

Future condition to restore:

- Reintroduce as "pre-mission ignition" or "failure recovery reflection," not as a separate area.

### 2.7 Spring Dedicated Device / Kiosk Mode

Current state:

- Device-admin and dedicated-device management exists.
- Strong lock-task/kiosk behavior is possible on provisioned devices.

Why it is not current-version core:

- It is too advanced for normal users.
- It can be risky if surfaced casually.
- Requires special device-owner setup and clear escape/recovery rules.

Decision:

- Hide from normal settings.
- Keep as experimental developer/admin feature.

Future condition to restore:

- Restore only as an advanced mode for spare/test devices with strong warnings.

### 2.8 Boss Mode

Current state:

- Boss Mode increases difficulty after perfect performance.

Why it is not current-version core:

- It depends on reliable task completion measurement.
- Before proof verification, "perfect week" can be fake or manual.

Decision:

- Hide user-facing Boss Mode.
- Keep engine dormant until verified execution exists.

Future condition to restore:

- Reintroduce after tasks can be verified through timer/app/GPS/health proof.

### 2.9 Complex Report/Analytics Surfaces

Current state:

- Stats, review console, monthly audit, autopsy, health dashboard, weekly report.

Why it is only partly core:

- Daily review is core.
- Too many analytics surfaces are not core for first usable version.

Decision:

- Keep one simple Daily Integrity Review.
- Hide deep monthly/weekly/advanced analytics until the base loop is stable.

Future condition to restore:

- Reintroduce as advanced insights after integrity data is trustworthy.

## 3. Important But Partially Built Or Not Fully Working

These are the features that matter most for the main product, but need design and engineering work.

## 4. Feature Plan: Mission Builder With Proof Type

### Problem It Solves

Users make vague promises:

- "I will study."
- "I will run."
- "I will use phone less."

Vague promises are easy to avoid and impossible to verify. The app needs to convert promises into measurable missions.

### Product Goal

Every task should become a mission with:

- what will be done
- when it starts and ends
- what proof is required
- which apps are allowed
- what counts as failure
- what recovery/punishment applies

### Current State

Tasks already support:

- name
- start/end time
- date
- completion/skipped flags
- task type
- focus mode
- app package name
- focus score
- completion source

Missing:

- proof type
- proof status
- proof data
- failure consequence
- recovery requirement

### Screens Involved

- Task Builder screen
- Home mission cards
- Work Start screen
- Work Lock / Pomodoro screens
- Daily review screen

### Implementation Plan

Phase 1:

- Add mission proof fields to the task model or a separate `MissionProof` table.
- Add proof options in Task Builder:
  - Manual check
  - Focus timer
  - Clean app usage
  - Health proof
  - GPS run
  - Photo proof
- For current version, enable only:
  - Manual check
  - Focus timer
  - Clean app usage
- Show disabled upcoming proof types with "coming later" only in internal/dev mode, not to normal users.

Phase 2:

- Add consequence selection:
  - soft reflection
  - entertainment lock
  - recovery task
  - accountability alert
- Store chosen consequence with mission.

Phase 3:

- Use proof status on Home:
  - Pending
  - Active
  - Verified
  - Failed
  - Needs recovery

Success criteria:

- User cannot create an unclear mission without time and completion expectation.
- Completion can be verified by at least one proof rule.
- Home clearly shows whether mission is truly done or only self-reported.

## 5. Feature Plan: Focus Shield And App Allowlist

### Problem It Solves

People open distracting apps during work almost automatically. They also misuse "productive" apps out of context, like opening YouTube for entertainment during study.

### Product Goal

During a mission, the phone should only allow what the mission needs.

### Current State

Strong foundations already exist:

- Accessibility service detects app/window changes.
- Apps are classified as Void, Signal, Tool, Context.
- Blocked apps can be intercepted.
- Websites can be blocked in browsers.
- Active mission context apps are supported.

Missing or weak:

- The setup is not forced early enough.
- Context apps need clearer per-mission selection.
- Unknown apps need a review flow that does not overwhelm users.

### Screens Involved

- Onboarding
- App Selector / Distractions setup
- Task Builder
- Work Start screen
- Focus breach screen
- Permission Center

### Implementation Plan

Phase 1:

- Move app classification setup into first-run or first mission setup.
- Ask the user to classify top installed apps:
  - Always block
  - Allow only when mission needs it
  - Always allow
- Keep emergency apps separate.

Phase 2:

- In Task Builder, ask "Which apps are allowed for this mission?"
- For context apps, require explicit mission-level approval.
- During active mission, block all context apps not selected.

Phase 3:

- When an unknown app is opened during focus:
  - block it first if strict mode
  - add it to review queue
  - ask later whether it is tool/context/distraction

Success criteria:

- A user can trust that distractions are blocked during work.
- YouTube/Chrome/ChatGPT can be allowed for one mission and blocked for another.
- Unknown apps do not silently bypass the system.

## 6. Feature Plan: Punishment And Recovery For Failed Promises

### Problem It Solves

Currently, users can skip or fake-complete tasks too easily. Punishment exists, but it mainly triggers from blocked-app breaches, not from broken promises.

### Product Goal

If a user skips, fails proof, or misses a mission window, the app should force a recovery path.

The goal is not cruelty. The goal is repair:

- acknowledge failure
- block easy dopamine
- complete a recovery action
- return to the day

### Current State

Already exists:

- Punishment activity
- Punishment foreground service
- Lockout timer
- Reflection/breathing tasks
- Work Lock penalty
- XP/streak penalty on skip
- Emergency apps

Missing:

- Task skip does not clearly trigger punishment/recovery.
- Failed proof does not exist yet.
- Recovery task is not connected to unlocking entertainment.
- Accountability SMS exists but appears not wired into skip flow.

### Screens Involved

- Home mission cards
- Punishment Activity
- Focus Breach Activity
- Emergency Cooldown
- Daily Review
- Accountability setup

### Implementation Plan

Phase 1:

- Create a unified `FailureEvent` concept:
  - skipped mission
  - missed mission window
  - failed proof
  - blocked app breach
  - quit work lock
- Store failure event in database.

Phase 2:

- When a mission is skipped, route user into a recovery screen instead of silently marking skip.
- Recovery options:
  - do a smaller version of the mission
  - complete replacement productive task
  - timed reflection
  - breathing reset
- Lock entertainment apps until recovery is complete in Discipline/Iron modes.

Phase 3:

- Wire `AccountabilityManager.sendSkipAlert()` when:
  - user opted in
  - mode allows it
  - skip/failure is confirmed

Phase 4:

- Make punishment duration based on:
  - strictness mode
  - task importance
  - repeated failures
  - whether user recovered today

Success criteria:

- Skipping is no longer frictionless.
- User always knows how to repair the day.
- Punishment never blocks emergency access.
- Failure creates recovery, not only shame.

## 7. Feature Plan: Proof Engine

### Problem It Solves

Manual "Done" is easy to fake. The app's long-term value depends on verifying whether the user actually did what they promised.

### Product Goal

Build a proof engine that supports different proof types per mission.

### Current State

Available signals:

- Task completion source
- Focus sessions
- App breach logs
- Health Connect sleep
- Usage stats
- Voice logs

Missing:

- Proof type model
- Proof verification flow
- GPS/location proof
- steps/exercise/distance Health Connect proof
- photo proof
- AI proof
- suspicious/fake-proof handling

### Screens Involved

- Task Builder
- Work Start screen
- Mission completion screen
- Home mission cards
- Daily Review
- Health permission screen
- Future Proof Review screen

### Implementation Plan

Phase 1: Proof v1

- Manual proof:
  - User marks done.
  - Lowest trust.
- Timer proof:
  - User stayed in focus mode for required duration.
- Clean app proof:
  - No blocked app breach during mission.

Phase 2: Health proof

- Add Health Connect permissions for:
  - steps
  - distance
  - exercise sessions
  - active calories if useful
- Verify walking/running missions from Health Connect first.
- Health Connect is less battery-heavy and more privacy-safe than constant GPS.

Phase 3: GPS proof

- Add location permission only when needed.
- Start location tracking only during run/walk mission.
- Verify:
  - minimum distance
  - believable speed
  - start/end time inside mission window
  - movement continuity
- Store summary, not full raw path unless necessary.

Phase 4: Photo proof

- Let user submit photo from camera after mission.
- Store timestamp and proof type.
- Start with human/self-review only.

Phase 5: AI proof

- Use AI only after photo proof is stable.
- AI should classify whether proof matches the task, with uncertainty.
- Never treat AI as perfect truth. Use statuses:
  - verified
  - suspicious
  - rejected
  - needs review

Success criteria:

- At least focus/study tasks can be verified without extra sensors.
- Running/walking can eventually be verified without manual honesty.
- Proof system is privacy-conscious and explainable.

## 8. Feature Plan: Run And Physical Task Verification

### Problem It Solves

The user may set "run today" and then press complete without running. This breaks trust in the app.

### Product Goal

For running/walking/physical tasks, verify the effort through device signals.

### Current State

Morning routines include:

- run
- pushups
- cold shower
- stretching

But completion is mostly manual/timer-based.

### Screens Involved

- Challenge / Morning Routine screen
- Task Builder
- Health Dashboard
- Proof completion screen
- Permission Center

### Implementation Plan

Phase 1:

- Add physical mission types:
  - Run
  - Walk
  - Pushups
  - Stretch
  - Cold shower
- For current version:
  - Run/Walk can start as manual/timer proof.
  - Pushups/stretch can use timer/manual proof.

Phase 2:

- Add Health Connect run/walk verification:
  - require exercise session or distance/steps during mission window
  - show proof summary: distance, duration, source app/device

Phase 3:

- Add GPS fallback:
  - if Health Connect unavailable
  - if user chooses in-app tracking

Phase 4:

- Photo proof for non-trackable tasks:
  - cold shower proof is hard and privacy-sensitive, so avoid strong claims
  - pushups can use timer/manual first, camera/AI later only if product really needs it

Success criteria:

- "Run" cannot be treated the same as manual todo forever.
- User sees why a run was verified or failed.
- App does not over-collect location when health data is enough.

## 9. Feature Plan: Distraction Dump / Thought Capture

### Problem It Solves

Not all distraction is from apps. People get random thoughts while working:

- "I should search this."
- "I need to message someone."
- "What if I check Instagram?"
- "I remembered another task."

If the app only blocks apps but does not handle thoughts, users still lose focus.

### Product Goal

Give users a way to capture thoughts without leaving the mission.

### Current State

No clear dedicated thought-capture feature exists.

### Screens Involved

- Work Lock Activity
- Pomodoro Chamber
- Focus Check-in Overlay
- Mission Summary
- Daily Review

### Implementation Plan

Phase 1:

- Add a small "capture thought" action inside focus mode.
- User can type one line.
- Save thought with task id and timestamp.
- Return user immediately to mission.

Phase 2:

- Add quick categories:
  - later task
  - worry
  - temptation
  - idea
  - message someone

Phase 3:

- At mission end, show captured thoughts:
  - convert to task
  - dismiss
  - schedule later

Success criteria:

- User does not need to leave work to handle mental noise.
- Thoughts are captured, not followed.
- The focus experience becomes calmer and more realistic.

## 10. Feature Plan: Daily Integrity Review

### Problem It Solves

Users often do not know why they failed. They remember feelings, not facts. The app needs to show the truth of the day without making the user quit.

### Product Goal

At night or next app open, show:

- what was promised
- what was verified
- what was skipped
- where phone time leaked
- what triggered failure
- what tomorrow should change

### Current State

Already exists:

- Daily integrity record
- Usage autopsy
- Monthly audit
- task stats
- temptation logs
- focus sessions

Weakness:

- Too many separate screens.
- Integrity does not yet drive a strong next-day plan.
- Proof is missing, so integrity can be based on manual completion.

### Screens Involved

- Home
- Review Console
- Stats
- Night Decision
- Future Daily Review screen

### Implementation Plan

Phase 1:

- Create one clear Daily Review screen.
- Show:
  - completed
  - skipped
  - failed proof
  - distractions opened
  - wasted time
  - recovery completed or not

Phase 2:

- Add one daily verdict:
  - Clean day
  - Recovered day
  - Leaked day
  - Broken promises

Phase 3:

- Recommend tomorrow adjustment:
  - fewer missions
  - earlier start
  - stricter app blocking
  - sleep lock adjustment
  - recovery mode

Phase 4:

- In Iron Mode, low-integrity day can increase next-day strictness.
- In Soft Mode, low-integrity day suggests support instead of punishment.

Success criteria:

- User understands exactly what happened.
- Review creates tomorrow's improvement.
- The screen is honest without being emotionally destructive.

## 11. Feature Plan: Onboarding For Discipline Setup

### Problem It Solves

Current onboarding is too thin. A user can enter the app before the app knows:

- which apps distract them
- what their first mission is
- what strictness they want
- whether proof is required
- whether accountability is enabled

### Product Goal

Onboarding should create the first working discipline loop.

### Current State

Current onboarding includes:

- language selection
- intro promise
- diagnostic
- identity setup
- commitment contract
- intensity

Missing:

- first mission setup
- distraction setup
- allowed app setup
- proof selection
- consequence setup
- permission readiness

### Screens Involved

- Language selection
- Intro promise
- Diagnostic/Profile
- Identity setup
- Commitment contract
- Intensity screen
- App Selector
- Task Builder
- Permission Center

### Implementation Plan

Phase 1:

- Redesign onboarding sequence:
  - language
  - promise
  - strictness mode
  - first mission
  - distraction setup
  - permissions
  - start home

Phase 2:

- Make permissions contextual:
  - Accessibility for app blocking
  - Usage access for phone waste reports
  - Health only when physical/sleep proof is enabled
  - Location only when GPS proof is enabled
  - Contacts/SMS only if accountability is enabled

Phase 3:

- Do not force every permission at once.
- Only require what is needed for the first mission.

Success criteria:

- First launch ends with a real mission ready.
- User understands what the app will block and why.
- Permissions feel purposeful, not scary.

## 12. Feature Plan: Accountability Alerts

### Problem It Solves

Some users need social consequence. If they break a promise, someone trusted should know.

### Product Goal

Make accountability opt-in, clear, and controlled.

### Current State

Already exists:

- Accountability setup screen
- `AccountabilityManager`
- SMS permission in manifest

Weakness:

- Skip alert appears not wired into normal task skip flow.
- SMS is sensitive and can be risky if not explained clearly.

### Screens Involved

- Accountability setup
- Task Builder consequence section
- Failure/recovery screen
- Permission Center

### Implementation Plan

Phase 1:

- Keep accountability optional.
- In setup, explain exactly when alerts are sent.
- Ask for partner name/phone.

Phase 2:

- Let user attach accountability to specific mission types or Iron Mode only.
- Wire alert on confirmed failure:
  - skipped task
  - failed proof
  - abandoned work lock

Phase 3:

- Log sent alert as event.
- If SMS fails, show non-blocking failure note.

Success criteria:

- Alerts are never surprising.
- User chooses when accountability applies.
- Broken promises can create real-world follow-up when enabled.

## 13. Feature Plan: Sleep Lock

### Problem It Solves

Bad sleep destroys next-day focus. Users often lose hours at night and start the next day weak.

### Product Goal

Sleep Lock should protect bedtime and reduce late-night phone drift.

### Current State

Sleep Lock is one of the most complete features:

- setup UI
- alarms
- active lock
- sound service
- emergency allowlist
- DND policy
- accessibility enforcement
- boot recovery

Known issues from audit:

- hidden 20-minute warning stage does not match setup UI
- DND silence toggle can be saved even when permission is missing
- sound download state can get stuck
- some stored fields are unused
- boot recovery may restore state without restoring visible lock surface

### Screens Involved

- Sleep Lock setup
- Bedtime Incoming activity
- Sleep Lock activity
- Permission Center
- Daily Review

### Implementation Plan

Phase 1:

- Align warning stages with UI.
- Either expose the 20-minute warning or remove it.
- Make DND toggle honest:
  - disabled until permission granted
  - or clearly marked as inactive until permission is granted

Phase 2:

- Fix sound download state:
  - success
  - failure
  - retry
  - no stuck loading state

Phase 3:

- Define reboot behavior:
  - if sleep window is active, should app relaunch lock surface or rely on accessibility guard?
- Implement consistently.

Phase 4:

- Connect Sleep Lock results to Daily Integrity:
  - bedtime respected
  - emergency exit used
  - late-night breach

Success criteria:

- Sleep Lock behavior matches what setup promised.
- User can trust it overnight.
- Sleep data helps explain next-day focus.

## 14. Feature Plan: Health Connect Expansion

### Problem It Solves

Health data can verify real-world behavior and explain focus quality.

### Product Goal

Use Health Connect for proof and insight, starting with low-risk data.

### Current State

Currently reads sleep sessions.

Missing:

- steps
- distance
- exercise sessions
- active minutes
- run/walk verification

### Screens Involved

- Health permission screen
- Health dashboard
- Task Builder proof section
- Run proof completion
- Daily Review

### Implementation Plan

Phase 1:

- Keep sleep insights.
- Add permission model for steps/distance/exercise only when needed.

Phase 2:

- Add run/walk proof using Health Connect:
  - mission window
  - distance
  - duration
  - source app/device

Phase 3:

- Add daily correlation:
  - slept poorly -> focus leaks increased
  - ran in morning -> fewer phone breaches

Success criteria:

- Health data is not just a dashboard.
- It directly verifies promises or explains performance.

## 15. Feature Plan: Phone Waste Autopsy

### Problem It Solves

Many users do not fail one task. They lose the whole day to the phone.

### Product Goal

Show where the day leaked and turn that into stricter rules.

### Current State

UsageAnalyzer already reads usage stats and classifies wasted/invested time.

Weakness:

- Classification may be rough.
- Insights are separate from mission planning.
- It does not automatically suggest blocking newly discovered leaks strongly enough.

### Screens Involved

- Daily Review
- Distractions setup
- Permission Center
- Home

### Implementation Plan

Phase 1:

- Move yesterday autopsy summary into Daily Review.
- Show top 3 phone leaks.

Phase 2:

- Add one-tap action:
  - block this app
  - allow as tool
  - ask per mission

Phase 3:

- If app is repeatedly high-waste:
  - recommend adding to Void
  - in Iron Mode, auto-suggest stricter lock

Success criteria:

- Phone waste becomes visible.
- User can convert insight into blocking rules immediately.

## 16. Suggested Implementation Order

### Phase 1: Product Focus Cleanup

Goal:

Make the visible app match the main idea.

Tasks:

1. Hide/freeze non-core navigation:
   - Club Chat
   - Iron Circle
   - contact discovery
   - friend compare
   - shareable cards
   - artifacts collection
   - advanced social/community surfaces
   - Spring dedicated-device controls from normal settings
2. Keep code but remove normal user entry points.
3. Keep only core surfaces:
   - Home
   - Task Builder
   - Distraction setup
   - Focus Lock / Work Lock
   - Punishment / Recovery
   - Sleep Lock
   - Daily Review
   - Permission Center

### Phase 2: Mission Builder Upgrade

Goal:

Every task becomes a mission with proof and consequence.

Tasks:

1. Add proof model.
2. Add proof selection to Task Builder.
3. Add consequence selection to Task Builder.
4. Show proof status on Home.

### Phase 3: Failure And Recovery System

Goal:

Skipping or failing cannot be frictionless.

Tasks:

1. Create unified failure event.
2. Route skip/failure to recovery screen.
3. Lock entertainment until recovery is complete.
4. Wire accountability alert where enabled.

### Phase 4: Proof v1

Goal:

Verify focus/study tasks using signals already available.

Tasks:

1. Manual proof.
2. Timer proof.
3. Clean app usage proof.
4. Mission summary with breach count and focus score.

### Phase 5: Daily Integrity Review

Goal:

Turn the day into a clear truth report.

Tasks:

1. Build one Daily Review screen.
2. Include tasks, proof, skips, phone waste, recovery.
3. Recommend tomorrow adjustment.

### Phase 6: Physical Proof

Goal:

Verify real-world tasks.

Tasks:

1. Expand Health Connect to steps/distance/exercise.
2. Add run/walk proof.
3. Add GPS fallback later.
4. Add photo proof later.

## 17. Current Version North Star

If a user asks, "What does this app do?", the answer should be:

IronMind helps you keep promises. It blocks distractions while you work, verifies whether you actually did the mission, and forces recovery when you break your commitment.

Every current-version feature should support that sentence.
