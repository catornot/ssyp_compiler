package Translator;

public abstract class BinaryBytecodeInstruction implements BytecodeInstruction {
    private final int arg1;
    private final int arg2;
    protected BinaryBytecodeInstruction(int arg1, int arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    int arg1() {
        return this.arg1;
    }

    int arg2() {
        return this.arg2;
    }
}
