package Translator;

public abstract class TernaryBytecodeInstruction implements BytecodeInstruction {
    private final int arg1;
    private final int arg2;
    private final int arg3;

    protected TernaryBytecodeInstruction(int arg1, int arg2, int arg3) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }



    int arg1() {
        return arg1;
    }
    int arg2() {
        return arg2;
    }
    int arg3() {
        return arg3;
    }
}
