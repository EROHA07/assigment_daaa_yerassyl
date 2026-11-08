# Assignment 4 — algorithm analysis

## Environment and commands

Reference validation environment: Linux x86_64, OpenJDK 21.0.12.1 (compiled with `--release 17`), Python 3.13.5, 3 logical CPU cores. These are the reference container details, not the student's laptop.

From the repository root on Windows PowerShell:

```powershell
python tools/run.py --setup
python tools/run.py --check
python tests/student/smoke_tests.py
python tests/student/benchmark_search.py
```

The official launcher compiles Java with `javac --release 17`. The program reads one JSON object per line and writes exactly one JSON result per line. It must not print prompts or debug messages to standard output.

## Algorithms, correctness and complexity

### 1. LCS — `lcs`

State `dp[i][j]` is the LCS length of prefixes `a[0..i)` and `b[0..j)`. Empty prefixes have value zero. If the last characters match, add one to the diagonal state; otherwise take the maximum of the state above and the state to the left. Reconstruct from the bottom-right: matching characters belong to an optimal subsequence; on a mismatch move to a neighbour with the same optimal value. Reverse the collected characters at the end. This returns a common subsequence of maximum length. Time and memory: O((n+1)(m+1)).

### 2. Ordered adjacent merge — `merge`

State `dp[l][r]` is the minimum cost to merge the consecutive original blocks from l through r. A final merge must split the interval at some k, so its cost is the left optimum plus the right optimum plus the sum of all sizes in [l,r]. Prefix sums calculate interval sums in O(1). Try every split for every interval, then reconstruct recursively: emit the left plan, the right plan, and only then the final merge [l,k,r]. This postorder is a valid chronological plan. Time O(n^3), memory O(n^2); prefix sums take O(n).

### 3. Tree maximum-weight independent set — `tree`

Root the tree at vertex 0 and build an iterative traversal order. For each vertex u, `take[u]` is its best subtree weight when u is selected; `skip[u]` is its best weight when u is not selected. In reverse traversal order, take is u's weight plus every child's skip; skip is the sum of each child's maximum of take and skip. Reconstruct top-down: a selected parent forces every child to be skipped. Ties may choose skip. The empty set is allowed, so negative weights never force a negative answer. Iterative traversal also handles a 100,000-vertex chain without recursion overflow. Time and auxiliary memory O(n).

### 4. Exact string search — `naive`, `kmp`, `rk`

All three return every zero-based match, including overlaps. An empty pattern matches all n+1 boundaries.

- Naive checks each possible start character by character. Worst-case time O(nm+n+1), O(1) auxiliary memory besides the output.
- KMP first builds the prefix function pi. On mismatch it falls back to pi[j-1] instead of restarting; after a match it also falls back, preserving overlaps. Time O(n+m+z), auxiliary memory O(m).
- Rabin–Karp uses polynomial rolling hashes with base 31 and modulus 101. The leftmost character has the highest power. The rolling step removes the outgoing character, multiplies by the base and adds the incoming character. A matching hash is only a candidate: every candidate is verified character by character. The small modulus deliberately creates collisions (for example, `ab` and `dj`); verification prevents false matches. Time O(n+m+c*m), where c is the number of equal-hash windows; worst case can be quadratic. Auxiliary memory O(1) plus output.

### 5. Suffix array and Kasai — `suffix`

Prefix doubling initially ranks suffixes by their first character. At width w, sort suffix starts by the pair (rank[i], rank[i+w]), using -1 when the second half is outside the string. Assign new ranks to equal/different pairs and double w until all ranks are unique or the prefix covers the text. Only suffix indices are sorted; suffix strings are not copied. Comparison sorting gives O(n log^2(n+1)) time and O(n) memory.

Kasai builds the inverse suffix rank. For each text position, compare its suffix with the previous suffix in sorted order, store the common-prefix length in the corresponding LCP cell, and reuse the previous match length minus one on the next iteration. The longest repeated substring is represented by a maximum adjacent LCP; overlapping occurrences are allowed. Kasai is O(n) time and O(n) memory. No sentinel or empty suffix is output.

### 6. Aho–Corasick — `aho`

Insert all patterns into one trie. Breadth-first construction creates failure links and completes transitions for the 95 printable ASCII characters. While scanning one text, increment the visit count of each reached state. Then process states in reverse BFS order and add each visit count to its failure state. A terminal state's final count therefore includes matches of that pattern and matches reached through longer patterns' suffix links. Duplicate dictionary entries share a terminal state but retain separate output columns. Counts are reset for every text. With S states, total pattern length L, p patterns and a fixed alphabet of 95, construction is O(L+95S+p); each text is O(|text|+S+p), with O(95S+p) auxiliary memory.

## Additional tests

`tests/student/smoke_tests.py` compiles the Java source and checks all eight commands, including an LCS subsequence, the merge plan cost, tree reconstruction, overlapping matches, the Rabin–Karp collision, suffix-array/LCP output, and Aho–Corasick suffix-pattern counts. It is an additional smoke test, not a replacement for the official 44 public cases.

The implementation was also checked in a reference environment with deterministic randomized search comparisons and stress cases: a 100,000-vertex tree path, suffix input of length 50,000, and Aho–Corasick with a 200,000-character text. The official `python tools/run.py --check` must still be run from this repository before submission.

## Naive / KMP / RK comparison

The benchmark script uses seed 20261108, n = 300, 1000 and 3000, and m = n/10. It tests a random four-character alphabet (pattern selected from the text) and a repetitive all-`a` case. It verifies identical positions before timing, performs one warm-up, then records three runs per algorithm. Compilation is excluded; timing includes writing/flushing one JSON request and reading/parsing its JSON response. The table below is from the reference Linux/OpenJDK container and may differ on another computer.

| Case | n | m | Algorithm | Median (ms) |
|---|---:|---:|---|---:|
| Random | 300 | 30 | Naive | 0.251 |
| Random | 300 | 30 | KMP | 0.175 |
| Random | 300 | 30 | RK | 0.257 |
| Random | 1000 | 100 | Naive | 0.302 |
| Random | 1000 | 100 | KMP | 0.400 |
| Random | 1000 | 100 | RK | 0.418 |
| Random | 3000 | 300 | Naive | 0.655 |
| Random | 3000 | 300 | KMP | 0.649 |
| Random | 3000 | 300 | RK | 0.757 |
| Repetitive | 300 | 30 | Naive | 0.838 |
| Repetitive | 300 | 30 | KMP | 0.367 |
| Repetitive | 300 | 30 | RK | 0.854 |
| Repetitive | 1000 | 100 | Naive | 1.386 |
| Repetitive | 1000 | 100 | KMP | 0.573 |
| Repetitive | 1000 | 100 | RK | 1.575 |
| Repetitive | 3000 | 300 | Naive | 1.665 |
| Repetitive | 3000 | 300 | KMP | 2.100 |
| Repetitive | 3000 | 300 | RK | 1.637 |

At these small sizes, JVM warm-up and JSON transport affect timings, so measurements do not prove asymptotic complexity. Repetitive inputs make naive and RK verify many long candidate matches, while KMP reuses prefix information. Random inputs often mismatch early, making naive competitive. The deliberately small RK modulus is for collision testing and should not be generalized to production hashing.

