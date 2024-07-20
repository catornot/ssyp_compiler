package VM;

public class VM {
    static final boolean isDebugging = true;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("provide a file argument");
            return;
        }

        InputReader reader = new InputReader(args[0]);

        (new VmRuntime(reader, 512, VM.isDebugging || args.length > 1 && args[1].equals("true"))).run();
    }
}
