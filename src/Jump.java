public class Jump extends MultiBytecodeInstructionTemplate {
    public Jump(CompareTypes type, int arg1, int arg2, int line) {
        super(new int[] {type.ordinal(), arg1, arg2, line}, "JMP");
    }
}
