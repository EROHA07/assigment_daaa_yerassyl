# Assignment 4 — Dynamic Programming and String Processing

## Implementation in this repository

This repository uses **Java 17**. The eight required commands are implemented in `Java/source/Algorithms.java`; the supplied JSON Lines launcher and transport are kept in `Java/source/Main.java` and `Java/source/Json.java`.

### Check the solution locally

Requirements: Python 3.12 or newer and JDK 17 or newer. Open a terminal in the repository root (the folder containing `tools` and `submission.json`) and run:

```powershell
python --version
java -version
javac -version
python tools/run.py --setup
python tools/run.py --check
python tests/student/smoke_tests.py
```

`--setup` checks that Java compiles and the program responds to `ping`; it does not check algorithm correctness. `--check` runs the 44 public cases and repeats the full batch in one process to check that requests do not affect one another. The smoke test is an extra small check. To compare Naive, KMP and Rabin–Karp timings, run:

```powershell
python tests/student/benchmark_search.py
```

If a command fails, copy the complete terminal output into the ChatGPT conversation. Do not hide or edit the error message. After the checks pass, commit the final version and submit the exact commit URL with its full 40-character SHA in Moodle, following the course instructions below.

### Files added for this implementation

- `Java/source/Algorithms.java` — eight required algorithm implementations.
- `tests/student/smoke_tests.py` — additional correctness smoke test.
- `tests/student/benchmark_search.py` — reproducible search comparison.
- `ANALYSIS.md` — English algorithm explanations, complexity, testing and benchmark discussion.

---


[Interactive lesson and practice](https://rzhaniya.github.io/daa-assignment-4-archive/) · [English specification](docs/Assignment_4_EN.md) · [Русская версия](docs/README_RU.md)

**Course:** Design and Analysis of Algorithms. **Practical:** 50 points. **Defence:** 50 points. **Availability:** 29 October–8 November 2026, 23:59, for both practical work and defence; Moodle records approved extensions.

Implement six parts with eight commands: LCS; ordered adjacent-block merging; maximum-weight independent set on a tree; naive, KMP and exact Rabin–Karp search; suffix array with Kasai LCP; Aho–Corasick. The work covers syllabus weeks 8–10. This is a starter template: the algorithm methods are TODOs. Read the specification before coding.


| Practical part | Points |
|---|---:|
| LCS | 8 |
| Ordered adjacent-block merging | 7 |
| Tree independent set | 7 |
| Naive / KMP / exact RK | 10 |
| Suffix array and Kasai LCP | 9 |
| Aho–Corasick | 9 |
| **Total** | **50** |

<details>
<summary>Detailed reference</summary>

[Contracts](docs/guide/CONTRACTS.md) · [Rubric](docs/guide/RUBRIC.md) · [Testing](docs/guide/TESTING.md) · [Submission checklist](docs/guide/SUBMISSION.md) · [Algorithm notes](docs/LECTURE_CLARIFICATIONS_EN_RU.md)

</details>
