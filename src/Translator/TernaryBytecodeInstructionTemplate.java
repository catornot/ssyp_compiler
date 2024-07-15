package Translator;

public class TernaryBytecodeInstructionTemplate extends TernaryBytecodeInstruction {

    private final String instruction;

    public TernaryBytecodeInstructionTemplate(int arg1, int arg2, int arg3, String instruction) {
        super(arg1, arg2, arg3);
        this.instruction = instruction;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.valueOf(instruction);
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d", this.instruction, this.arg1(), this.arg2(), this.arg3());
    }
}
