package Translator;

public abstract class SingleBytecodeInstruction implements BytecodeInstruction {
    private final int arg;

    protected SingleBytecodeInstruction(int arg) {
        this.arg = arg;
    }

    int arg() {
        return this.arg;
    }
}
