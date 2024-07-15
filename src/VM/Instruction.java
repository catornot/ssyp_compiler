package VM;

public interface Instruction {
    void execute(VmRuntime stack);

    void println();
}
