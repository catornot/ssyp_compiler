import java.util.Arrays;

public class MultiBytecodeInstructionTemplate extends MultiBytecodeInstruction {
    private final String instruction;

    public MultiBytecodeInstructionTemplate(int[] args, String instruction) {
        super(args);
        this.instruction = instruction;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.valueOf(instruction);
    }

    @Override
    public String toString() {
        return this.instruction + " " + String.join(" ", Arrays.toString(args()));
    }
}
