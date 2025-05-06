import java.util.*;

public class SpellChecker {

    /* ------------------------------------------------------------
     *  Trie Node – fixed‑size array for 26 English letters
     * ---------------------------------------------------------- */
    private static class Node {
        Node[] next = new Node[26];
        boolean isWord;                 // true ⇔ full word ends here
    }

    /* Root of the trie */
    private final Node root = new Node();

    /* ------------------------------------------------------------
     *  Dictionary Construction
     * ---------------------------------------------------------- */
    /**
     * Inserts a lower‑case word into the trie.
     */
    private void insert(String word) {
        Node x = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') continue; // skip non‑letters (robustness)
            int idx = ch - 'a';
            if (x.next[idx] == null) x.next[idx] = new Node();
            x = x.next[idx];
        }
        x.isWord = true;
    }

    /**
     * @return true iff the lower‑case word exists in the trie.
     */
    private boolean contains(String word) {
        Node x = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') return false; // reject words with symbols
            x = x.next[ch - 'a'];
            if (x == null) return false;
        }
        return x.isWord;
    }

    /* ------------------------------------------------------------
     *  Suggestion Generation – longest‑prefix match (≤3 words)
     * ---------------------------------------------------------- */

    /**
     * Collects up to three suggestions that extend the deepest prefix of
     * <code>query</code>. Suggestions come out in alphabetical order thanks to
     * the ordered DFS from 'a' to 'z'.
     */
    private List<String> suggest(String query) {
        // 1) Walk as far as possible following the query characters
        Node x = root;
        StringBuilder prefix = new StringBuilder();
        for (char ch : query.toCharArray()) {
            if (ch < 'a' || ch > 'z') break;       // non‑letter cut‑off
            int idx = ch - 'a';
            if (x.next[idx] == null) break;        // mismatch – stop at longest
            prefix.append(ch);
            x = x.next[idx];
        }

        // No node exists even for the first letter ⇒ handled by caller

        // 2) DFS from {x} and gather first ≤3 words
        List<String> out = new ArrayList<>(3);
        dfsCollect(x, prefix, out);
        return out;
    }

    /** Recursive DFS used by {@link #suggest(String)}. */
    private void dfsCollect(Node x, StringBuilder sb, List<String> out) {
        if (out.size() == 3) return;          // cap at 3 suggestions
        if (x.isWord) out.add(sb.toString());
        for (char c = 'a'; c <= 'z' && out.size() < 3; c++) {
            Node nxt = x.next[c - 'a'];
            if (nxt != null) {
                sb.append(c);
                dfsCollect(nxt, sb, out);
                sb.deleteCharAt(sb.length() - 1); // back‑track
            }
        }
    }

    /* ------------------------------------------------------------
     *  Main Driver – reads stdin, prints answers, exits on "EXIT"
     * ---------------------------------------------------------- */
    public static void main(String[] args) {
        SpellChecker sc = new SpellChecker();
        Scanner in = new Scanner(System.in);

        if (!in.hasNextInt()) {
            System.err.println("First token must be the dictionary size (integer).");
            return;
        }
        int N = in.nextInt();
        for (int i = 0; i < N && in.hasNext(); i++) {
            sc.insert(in.next().toLowerCase());
        }

        /* Process queries */
        while (in.hasNext()) {
            String query = in.next();
            if ("EXIT".equals(query)) break;

            String lower = query.toLowerCase();

            // 1) Direct hit?
            if (sc.contains(lower)) {
                System.out.println("Correct Word");
                continue;
            }

            // 2) First‑letter missing ⇒ No Suggestions
            char first = lower.charAt(0);
            if (first < 'a' || first > 'z' || sc.root.next[first - 'a'] == null) {
                System.out.println("No Suggestions");
                continue;
            }

            // 3) Generate suggestions
            List<String> sug = sc.suggest(lower);
            if (sug.isEmpty()) {
                System.out.println("No Suggestions");
            } else {
                StringBuilder line = new StringBuilder("Misspelled?");
                for (String w : sug) line.append(' ').append(w);
                System.out.println(line);
            }
        }
    }
}
