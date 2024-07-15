package Translator;

public class Call implements BytecodeInstruction {

    private final String callingName;
    public int freeSpace;

    public Call(String functionName, int freeSpace) {
        this.callingName = functionName;
        this.freeSpace = freeSpace;
    }

    @Override
    public BytecodeType kind() {
        return BytecodeType.CALL;
    }

    @Override
    public String toString() {
         return String.join(" ", "CALL", this.callingName, this.freeSpace + "");
    }
}
