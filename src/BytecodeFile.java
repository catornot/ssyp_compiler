import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BytecodeFile {
    private final String filename;
    private final HashMap<String, Integer> functions;
    private final ArrayList<String> BytecodeLines;
    public BytecodeFile(String filename) {
        this.filename = filename;
        functions = new HashMap<>();
        BytecodeLines = new ArrayList<>();
    }
    void init() {
        try {
            File file = new File(this.filename);
            if (file.createNewFile()) {
                System.out.println("Создан новый файл.");
            }
        } catch (IOException error) {
            System.out.println("Локация ошибки:");
            error.getLocalizedMessage();
        }
    }

    public void add_func(String func_name) {
        functions.put(func_name, functions.size());
        BytecodeLines.add(func_name + ": ");
    }

    public void add_instructions(String func_name, BytecodeInstruction instruction) {
        int line = functions.get(func_name);
        String last_str = BytecodeLines.get(line);
        BytecodeLines.set(line, last_str + instruction.toString() + " ");
    }

    public void write() {
        try {
            FileWriter writer = new FileWriter(this.filename);
            writer.write(String.join("\n", BytecodeLines));
            writer.close();
            System.out.println("Данные записаны.");
        } catch (IOException error) {
            System.out.println("Локация ошибки:");
            error.getLocalizedMessage();
        }
    }
}
