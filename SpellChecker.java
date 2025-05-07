import java.io.*;
import java.util.*;

/**
 * CENG 383 – PA‑2 ‑ Spell Checker (improved)
 * ‑ Trie‑based, case‑insensitive spell checker that prints
 *   Correct Word / No Suggestions / Misspelled? <up to 3 words>
 *   according to assignment rules.
 *
 *  Dictionary input formats supported:
 *  1.  N  <word1> <word2> … <wordN>   (N first, may span lines)
 *  2.  <word1> <word2> … <wordK> \n   (first line only)
 *
 *  Queries follow the dictionary and finish with the token "EXIT".
 */
public class SpellChecker {
    /* ‑‑‑ Trie node ‑‑‑ */
    private static class Node {
        Node[] next = new Node[26];   // children for 'a'…'z'
        boolean isWord;               // end‑of‑word flag
    }

    private final Node root = new Node();
    private static final int LIMIT = 3;        // max suggestions

    /* ---------- insert ---------- */
    private void insert(String word) {
        if (word.isEmpty()) return;
        Node cur = root;
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (cur.next[idx] == null) cur.next[idx] = new Node();
            cur = cur.next[idx];
        }
        cur.isWord = true;
    }

    /* ---------- clean word (letters → lowercase) ---------- */
    private static String clean(String token) {
        StringBuilder sb = new StringBuilder();
        for (char ch : token.toCharArray()) if (Character.isLetter(ch)) sb.append(Character.toLowerCase(ch));
        return sb.toString();
    }

    /* ---------- query handling ---------- */
    private void handleQuery(String raw) {
        String w = clean(raw);
        if (w.isEmpty()) return;                 // ignore blanks / non‑letters

        // 1) Follow as far as possible in the trie keeping depth
        Node cur = root;
        int depth = 0;
        for (char ch : w.toCharArray()) {
            int idx = ch - 'a';
            if (idx < 0 || idx >= 26 || cur.next[idx] == null) break; // path breaks
            cur = cur.next[idx];
            depth++;
        }

        // exact match ?
        if (depth == w.length() && cur.isWord) {
            System.out.println("Correct Word");
            return;
        }

        // first letter absent ?
        if (depth == 0) {
            System.out.println("No Suggestions");
            return;
        }

        // 2) collect suggestions from deepest matched node
        List<String> sugg = new ArrayList<>(LIMIT);
        dfs(cur, new StringBuilder(w.substring(0, depth)), sugg);

        if (sugg.isEmpty()) System.out.println("No Suggestions");
        else                System.out.println("Misspelled? " + String.join(" ", sugg));
    }

    /* depth‑first lexicographic traversal (stop at LIMIT) */
    private void dfs(Node node, StringBuilder sb, List<String> out) {
        if (out.size() == LIMIT) return;
        if (node.isWord) out.add(sb.toString());
        for (char c = 'a'; c <= 'z' && out.size() < LIMIT; c++) {
            Node nxt = node.next[c - 'a'];
            if (nxt != null) {
                sb.append(c);
                dfs(nxt, sb, out);
                sb.deleteCharAt(sb.length() - 1); // backtrack
            }
        }
    }

    /* ---------- main ---------- */
    public static void main(String[] args) throws IOException {
        SpellChecker sc = new SpellChecker();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        /* ---- 1) read first non‑empty line ---- */
        String line;
        do { line = br.readLine(); } while (line != null && line.trim().isEmpty());
        if (line == null) return;

        StringTokenizer st = new StringTokenizer(line);
        String first = st.nextToken();

        // --- format‑A: integer N followed by N words (may span lines) ---
        if (first.chars().allMatch(Character::isDigit)) {
            int N = Integer.parseInt(first);
            int read = 0;
            while (read < N) {
                if (!st.hasMoreTokens()) {
                    line = br.readLine();
                    if (line == null) break;
                    st = new StringTokenizer(line);
                    continue;
                }
                sc.insert(clean(st.nextToken()));
                read++;
            }
        }
        // --- format‑B: single line dictionary ---
        else {
            sc.insert(clean(first));
            while (st.hasMoreTokens()) sc.insert(clean(st.nextToken()));
        }

        /* ---- 2) remaining tokens are queries ---- */
        while ((line = br.readLine()) != null) {
            StringTokenizer q = new StringTokenizer(line);
            while (q.hasMoreTokens()) {
                String token = q.nextToken();
                if ("EXIT".equals(token)) return;   // terminate
                sc.handleQuery(token);
            }
        }
    }
}
