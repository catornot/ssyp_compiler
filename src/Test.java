import Parsing.Function;
import Parsing.Parser;
import Translator.Translator;
import Translator.BytecodeFile;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<String> tokens = Lexer.tokenizeFromFile("test.txt");
        Parser parser = new Parser(tokens);
        Function[] functions = parser.getFunctions();
        for (Function function: functions) {
            System.out.println("\n" + function.toString());
        }
      
        BytecodeFile file = new BytecodeFile("out/bytecode.bc");
        file.init();

        Translator.translate(functions, file);

        file.write();
    }
}
