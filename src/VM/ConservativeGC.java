package VM;

import java.util.ArrayList;
import java.util.Arrays;

public class ConservativeGC {
    public void cleanupHeap(VmRuntime runtime, int extraKey) {
        ArrayList<Integer> seenArrays = new ArrayList<>(4);
        seenArrays.add(extraKey);

        for (int slotValue : runtime.getRawStackView()) {
            if (runtime.getArray(slotValue) != null && !seenArrays.contains(slotValue)) {
                seenArrays.add(slotValue);
            }
        }

        for (int key : runtime.getRawHeapPages().stream().toList()) {
            if (!seenArrays.contains(key)) {
                if (runtime.isDebugging()) {
                    System.out.println(STR."Destroyed key:\{key} len:\{runtime.getArray(key).length}");
                }

                runtime.DestroyArray(key);
            }
        }
    }
}
