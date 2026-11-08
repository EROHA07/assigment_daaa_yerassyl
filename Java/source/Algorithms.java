import java.util.*;

/** Implementations for DAA Assignment 4. See docs/guide/CONTRACTS.md. */
public final class Algorithms {
    private Algorithms() {}

    private static String str(Map<String,Object> in, String key) {
        Object v=in.get(key);
        if (!(v instanceof String)) throw new IllegalArgumentException("Expected string field: "+key);
        return (String)v;
    }
    private static long number(Object v) {
        if (!(v instanceof Number)) throw new IllegalArgumentException("Expected integer number");
        return ((Number)v).longValue();
    }
    private static List<?> list(Map<String,Object> in,String key) {
        Object v=in.get(key);
        if (!(v instanceof List)) throw new IllegalArgumentException("Expected array field: "+key);
        return (List<?>)v;
    }
    private static Map<String,Object> result(Object... kv) {
        Map<String,Object> m=new LinkedHashMap<>();
        for(int i=0;i<kv.length;i+=2)m.put((String)kv[i],kv[i+1]);
        return m;
    }

    // LCS: prefix DP and backwards reconstruction. O(n*m) time and memory.
    static Map<String,Object> lcs(Map<String,Object> input) {
        String a=str(input,"a"),b=str(input,"b"); int n=a.length(),m=b.length();
        int[][] dp=new int[n+1][m+1];
        for(int i=1;i<=n;i++)for(int j=1;j<=m;j++)
            dp[i][j]=a.charAt(i-1)==b.charAt(j-1)?dp[i-1][j-1]+1:Math.max(dp[i-1][j],dp[i][j-1]);
        StringBuilder reversed=new StringBuilder(); int i=n,j=m;
        while(i>0&&j>0){
            if(a.charAt(i-1)==b.charAt(j-1)){reversed.append(a.charAt(i-1));i--;j--;}
            else if(dp[i-1][j]>=dp[i][j-1])i--;else j--;
        }
        return result("length",(long)dp[n][m],"sequence",reversed.reverse().toString());
    }

    // Ordered adjacent interval merging. O(n^3) time, O(n^2) memory.
    static Map<String,Object> merge(Map<String,Object> input) {
        List<?> raw=list(input,"sizes");int n=raw.size();long[] prefix=new long[n+1];
        for(int i=0;i<n;i++)prefix[i+1]=prefix[i]+number(raw.get(i));
        if(n<=1)return result("cost",0L,"plan",new ArrayList<>());
        long[][] dp=new long[n][n];int[][] split=new int[n][n];
        for(int len=2;len<=n;len++)for(int l=0;l+len<=n;l++){
            int r=l+len-1;long sum=prefix[r+1]-prefix[l],best=Long.MAX_VALUE;int bestK=l;
            for(int k=l;k<r;k++){long candidate=dp[l][k]+dp[k+1][r]+sum;if(candidate<best){best=candidate;bestK=k;}}
            dp[l][r]=best;split[l][r]=bestK;
        }
        List<Object> plan=new ArrayList<>();addMerges(0,n-1,split,plan);
        return result("cost",dp[0][n-1],"plan",plan);
    }
    private static void addMerges(int l,int r,int[][] split,List<Object> plan){
        if(l>=r)return;int k=split[l][r];addMerges(l,k,split,plan);addMerges(k+1,r,split,plan);
        plan.add(Arrays.asList((long)l,(long)k,(long)r));
    }

    // Tree maximum-weight independent set. Iterative traversal avoids stack overflow.
    static Map<String,Object> tree(Map<String,Object> input) {
        int n=(int)number(input.get("n"));List<?> rawWeights=list(input,"weights"),rawEdges=list(input,"edges");
        long[] weights=new long[n];for(int i=0;i<n;i++)weights[i]=number(rawWeights.get(i));
        if(n==0)return result("weight",0L,"selected",new ArrayList<>());
        @SuppressWarnings("unchecked") List<Integer>[] graph=new ArrayList[n];
        for(int i=0;i<n;i++)graph[i]=new ArrayList<>();
        for(Object e:rawEdges){List<?> pair=(List<?>)e;int u=(int)number(pair.get(0)),v=(int)number(pair.get(1));graph[u].add(v);graph[v].add(u);}
        int[] parent=new int[n],order=new int[n],stack=new int[n];Arrays.fill(parent,-2);
        int size=0,top=0;stack[top++]=0;parent[0]=-1;
        while(top>0){int u=stack[--top];order[size++]=u;for(int v:graph[u])if(parent[v]==-2){parent[v]=u;stack[top++]=v;}}
        long[] take=new long[n],skip=new long[n];
        for(int ix=size-1;ix>=0;ix--){int u=order[ix];take[u]=weights[u];skip[u]=0;
            for(int v:graph[u])if(parent[v]==u){take[u]+=skip[v];skip[u]+=Math.max(take[v],skip[v]);}}
        List<Long> selected=new ArrayList<>();int[] nodes=new int[n];boolean[] parentTaken=new boolean[n];top=0;nodes[top]=0;parentTaken[top++]=false;
        while(top>0){--top;int u=nodes[top];boolean pTaken=parentTaken[top],choose=!pTaken&&take[u]>skip[u];
            if(choose)selected.add((long)u);
            for(int v:graph[u])if(parent[v]==u){nodes[top]=v;parentTaken[top++]=choose;}}
        return result("weight",Math.max(take[0],skip[0]),"selected",selected);
    }

    // Naive exact matching, including overlaps and empty-pattern boundaries.
    static Map<String,Object> naive(Map<String,Object> input) {
        String text=str(input,"text"),pattern=str(input,"pattern");int n=text.length(),m=pattern.length();List<Long> positions=new ArrayList<>();
        if(m==0){for(int i=0;i<=n;i++)positions.add((long)i);}
        else for(int i=0;i+m<=n;i++){int j=0;while(j<m&&text.charAt(i+j)==pattern.charAt(j))j++;if(j==m)positions.add((long)i);}
        return result("positions",positions);
    }

    // KMP with prefix function. O(n+m+z).
    static Map<String,Object> kmp(Map<String,Object> input) {
        String text=str(input,"text"),pattern=str(input,"pattern");int n=text.length(),m=pattern.length();List<Long> positions=new ArrayList<>();
        if(m==0){for(int i=0;i<=n;i++)positions.add((long)i);return result("positions",positions);}
        if(m>n)return result("positions",positions);
        int[] pi=new int[m];
        for(int i=1,j=0;i<m;i++){while(j>0&&pattern.charAt(i)!=pattern.charAt(j))j=pi[j-1];if(pattern.charAt(i)==pattern.charAt(j))j++;pi[i]=j;}
        for(int i=0,j=0;i<n;i++){while(j>0&&text.charAt(i)!=pattern.charAt(j))j=pi[j-1];if(text.charAt(i)==pattern.charAt(j))j++;if(j==m){positions.add((long)(i-m+1));j=pi[j-1];}}
        return result("positions",positions);
    }

    // Exact Rabin-Karp: base 31, modulus 101, with mandatory character verification.
    static Map<String,Object> rk(Map<String,Object> input) {
        String text=str(input,"text"),pattern=str(input,"pattern");int n=text.length(),m=pattern.length();List<Long> positions=new ArrayList<>();
        if(m==0){for(int i=0;i<=n;i++)positions.add((long)i);return result("positions",positions);}
        if(m>n)return result("positions",positions);
        final long base=31,mod=101;long high=1;for(int i=1;i<m;i++)high=high*base%mod;
        long ph=0,th=0;for(int i=0;i<m;i++){ph=(ph*base+pattern.charAt(i))%mod;th=(th*base+text.charAt(i))%mod;}
        for(int start=0;start<=n-m;start++){
            if(ph==th){int j=0;while(j<m&&text.charAt(start+j)==pattern.charAt(j))j++;if(j==m)positions.add((long)start);}
            if(start<n-m){th=(th-(text.charAt(start)*high)%mod+mod)%mod;th=(th*base+text.charAt(start+m))%mod;}
        }
        return result("positions",positions);
    }

    // Prefix-doubling suffix array and Kasai LCP. O(n log^2 n) time.
    static Map<String,Object> suffix(Map<String,Object> input) {
        String s=str(input,"text");int n=s.length();
        if(n==0)return result("sa",new ArrayList<>(),"lcp",new ArrayList<>(),"repeated","");
        Integer[] boxed=new Integer[n];int[] rank=new int[n],next=new int[n];
        for(int i=0;i<n;i++){boxed[i]=i;rank[i]=s.charAt(i);}
        for(int width=1;width<n;width*=2){final int w=width;final int[] r=rank;
            Arrays.sort(boxed,(x,y)->{int c=Integer.compare(r[x],r[y]);if(c!=0)return c;int rx=x+w<n?r[x+w]:-1,ry=y+w<n?r[y+w]:-1;return Integer.compare(rx,ry);});
            next[boxed[0]]=0;
            for(int i=1;i<n;i++){int x=boxed[i-1],y=boxed[i];boolean diff=rank[x]!=rank[y]||(x+w<n?rank[x+w]:-1)!=(y+w<n?rank[y+w]:-1);next[y]=next[x]+(diff?1:0);}
            int[] tmp=rank;rank=next;next=tmp;if(rank[boxed[n-1]]==n-1)break;if(width>n/2)break;
        }
        int[] sa=new int[n];for(int i=0;i<n;i++)sa[i]=boxed[i];int[] inverse=new int[n];for(int i=0;i<n;i++)inverse[sa[i]]=i;
        int[] lcp=new int[n];int h=0;
        for(int i=0;i<n;i++){int pos=inverse[i];if(pos==0){h=0;continue;}int j=sa[pos-1];while(i+h<n&&j+h<n&&s.charAt(i+h)==s.charAt(j+h))h++;lcp[pos]=h;if(h>0)h--;}
        int best=0,bestPos=0;for(int i=1;i<n;i++)if(lcp[i]>best){best=lcp[i];bestPos=sa[i];}
        List<Long> saOut=new ArrayList<>(n),lcpOut=new ArrayList<>(n);for(int i=0;i<n;i++){saOut.add((long)sa[i]);lcpOut.add((long)lcp[i]);}
        return result("sa",saOut,"lcp",lcpOut,"repeated",best==0?"":s.substring(bestPos,bestPos+best));
    }

    // Aho-Corasick with dense printable-ASCII transitions and reverse failure aggregation.
    static Map<String,Object> aho(Map<String,Object> input) {
        List<?> rawPatterns=list(input,"patterns"),rawTexts=list(input,"texts");int p=rawPatterns.size(),total=1;
        for(Object x:rawPatterns)total+=((String)x).length();
        final int alphabet=95;int[][] go=new int[total][alphabet];int[] fail=new int[total],terminal=new int[p];int states=1;
        for(int i=0;i<p;i++){String pat=(String)rawPatterns.get(i);int state=0;
            for(int k=0;k<pat.length();k++){int c=pat.charAt(k)-32;if(c<0||c>=alphabet)throw new IllegalArgumentException("Character outside printable ASCII");if(go[state][c]==0)go[state][c]=states++;state=go[state][c];}
            terminal[i]=state;
        }
        int[] queue=new int[states],order=new int[states];int head=0,tail=0,orderSize=0;
        for(int c=0;c<alphabet;c++){int v=go[0][c];if(v!=0){fail[v]=0;queue[tail++]=v;}}
        while(head<tail){int u=queue[head++];order[orderSize++]=u;
            for(int c=0;c<alphabet;c++){int v=go[u][c];if(v!=0){fail[v]=go[fail[u]][c];queue[tail++]=v;}else go[u][c]=go[fail[u]][c];}
        }
        List<Object> rows=new ArrayList<>();
        for(Object rawText:rawTexts){String text=(String)rawText;long[] visits=new long[states];int state=0;
            for(int i=0;i<text.length();i++){int c=text.charAt(i)-32;if(c<0||c>=alphabet)throw new IllegalArgumentException("Character outside printable ASCII");state=go[state][c];visits[state]++;}
            for(int i=orderSize-1;i>=0;i--){int u=order[i];visits[fail[u]]+=visits[u];}
            List<Long> row=new ArrayList<>(p);for(int i=0;i<p;i++)row.add(visits[terminal[i]]);rows.add(row);
        }
        return result("counts",rows);
    }
}
