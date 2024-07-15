package Parsing;

public record Function(String name, Variable[] arguments, Variable[] locals, Instruction[] instructions) {

    @Override
    public String toString() {
        String[] args = new String[arguments.length];
        String[] vars = new String[locals.length];
        String[] instructs = new String[instructions.length];
        for (int k = 0; k < args.length; k++) {
            args[k] = arguments[k].toString();
        }
        for (int k = 0; k < vars.length; k++) {
            vars[k] = locals[k].toString();
        }
        for (int k = 0; k < instructs.length; k++) {
            instructs[k] = instructions[k].toString();
        }
        return String.format("""
                        Function %s:
                            ARGS: %s
                            VARS: %s
                            INSTRUCTS: %s
                        """, name,
                String.join(", ", args),
                String.join(", ", vars),
                String.join("\n\t\t", instructs));
    }
}
