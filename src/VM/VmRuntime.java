package VM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class VmRuntime {
    private final List<Integer> stack;
    private final List<Pair<String, Integer>> callStack;
    private final int endInstruction = Integer.MAX_VALUE;
    private final HashMap<String, List<Instruction>> instructions = new HashMap<>();
    private final boolean isDebugging;
    private final HashMap<Integer, Integer[]> heap = new HashMap<>();
    private List<Instruction> currentInstructions;
    private String currentFunctionName = "main";
    private int stackBase;
    private int stackBottom;
    private int currentInstruction = 0;
    private int thisKey = 0xFFF;

    private final ConservativeGC gc;

    VmRuntime(InputReader reader, int size, boolean isDebugging) {
        this.stack = new ArrayList<>(size + 1);
        this.callStack = new ArrayList<>(size + 1);
        this.isDebugging = isDebugging;


        for (int i : IntStream.range(0, size + 1).toArray()) {
            this.stack.add(0);
        }

        this.stack.set(size, this.endInstruction);
        this.stackBase = size - 1;
        this.stackBottom = size - 1;

        while (reader.hasNext()) {
            Pair<String, ArrayList<Instruction>> pair = reader.nextFunction();
            this.instructions.put(pair.get0(), pair.get1());
        }

        if (!this.instructions.containsKey("main")) {
            System.out.println("entry function main doesn't exist");
            throw new RuntimeException();
        }

        this.callStack.add(new Pair<>(currentFunctionName, this.currentInstruction));
        this.currentInstructions = this.instructions.get(currentFunctionName);

        this.gc = new ConservativeGC();
    }

    public void run() {
        while (this.currentInstructions.size() > this.currentInstruction) {
            this.runNext();
            assert Integer.signum(this.currentInstruction) == 1;
        }
    }

    private void runNext() {
        if (this.isDebugging)
            this.currentInstructions.get(this.currentInstruction).println(this);

        this.currentInstructions.get(this.currentInstruction).execute(this);
        this.currentInstruction++;
    }

    public int stackAt(int offset) {
        this.stackBottom = Math.min(this.stackBottom, this.stackBase + offset);
        return this.stack.get(this.stackBase + offset);
    }

    public void setStackAt(int offset, int value) {
        int stackField = this.stackBase + offset;
        this.stackBottom = Math.min(this.stackBottom, stackField);

        this.stack.set(stackField, value);
    }

    public void enterNewFunction(String functionLabel, int offset) {
        this.callStack.getLast().set1(this.currentInstruction);

        if (!this.instructions.containsKey(functionLabel)) {
            System.out.println(STR."function \{functionLabel} doesn't exist");
            throw new RuntimeException();
        }

        this.currentInstructions = this.instructions.get(functionLabel);

        assert Integer.signum(offset) == -1;

        int previousStackBase = this.stackBase;
        this.stackBase += offset - 1; // advance by one from the free slot
        this.currentInstruction = -1; // instruction will get incremented back to 0 after this

        this.setStackAt(1, previousStackBase);

        this.currentFunctionName = functionLabel;
        this.callStack.add(new Pair<>(this.currentFunctionName, 0));
    }

    public void returnWith(int obj) {
        this.stackBottom = this.stackBase;

        if (this.stackAt(1) == this.endInstruction || this.callStack.size() == 1) {
            this.currentInstruction = this.currentInstructions.size();
            this.stackBase = this.stack.size() - 1;

            System.out.println(STR."out: \{obj}");
        } else {
            this.callStack.removeLast(); // remove current function from stack
            this.currentInstruction = this.callStack.getLast().get1();
            this.currentFunctionName = this.callStack.getLast().get0();
            this.currentInstructions = this.instructions.get(this.currentFunctionName);

            int base = this.stackAt(1);
            this.setStackAt(1, obj);
            this.stackBase = base;
        }

        this.gc.cleanupHeap(this, obj);
    }

    public void jumpBy(int offset) {
        this.currentInstruction = offset;

        assert Integer.signum(this.currentInstruction) == 1 && this.currentInstruction < this.currentInstructions.size();

        this.currentInstruction--; // TODO: check for correctness
    }

    public String getCurrentFunctionName() {
        return this.currentFunctionName;
    }

    public int createNewArray(int size) {
        heap.put(thisKey, new Integer[size]);
        thisKey++;
        return thisKey - 1;
    }

    public void updateArray(int key, int index, int value) {
        heap.get(key)[index] = value;
    }

    public void changeArray(int key, Integer[] newArray) {
        heap.put(key, newArray);
    }

    public int writeStr(String string) {
        Integer[] array = new Integer[string.length()];
        char[] char_array = string.toCharArray();
        for (int k = 0; k < string.length(); k++) {
            array[k] = ((int) char_array[k]);
        }
        int stringKey = createNewArray(array.length);
        changeArray(stringKey, array);
        return stringKey;
    }

    public Integer[] getArray(int key) {
        return heap.get(key);
    }

    public void DestroyArray(int key) {
        heap.remove(key);
    }

    public int getInstructionNumber() {
        return this.currentInstruction;
    }

    public boolean isDebugging() {
        return this.isDebugging;
    }

    public List<Integer> getRawStackView() {
        return this.stack.subList(this.stackBottom, this.stack.size());
    }

    public Set<Integer> getRawHeapPages() {
        return this.heap.keySet();
    }
}