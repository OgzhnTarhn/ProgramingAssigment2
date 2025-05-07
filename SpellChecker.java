import java.util.*;

/**
 * CENG 383 – PA‑2 Spell Checker (Scanner‑only version)
 * ‑ Case‑insensitive Trie that prints
 *    Correct Word / No Suggestions / Misspelled? <≤3 words>
 *    exactly as assignment rules state.
 *
 * Supported dictionary input formats
 *   1)  N  <word1> … <wordN>   (N first, may span lines)
 *   2)  <word1> … <wordK>       (single‑line dictionary)
 *
 * After the dictionary, every token is treated as a *query*
 * until the sentinel EXIT appears.
 *
 * Uses **only java.util** (Scanner + collections).
 */
public class SpellChecker {
    /* ----- Trie node ----- */
    private static class Node {
        Node[] next = new Node[26];  // children for 'a'…'z'
        boolean isWord;              // end‑marker
    }

    private final Node root = new Node();
    private static final int LIMIT = 3; // max #suggestions

    /* ----- insert word into trie ----- */
    private void insert(String w) {
        if (w.isEmpty()) return;
        Node cur = root;
        for (char ch : w.toCharArray()) {
            int idx = ch - 'a';
            if (cur.next[idx] == null) cur.next[idx] = new Node();
            cur = cur.next[idx];
        }
        cur.isWord = true;
    }

    /* ----- keep letters & lowercase ----- */
    private static String clean(String tok) {
        StringBuilder sb = new StringBuilder();
        for (char c : tok.toCharArray()) if (Character.isLetter(c)) sb.append(Character.toLowerCase(c));
        return sb.toString();
    }

    /* ----- process a single query token ----- */
    private void handleQuery(String raw) {
        String w = clean(raw);
        if (w.isEmpty()) return;

        // follow as far as possible, recording depth
        Node cur = root;
        int depth = 0;
        for (char ch : w.toCharArray()) {
            int idx = ch - 'a';
            if (idx < 0 || idx >= 26 || cur.next[idx] == null) break;
            cur = cur.next[idx];
            depth++;
        }

        if (depth == w.length() && cur.isWord) {            // exact hit
            System.out.println("Correct Word");
            return;
        }
        if (depth == 0) {                                   // no first letter
            System.out.println("No Suggestions");
            return;
        }

        List<String> out = new ArrayList<>(LIMIT);
        dfs(cur, new StringBuilder(w.substring(0, depth)), out);
        System.out.println(out.isEmpty() ? "No Suggestions" : "Misspelled? " + String.join(" ", out));
    }

    /* ----- lexicographic DFS limited to LIMIT results ----- */
    private void dfs(Node n, StringBuilder sb, List<String> out) {
        if (out.size() == LIMIT) return;
        if (n.isWord) out.add(sb.toString());
        for (char c = 'a'; c <= 'z' && out.size() < LIMIT; c++) {
            Node nxt = n.next[c - 'a'];
            if (nxt != null) {
                sb.append(c);
                dfs(nxt, sb, out);
                sb.deleteCharAt(sb.length() - 1); // backtrack
            }
        }
    }

    /* ----- main: Scanner‑based I/O ----- */
    public static void main(String[] args) {
        SpellChecker sp = new SpellChecker();
        Scanner sc = new Scanner(System.in);

        /* 1) read first non‑blank line (dictionary header) */
        String firstLine = "";
        while (sc.hasNextLine()) {
            firstLine = sc.nextLine().trim();
            if (!firstLine.isEmpty()) break;               // skip blank lines
        }
        if (firstLine.isEmpty()) return;                   // empty input

        Scanner lineScan = new Scanner(firstLine);
        String firstTok = lineScan.next();

        // ---- Format‑A: N words ----
        if (firstTok.chars().allMatch(Character::isDigit)) {
            int N = Integer.parseInt(firstTok);
            int read = 0;
            while (read < N) {
                if (lineScan.hasNext()) {
                    sp.insert(clean(lineScan.next()));
                    read++;
                } else if (sc.hasNext()) {                 // spill to next tokens
                    sp.insert(clean(sc.next()));
                    read++;
                } else break;
            }
        }
        // ---- Format‑B: entire first line is dictionary ----
        else {
            sp.insert(clean(firstTok));
            while (lineScan.hasNext()) sp.insert(clean(lineScan.next()));
        }

        /* 2) every following token is a query until EXIT */
        while (sc.hasNext()) {
            String tok = sc.next();
            if ("EXIT".equals(tok)) break;
            sp.handleQuery(tok);
        }
    }
}
