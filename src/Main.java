import Parsing.Function;
import Parsing.Parser;
import Parsing.Program;
import Parsing.Struct;
import Translator.BytecodeFile;
import Translator.Translator;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("provide a file argument");
            return;
        }

        List<String> tokens = Lexer.tokenizeFromFile(args[0]);

        for (String tk : tokens) {
            System.out.println(tk);
        }

        Parser parser = new Parser(tokens);
        Function[] functions = parser.getFunctions();
        Struct[] structs = parser.getStructs();

        // TODO: support different bytecode output
        BytecodeFile file = new BytecodeFile(STR."\{args[0]}.bc");
        file.init();

        Translator.translate(new Program(functions, structs), file);

        file.write();
    }
}
