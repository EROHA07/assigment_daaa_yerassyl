"""Deterministic student tests for DAA Assignment 4 (standard library only)."""
import json
import random
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "Java" / "source"


def run_requests(requests, timeout=20):
    with tempfile.TemporaryDirectory() as build:
        subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", build,
                        str(SOURCE / "Main.java"), str(SOURCE / "Algorithms.java"),
                        str(SOURCE / "Json.java")], check=True, cwd=ROOT)
        payload = "".join(json.dumps(x, separators=(",", ":")) + "\n" for x in requests)
        completed = subprocess.run(["java", "-Xmx384m", "-cp", build, "Main"],
                                   input=payload, text=True, capture_output=True,
                                   timeout=timeout, check=True)
        lines = completed.stdout.splitlines()
        assert len(lines) == len(requests), (len(lines), len(requests), completed.stderr)
        return [json.loads(line) for line in lines]


def main():
    requests = [
        {"task": "lcs", "a": "tea_time", "b": "team"},
        {"task": "lcs", "a": "", "b": "abc"},
        {"task": "merge", "sizes": [2, 3, 4]},
        {"task": "merge", "sizes": []},
        {"task": "tree", "n": 3, "weights": [4, 9, 6], "edges": [[0, 1], [1, 2]]},
        {"task": "tree", "n": 4, "weights": [-8, -1, -4, -2], "edges": [[0, 1], [1, 2], [2, 3]]},
        {"task": "naive", "text": "aaaaa", "pattern": "aaa"},
        {"task": "naive", "text": "abc", "pattern": ""},
        {"task": "kmp", "text": "abababa", "pattern": "aba"},
        {"task": "kmp", "text": "abc", "pattern": "abcd"},
        {"task": "rk", "text": "abdj", "pattern": "ab"},
        {"task": "rk", "text": "aaaaa", "pattern": "aaa"},
        {"task": "suffix", "text": "banana"},
        {"task": "suffix", "text": ""},
        {"task": "aho", "patterns": ["he", "she", "he"], "texts": ["sheshe", "sheshe", ""]},
        {"task": "aho", "patterns": [], "texts": ["abc", ""]},
    ]
    out = run_requests(requests)
    assert out[0]["length"] == 4 and out[0]["sequence"] == "team"
    assert out[1] == {"length": 0, "sequence": ""}
    assert out[2]["cost"] == 14 and out[2]["plan"] == [[0, 0, 1], [0, 1, 2]]
    assert out[3] == {"cost": 0, "plan": []}
    assert out[4]["weight"] == 10 and set(out[4]["selected"]) == {0, 2}
    assert out[5]["weight"] == 0 and out[5]["selected"] == []
    assert out[6]["positions"] == [0, 1, 2]
    assert out[7]["positions"] == [0, 1, 2, 3]
    assert out[8]["positions"] == [0, 2, 4]
    assert out[9]["positions"] == []
    assert out[10]["positions"] == [0]  # ab and dj collide modulo 101; verify characters.
    assert out[11]["positions"] == [0, 1, 2]
    assert out[12] == {"sa": [5, 3, 1, 0, 4, 2], "lcp": [0, 1, 3, 0, 0, 2], "repeated": "ana"}
    assert out[13] == {"sa": [], "lcp": [], "repeated": ""}
    assert out[14]["counts"] == [[2, 2, 2], [2, 2, 2], [0, 0, 0]]
    assert out[15]["counts"] == [[], []]

    rng = random.Random(20261108)
    random_requests, expected = [], []
    for _ in range(80):
        text = "".join(rng.choice("ab c") for _ in range(rng.randrange(25)))
        pattern = "".join(rng.choice("ab c") for _ in range(rng.randrange(9)))
        positions = (list(range(len(text) + 1)) if not pattern else
                     [i for i in range(len(text) - len(pattern) + 1)
                      if text.startswith(pattern, i)])
        for task in ("naive", "kmp", "rk"):
            random_requests.append({"task": task, "text": text, "pattern": pattern})
            expected.append(positions)
    random_out = run_requests(random_requests)
    assert all(x["positions"] == e for x, e in zip(random_out, expected))

    n = 100_000
    chain = {"task": "tree", "n": n, "weights": [1] * n,
             "edges": [[i - 1, i] for i in range(1, n)]}
    chain_out = run_requests([chain], timeout=20)[0]
    assert chain_out["weight"] == 50_000 and len(chain_out["selected"]) == 50_000
    print("PASS: 16 fixed cases, 80 randomized search cases, and 100,000-node tree stress.")


if __name__ == "__main__":
    main()
