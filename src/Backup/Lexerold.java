package Backup;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Lexerold {
    private String code = null;
    private final String[] key_combs;
    public String[] separators = {" ", "\n", "  "};
    public Boolean DO_STRIP = true;
    public Lexerold(String[] key_combs) {
        this.key_combs = key_combs;
    }


    public Stream<String> tokenize() {
        ArrayList<String> lexemes = new ArrayList<>(), code_fragments;
        lexemes.add(this.code);
        for (String sep: this.separators) {
            code_fragments = (ArrayList<String>) lexemes.clone();
            lexemes.clear();
            for (String elm: code_fragments) {
                if (elm != null) {
                    Collections.addAll(lexemes, elm.split(sep));
                }
            }
        }
        for (String comb: this.key_combs) {
            for (int k = 0; k < lexemes.size(); k++) {
                String lexeme = lexemes.get(k);
                if (lexeme.contains(comb) && !lexeme.equals(comb)) {
                    int index = lexemes.get(k).indexOf(comb);
                    lexemes.remove(k);
                    lexemes.add(k, lexeme.substring(index + 1));
                    lexemes.add(k, comb);
                    lexemes.add(k, lexeme.substring(0, index));
                }
            }
        }
        if (this.DO_STRIP) {
            lexemes.replaceAll(String::strip);
        }
        return lexemes.stream().filter(elm-> !Objects.equals(elm, ""));
    }

    public List<String> getTokens(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine().trim());
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла.");
        }
        Lexerold lexer = new Lexerold(
                this.key_combs
        );
        lexer.code = String.join("\n", lines);
        return lexer.tokenize().toList();
    }
}
