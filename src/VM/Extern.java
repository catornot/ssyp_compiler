package VM;

public class Extern implements Instruction {

    @Override
    public void execute(VmRuntime runtime) {

        // basic print
        if (runtime.getCurrentFunctionName().equals("print")) {
            for (int i = -1; ; i--) {
                int obj = runtime.stackAt(i);

                if (obj == 0) {
                    break;
                }

                System.out.println(obj);
            }

            runtime.returnWith(0);
            return;
        }

        runtime.returnWith(0);
    }

    @Override
    public void println() {
        System.out.println("EXTERN");
    }
}
