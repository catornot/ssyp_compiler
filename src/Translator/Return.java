package Translator;

public class Return extends SingleBytecodeInstructionTemplate {
    public Return(int arg) {
        super(arg, "RETURN");
    }
}