import java.util.*;

public class SpellChecker {

    /* --------------------  Trie Data Structure  -------------------- */

    /** Single node of a 26‑way trie (for letters 'a'–'z'). */
    private static class Node {
        Node[] next = new Node[26];      // child references
        boolean isWord;                  // marks end‑of‑word
    }

    private final Node root = new Node(); // trie root

    /** Inserts a word into the trie (assumes input is lowercase a‑z). */
    private void insert(String word) {
        Node cur = root;
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (cur.next[idx] == null) cur.next[idx] = new Node();
            cur = cur.next[idx];
        }
        cur.isWord = true;
    }

    /** Returns the node reached after following all characters of key, or null if a link is missing. */
    private Node searchNode(String key) {
        Node cur = root;
        for (char ch : key.toCharArray()) {
            int idx = ch - 'a';
            if (idx < 0 || idx >= 26 || cur.next[idx] == null) return null; // non‑letter or missing path
            cur = cur.next[idx];
        }
        return cur; // may be word or just prefix
    }

    /* --------------------  Spell‑check Operations  -------------------- */

    /**
     * Handles a single query: prints "Correct Word", "No Suggestions" or
     * "Misspelled? w1 w2 w3" according to assignment rules.
     */
    private void handleQuery(String query) {
        String word = query.toLowerCase();
        Node node = searchNode(word);

        // Case‑1: exact match found? (node exists & isWord flag set)
        if (node != null && node.isWord) {
            System.out.println("Correct Word");
            return;
        }

        // Case‑2: first letter missing in trie => no suggestions
        if (root.next[word.charAt(0) - 'a'] == null) {
            System.out.println("No Suggestions");
            return;
        }

        // Case‑3: generate up to 3 suggestions with longest matching prefix
        List<String> suggestions = collectSuggestions(node == null ? searchNode(word.substring(0, word.length() - 1)) : node, word);

        if (suggestions.isEmpty()) {
            System.out.println("No Suggestions");
        } else {
            System.out.print("Misspelled? ");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.print(suggestions.get(i));
                if (i != suggestions.size() - 1) System.out.print(' ');
            }
            System.out.println();
        }
    }

    /** Performs DFS from start node to collect ≤3 suggestions in alphabetical order. */
    private List<String> collectSuggestions(Node start, String prefix) {
        List<String> result = new ArrayList<>(3);
        if (start == null) return result;
        dfs(start, new StringBuilder(prefix), result);
        return result;
    }

    /** Helper DFS (lexicographic). Stops when result size reaches 3. */
    private void dfs(Node node, StringBuilder sb, List<String> out) {
        if (out.size() == 3) return;           // already enough suggestions
        if (node.isWord) out.add(sb.toString());
        for (char c = 'a'; c <= 'z' && out.size() < 3; c++) {
            Node next = node.next[c - 'a'];
            if (next != null) {
                sb.append(c);
                dfs(next, sb, out);
                sb.deleteCharAt(sb.length() - 1); // backtrack
            }
        }
    }

    /* --------------------  Main Driver  -------------------- */

    public static void main(String[] args) {
        SpellChecker sc = new SpellChecker();
        Scanner in = new Scanner(System.in);

        /* ---------- 1) Read dictionary ---------- */
        if (!in.hasNext()) return; // no input
        String firstToken = in.next();

        // Detect format — integer N or word list
        boolean firstIsInt = isInteger(firstToken);
        if (firstIsInt) {
            int N = Integer.parseInt(firstToken);
            for (int i = 0; i < N && in.hasNext(); i++) {
                sc.insert(cleanWord(in.next()));
            }
        } else {
            // format #1: first line already contains dictionary words.
            sc.insert(cleanWord(firstToken));
            // consume rest of the line (dictionary words)
            String restOfLine = in.nextLine();
            for (String w : restOfLine.split("\\s+")) {
                if (!w.isEmpty()) sc.insert(cleanWord(w));
            }
        }

        /* ---------- 2) Process queries until EXIT ---------- */
        while (in.hasNext()) {
            String query = in.next();
            if ("EXIT".equals(query)) break;            // terminate
            sc.handleQuery(query);
        }
    }

    /* --------------------  Utility Methods  -------------------- */

    /** Checks whether a string contains only digits (base‑10 integer). */
    private static boolean isInteger(String s) {
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return !s.isEmpty();
    }

    /** Normalises a token: keeps only lowercase letters a‑z. */
    private static String cleanWord(String token) {
        StringBuilder sb = new StringBuilder();
        for (char ch : token.toCharArray()) {
            if (Character.isLetter(ch)) sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }
}
