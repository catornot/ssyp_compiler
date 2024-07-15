import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Lexer {
    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        String splitBySemicolon = input.replace(";", ";\n");
        String[] splitByLine = splitBySemicolon.split("\n");
        List<String> splitSpcLine = new ArrayList<>();
        for (String elementOfSplitByLine : splitByLine) {
            String[] splitBySpc = elementOfSplitByLine.split(" ");
            splitSpcLine.addAll(Arrays.asList(splitBySpc));
        }
        for (String elementOfSplitSpcLine : splitSpcLine) {
            boolean isStartsWithSpecialSymbol = (elementOfSplitSpcLine.charAt(0) == '#' || elementOfSplitSpcLine.charAt(0) == '@');
            if (isStartsWithSpecialSymbol && elementOfSplitSpcLine.charAt(elementOfSplitSpcLine.length() - 1) == ';') {
                tokens.add(String.valueOf(elementOfSplitSpcLine.charAt(0)));
                tokens.add(elementOfSplitSpcLine.substring(1, elementOfSplitSpcLine.length() - 1));
                tokens.add(elementOfSplitSpcLine.substring(elementOfSplitSpcLine.length() - 1));
            } else if (isStartsWithSpecialSymbol) {
                tokens.add(String.valueOf(elementOfSplitSpcLine.charAt(0)));
                tokens.add(elementOfSplitSpcLine.substring(1));
            } else if (elementOfSplitSpcLine.charAt(elementOfSplitSpcLine.length() - 1) == ';') {
                tokens.add(elementOfSplitSpcLine.substring(0, elementOfSplitSpcLine.length() - 1));
                tokens.add(elementOfSplitSpcLine.substring(elementOfSplitSpcLine.length() - 1));

            } else {
                tokens.add(elementOfSplitSpcLine);
            }
        }
        return tokens;
    }

    // TODO refactor this
    public static List<String> tokenizeFromFile(String filename) {
        try {
            return tokenize(Files.readString(Path.of(filename), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}


