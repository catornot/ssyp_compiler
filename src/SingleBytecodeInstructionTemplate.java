public class SingleBytecodeInstructionTemplate extends SingleBytecodeInstruction {
    private final String instruction;

    public SingleBytecodeInstructionTemplate(int arg, String instruction) {
        super(arg);
        this.instruction = instruction;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.valueOf(instruction);
    }

    @Override
    public String toString() {
        return String.format("%s %d", this.instruction, this.arg());
    }
}
