package Parsing;

import java.util.Optional;

public class Instruction {
    private final InstructionType instructionType;
    private final Either<String, Integer>[] vars;
    private final Optional<String> functionName;

    Instruction(InstructionType instructionType, Either<String, Integer>[] vars, Optional<String> functionName) {
        this.instructionType = instructionType;
        this.vars = vars;
        this.functionName = functionName;
    }


    public InstructionType type() {
        return this.instructionType;
    }

    public Optional<Either<String, Integer>> get(int index) {
        if (index < this.vars.length) {
            return Optional.of(this.vars[index]);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> functionName() {
        return this.functionName;
    }
  
    @Override
    public String toString() {
        String[] words = new String[1 + vars.length];
        words[0] = functionName + ": " + instructionType.toString();
        for (int k = 0; k < words.length - 1; k++) {
            words[k + 1] = vars[k].toString();
        }
        return String.join(" ", words);
    }
}
