package VM;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class Extern implements Instruction {

    @Override
    public void execute(VmRuntime runtime) {

        // basic print
        if (runtime.getCurrentFunctionName().equals("print")) {
            int len = -(runtime.stackAt(0) + 1);
            for (int i = -1; i > len; i--) {
                int obj = runtime.stackAt(i);

                if (i != 0) {
                    System.out.print(", ");
                }

                System.out.print(obj);
            }

            System.out.print("\n");

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("print_array")) {
            Integer[] array = runtime.getArray(runtime.stackAt(0));

            assert array != null;

            System.out.println(Arrays.toString(array));
            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("print_string")) {
            Integer[] charArray = runtime.getArray(runtime.stackAt(-1));
            if (charArray == null) {
                runtime.returnWith(0);
                return;
            }

            for (int c : charArray) {
                System.out.print((char) c);
            }
            System.out.print("\n");

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("print_char")) {
            System.out.println((char) runtime.stackAt(-1));
        }

        if (runtime.getCurrentFunctionName().equals("len")) {
            Integer[] array = runtime.getArray(runtime.stackAt(0));

            assert array != null;

            runtime.returnWith(array.length);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("range")) {
            int size, rangeOffset, value;

            size = runtime.stackAt(-1) - runtime.stackAt(0);
            rangeOffset = runtime.createNewArray(size);
            value = runtime.stackAt(0);

            for (int k = 0; k < size && value < runtime.stackAt(-1); k++) {
                runtime.updateArray(rangeOffset, k, value);
                value += runtime.stackAt(-2);
            }

            runtime.returnWith(rangeOffset);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("assert_eq")) {
            assert runtime.stackAt(0) == runtime.stackAt(1);

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("clear_out")) {
            System.out.print("\033[H\033[J");
            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("sleep")) {
            int timeout = runtime.stackAt(0);
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                System.out.println("sleep interrupted");
            }
            runtime.returnWith(0);
            return;
        }

        runtime.returnWith(0);
    }

    @Override
    public void println(VmRuntime stack) {
        System.out.println("EXTERN");
    }
}
