import java.util.*;

public class SpellChecker {

    private static class Node {
        Node[] next = new Node[26];
        boolean isWord;
    }

    private final Node root = new Node();
    private static final int LIMIT = 3;
    //trie yapısına girilen kelimeyi harfleri üzerinden dolaşarak ekliyoruz
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
    //harf dışı karakterleri temizlemek ve kalan harfleri küçük harflere çevirmek
    private static String clean(String tok) {
        StringBuilder sb = new StringBuilder();
        for (char c : tok.toCharArray()) if (Character.isLetter(c)) sb.append(Character.toLowerCase(c));
        return sb.toString();
    }
    //Kelimedeki harflere sırayla bakılarak trie içinde gezilir, uygun düğüm yoksa ilerleme durur
    private void handleQuery(String raw) {
        String w = clean(raw);
        if (w.isEmpty()) return;

        Node cur = root;
        int depth = 0;
        for (char ch : w.toCharArray()) {
            int idx = ch - 'a';
            if (idx < 0 || idx >= 26 || cur.next[idx] == null) break;
            cur = cur.next[idx];
            depth++;
        }

        if (depth == w.length() && cur.isWord) {
            System.out.println("Correct Word");
            return;
        }
        if (depth == 0) {
            System.out.println("No Suggestions");
            return;
        }

        List<String> out = new ArrayList<>(LIMIT);
        dfs(cur, new StringBuilder(w.substring(0, depth)), out);
        System.out.println(out.isEmpty() ? "No Suggestions" : "Misspelled? " + String.join(" ", out));
    }

    //ilgili düğümdeki alt dalları gezerek isWord=true olan kelimeleri bulmak
    //sınır 3 tane
    private void dfs(Node n, StringBuilder sb, List<String> out) {
        if (out.size() == LIMIT) return;
        if (n.isWord) out.add(sb.toString());
        for (char c = 'a'; c <= 'z' && out.size() < LIMIT; c++) {
            Node nxt = n.next[c - 'a'];
            if (nxt != null) {
                sb.append(c);
                dfs(nxt, sb, out);
                sb.deleteCharAt(sb.length() - 1); // backtrack (son harfi sil)
            }
        }
    }

    //ilk kelime sayı içeriyorsa, o sayı kadar kelime okur ve trie'a ekler
    //ilk kelime sayı değilse tüm kelimeler doğrudan trie'a eklenir
    public static void main(String[] args) {
        SpellChecker sp = new SpellChecker();
        Scanner sc = new Scanner(System.in);

        String firstLine = "";
        while (sc.hasNextLine()) {
            firstLine = sc.nextLine().trim();
            if (!firstLine.isEmpty()) break;
        }
        if (firstLine.isEmpty()) return;

        Scanner lineScan = new Scanner(firstLine);
        String firstTok = lineScan.next();

        if (firstTok.chars().allMatch(Character::isDigit)) {
            int N = Integer.parseInt(firstTok);
            int read = 0;
            while (read < N) {
                if (lineScan.hasNext()) {
                    sp.insert(clean(lineScan.next()));
                    read++;
                } else if (sc.hasNext()) {
                    sp.insert(clean(sc.next()));
                    read++;
                } else break;
            }
        }
        else {
            sp.insert(clean(firstTok));
            while (lineScan.hasNext()) sp.insert(clean(lineScan.next()));
        }

        while (sc.hasNext()) {
            String tok = sc.next();
            if ("EXIT".equals(tok)) break;
            sp.handleQuery(tok);
        }
    }
}
