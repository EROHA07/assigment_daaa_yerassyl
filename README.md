# DAA Assignment 4

**Student:** Yerassyl Nassypkeliyev  
**Group:** SE2538  
**Language:** Java

## Overview

This project implements eight algorithms covering dynamic programming, tree optimization, string matching, and string processing.

## Implemented Algorithms

1. **Longest Common Subsequence (LCS)** — finds the longest subsequence common to two strings.
2. **Merge Intervals** — calculates the optimal cost of merging adjacent intervals.
3. **Maximum Weight Independent Set on a Tree** — finds a set of non-adjacent vertices with maximum total weight.
4. **Naive String Matching** — searches for a pattern by checking every possible position.
5. **Knuth–Morris–Pratt (KMP)** — performs efficient pattern matching using a prefix function.
6. **Rabin–Karp** — uses rolling hashing to find pattern occurrences.
7. **Suffix Array** — constructs a suffix array and LCP array for string processing.
8. **Aho–Corasick** — searches for multiple patterns in a text using a trie and failure links.

## Project Structure

```text
.
├── Java/
│   └── source/
│       ├── Algorithms.java
│       ├── Main.java
│       └── Json.java
├── tests/
│   └── student/
│       ├── smoke_tests.py
│       └── benchmark_search.py
├── .github/
│   └── workflows/
│       └── check.yml
├── ANALYSIS.md
└── submission.json
```

## Testing and Analysis

The project includes automated tests covering standard cases, edge cases, randomized string-matching inputs, and large tree inputs.

`ANALYSIS.md` contains algorithm descriptions, time and space complexity analysis, correctness test cases, and benchmark results.

## Technologies

- Java 17
- Python 3
- GitHub Actions
