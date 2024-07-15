package Translator;

public class BinaryBytecodeInstructionTemplate extends BinaryBytecodeInstruction {

    private final String instruction;

    public BinaryBytecodeInstructionTemplate(int arg1, int arg2, String instruction) {
        super(arg1, arg2);
        this.instruction = instruction;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.valueOf(this.instruction);
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", this.instruction, this.arg1(), this.arg2());
    }
}
