# IronMind Bug / Issue Audit Prompt

Use this prompt whenever auditing a screen, flow, or feature for bugs, weak logic, UX issues, regressions, edge cases, or structural problems.

## Prompt

Act as:

1. Software Engineer
2. UI/UX Designer
3. Product Manager
4. Analyst
5. End User
6. Behavioral Psychologist / Habit Coach

Your job is to deeply scan the feature or screen and identify:
- bugs
- logic flaws
- risky behavior
- weak UX
- missing edge-case handling
- poor product decisions
- misleading wording
- things that should be removed, merged, simplified, or fixed

Be honest and specific.

## Output Format

## 1. What Is Being Audited
- screen / feature / flow name

## 2. Overall Judgment
- stable / unstable
- complete / incomplete
- usable / confusing / risky

## 3. Findings
List findings ordered by severity:

For each finding include:
- title
- why it matters
- who it hurts
- likely cause
- whether it is:
  - bug
  - UX issue
  - product issue
  - copy issue
  - analytics gap
  - architecture issue

## 4. Persona Interpretation

### Software Engineer
- correctness
- risk
- maintainability

### UI/UX Designer
- hierarchy
- layout
- interaction
- readability
- friction

### Product Manager
- feature placement
- priority problems
- unnecessary or missing elements

### Analyst
- what is not measurable
- what assumptions are weak

### End User
- what feels broken, confusing, or frustrating

### Behavioral Psychologist / Habit Coach
- where the feature harms motivation, discipline, recovery, or emotional balance

## 5. What Is Good
- list strengths worth protecting

## 6. What Should Be Fixed First
- highest-value fixes

## 7. What Can Wait
- medium/low priority issues

## 8. What Should Be Removed / Merged / Simplified
- if applicable

## 9. Recommended Fix Plan
Break the fix into:
1. critical fixes
2. structural fixes
3. polish fixes

## 10. Best Next Sprint
- give the sprint name
- explain what to fix first

---

## Compact Version

Use this shorter form when needed:

- Overall judgment
- Findings
- What is good
- What to fix first
- What can wait
- Next sprint

---

## Best Usage Notes

- Use this for review after implementation, before polish, or when something feels “off.”
- Findings should come first, not praise.
- If there are no issues, say so clearly and mention any remaining risk or testing gap.
- For IronMind, always include:
  - logic correctness
  - UX clarity
  - behavioral impact
