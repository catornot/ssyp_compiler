package Translator;

public class Jump implements BytecodeInstruction {

    private final CompareTypes cmpType;
    private final int leftCmp;
    private final int rightCmp;
    private int destination;

    public Jump(CompareTypes type, int leftCmp, int rightCmp, int destination) {
        this.cmpType = type;
        this.leftCmp = leftCmp;
        this.rightCmp = rightCmp;
        this.destination = destination;
    }

    public void setJumpDestination(int destination) {
        this.destination = destination;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.JMP;
    }

    @Override
    public String toString() {
        return String.format("JMP %d %d %d %d", this.cmpType.ordinal(), this.leftCmp, this.rightCmp, this.destination);
    }
}
