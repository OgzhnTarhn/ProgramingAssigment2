import java.util.*;

public class SpellChecker {

    private final Node root = new Node();
    private static final int LIMIT = 3;

    //Kelimedeki harflere sırayla bakılarak trie içinde gezilir, uygun düğüm yoksa ilerleme durur
    private void handleQuery(String raw) {
        String word = clean(raw);
        if (word.isEmpty()) return;

        Node current = root;
        int depth = 0;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (index < 0 || index >= 26 || current.next[index] == null) break;
            current = current.next[index];
            depth++;
        }

        if (depth == word.length() && current.isWord) {
            System.out.println("Correct Word");
            return;
        }
        if (depth == 0) {
            System.out.println("No Suggestions");
            return;
        }

        //İflere girmedi o yüzden öneri oluşturmamız gerekli (prefix'e göre)(MAX 3 tane)
        List<String> out = new ArrayList<>(LIMIT);
        dfs(current, new StringBuilder(word.substring(0, depth)), out);
        if (out.isEmpty()) {
            System.out.println("No Suggestions");
        } else {
            StringBuilder result = new StringBuilder("Misspelled? ");
            for (int i = 0; i < out.size(); i++) {
                if (i > 0) result.append(" ");
                result.append(out.get(i));
            }
            System.out.println(result.toString());
        }
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

    //trie yapısına girilen kelimeyi harfleri üzerinden dolaşarak ekliyoruz
    private void insert(String word) {
        if (word.isEmpty()) return;
        Node current = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (current.next[index] == null) current.next[index] = new Node();
            current = current.next[index];
        }
        current.isWord = true;
    }

    //harf dışı karakterleri temizlemek ve kalan harfleri küçük harflere çevirmek
    private static String clean(String tok) {
        StringBuilder sb = new StringBuilder();
        for (char c : tok.toCharArray()) if (Character.isLetter(c)) sb.append(Character.toLowerCase(c));
        return sb.toString();
    }

    //ilk kelime sayı içeriyorsa, o sayı kadar kelime okur ve trie'a ekler
    //ilk kelime sayı değilse tüm kelimeler doğrudan trie'a eklenir
    //Sonra kullanıcıdan sorgular alınır
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

        boolean isAllDigits = true;
        for (char c : firstTok.toCharArray()) {
            if (!Character.isDigit(c)) {
                isAllDigits = false;
                break;
            }
        }

        if (isAllDigits) {
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
