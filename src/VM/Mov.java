package VM;

public class Mov implements Instruction {
    private final int offsetFrom;
    private final int offsetTo;

    Mov(int offsetFrom, int offsetTo) {
        this.offsetFrom = offsetFrom;
        this.offsetTo = offsetTo;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.setStackAt(this.offsetTo, runtime.stackAt(this.offsetFrom));
    }

    @Override
    public void println() {
        System.out.println("MOV");
    }
}
