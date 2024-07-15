package Translator;

public class Extern implements BytecodeInstruction {
    @Override
    public BytecodeType kind() {
        return BytecodeType.EXTERN;
    }

    @Override
    public String toString() {
        return "EXTERN";
    }
}
