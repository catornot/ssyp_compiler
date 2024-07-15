public abstract class MultiBytecodeInstruction implements BytecodeInstruction {
    private final int[] args;

    protected MultiBytecodeInstruction(int[] args) {
        this.args = args;
    }

    int[] args() {
        return this.args;
    }
}
