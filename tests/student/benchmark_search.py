"""Simple reproducible Naive/KMP/RK timing comparison on common-domain inputs."""
import json, pathlib, random, statistics, subprocess, tempfile, time

ROOT=pathlib.Path(__file__).resolve().parents[2]
SRC=ROOT/"Java"/"source"
SEED=20261108

def main():
    with tempfile.TemporaryDirectory() as build:
        subprocess.run(["javac","--release","17","-encoding","UTF-8","-d",build,
                        *map(str,SRC.glob("*.java"))],check=True)
        p=subprocess.Popen(["java","-Xmx384m","-cp",build,"Main"],stdin=subprocess.PIPE,
                           stdout=subprocess.PIPE,text=True)
        rng=random.Random(SEED)
        try:
            print("case,n,m,algorithm,median_ms")
            for kind in ("random","repetitive"):
                for n in (300,1000,3000):
                    m=max(1,n//10)
                    if kind=="random":
                        text="".join(rng.choice("abcd") for _ in range(n)); start=n//3
                        pattern=text[start:start+m]
                    else: text="a"*n; pattern="a"*m
                    def call(task):
                        request={"task":task,"text":text,"pattern":pattern}
                        p.stdin.write(json.dumps(request,separators=(",",":"))+"\n");p.stdin.flush()
                        return json.loads(p.stdout.readline())["positions"]
                    names=("naive","kmp","rk")
                    expected={name:call(name) for name in names}
                    assert expected["naive"]==expected["kmp"]==expected["rk"]
                    for name in names:
                        call(name) # warm-up
                        times=[]
                        for _ in range(3):
                            t=time.perf_counter(); got=call(name); times.append((time.perf_counter()-t)*1000)
                            assert got==expected["naive"]
                        print(f"{kind},{n},{m},{name},{statistics.median(times):.3f}")
        finally:
            p.terminate();p.wait(timeout=5)

if __name__=="__main__": main()
