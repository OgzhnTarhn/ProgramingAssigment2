import java.util.*;

/**
 * CENG 383 – Programming Assignment 2
 * -----------------------------------
 * SpellChecker
 *
 * <p>Implements a dictionary‑based spell checker using an R‑way (26‑ary) trie.
 * Input is entirely read from <code>stdin</code>, so <strong>no extra command‑line
 * arguments or external text files are required</strong>.</p>
 *
 * <h2>Input Format</h2>
 * <ol>
 *   <li>First line – an integer <code>N</code>: number of words in the dictionary</li>
 *   <li>Next <code>N</code> tokens – dictionary words (case‑insensitive)</li>
 *   <li>Remaining tokens – query words, one per token
 *       (<code>EXIT</code> <em>in all capitals</em> terminates the program)</li>
 * </ol>
 *
 * <h2>Output Rules</h2>
 * <ul>
 *   <li>If the query exists in the dictionary → <code>Correct Word</code></li>
 *   <li>If no dictionary word starts with the same first letter →
 *       <code>No Suggestions</code></li>
 *   <li>Otherwise → <code>Misspelled?</code> followed by up to three suggestions
 *       with the longest common prefix, printed in strictly alphabetical order
 *       and separated by single spaces.</li>
 * </ul>
 *
 * <p>All operations run in time O(<i>L</i>) where <i>L</i> is the length of the
 * query, plus the number of extra nodes visited while collecting at most three
 * suggestions (bounded by a small constant in practice).</p>
 *
 * @author  <your‑name‑here>
 * @version 1.0  (May 2025)
 */
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
