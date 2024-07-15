import Parsing.Function;
import Parsing.Parser;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        Lexer lexer = new Lexer(new String[]{"@", "#", ";"});
        List<String> tokens = lexer.getTokens("test.txt");
        Parser parser = new Parser(tokens);
        Function[] functions = parser.getFunctions();
        for (Function function: functions) {
            System.out.println("\n" + function.toString());
        }
    }
}
