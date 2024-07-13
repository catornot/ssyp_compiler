package VM;

public class VM {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("provide a file argument");
            return;
        }

        InputReader reader = new InputReader(args[0]);

        (new VmRuntime(reader, 5)).run();
//        while( reader.hasNext()) {
//            System.out.println(reader.nextInstruction());
//        }
    }
}