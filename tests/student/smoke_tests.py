"""Small additional smoke tests for the Java DAA A4 implementation."""
import json
import pathlib
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "Java" / "source"

def main():
    with tempfile.TemporaryDirectory() as build:
        subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", build,
                        *map(str, SRC.glob("*.java"))], check=True)
        cases = [
            {"task":"lcs","a":"axbyc","b":"abc"},
            {"task":"merge","sizes":[2,3,4]},
            {"task":"tree","n":3,"weights":[4,9,6],"edges":[[0,1],[1,2]]},
            {"task":"naive","text":"aaaaa","pattern":"aaa"},
            {"task":"kmp","text":"abababa","pattern":"aba"},
            {"task":"rk","text":"abdj","pattern":"ab"},
            {"task":"suffix","text":"banana"},
            {"task":"aho","patterns":["he","she","he"],"texts":["sheshe",""]},
        ]
        payload="".join(json.dumps(x)+"\n" for x in cases)
        p=subprocess.run(["java","-Xmx384m","-cp",build,"Main"],input=payload,
                         text=True,capture_output=True,check=True,timeout=20)
        out=[json.loads(line) for line in p.stdout.splitlines()]
        assert len(out)==len(cases)
        assert out[0]["length"]==3 and out[0]["sequence"]=="abc"
        assert out[1]["cost"]==14
        assert out[2]["weight"]==10 and set(out[2]["selected"])=={0,2}
        assert out[3]["positions"]==[0,1,2]
        assert out[4]["positions"]==[0,2,4]
        assert out[5]["positions"]==[0] # deliberate base-31/mod-101 collision
        assert out[6]["sa"]==[5,3,1,0,4,2] and out[6]["lcp"]==[0,1,3,0,0,2]
        assert out[7]["counts"]==[[2,2,2],[0,0,0]]
    print("PASS: eight command smoke tests.")

if __name__=="__main__":
    main()
