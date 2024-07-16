package Translator;

import Parsing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Translator {
    public static BytecodeFile translate(Function[] functions, BytecodeFile file) {
        for (Function func : functions) {
            generateFunction(func, file);
        }

        file.add_func("print");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        return file;
    }

    private static void generateFunction(Function func, BytecodeFile file) {
        ArrayList<String> virtualStack = new ArrayList<>();
        virtualStack.addAll(Arrays.stream(func.arguments()).map(Variable::name).toList());
        virtualStack.addAll(Arrays.stream(func.locals()).map(Variable::name).toList());

        file.add_func(func.name());

        ArrayList<Block> block = new ArrayList<>();
        ArrayList<BytecodeInstruction> instructions = new ArrayList<>();
        for (Instruction ins : func.instructions()) {
            generateInstruction(ins, block, instructions, virtualStack);
        }

        if (!block.isEmpty()) {
            System.out.println("unclosed block found");
            throw new RuntimeException();
        }

        for (BytecodeInstruction bytecode : instructions) {
            file.add_instructions(bytecode);
        }

        if (func.instructions()[func.instructions().length - 1].type() != InstructionType.RETURN) {
            file.add_instructions(new Return(0));
        }
    }

    private static void generateInstruction(Instruction ins, ArrayList<Block> blocks, ArrayList<BytecodeInstruction> instructions, ArrayList<String> virtualStack) {
        switch (ins.type()) {
            case ADD -> instructions.add(
                    new Add(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case SUB -> instructions.add(
                    new Sub(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case DIV -> instructions.add(
                    new Div(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case MUL -> instructions.add(
                    new Mul(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case ASSIGN -> {
                Optional<Integer> arg1 = getVarOnlyAddress(ins, 0, virtualStack);
                if (arg1.isEmpty()) {
                    System.out.println();
                    throw new RuntimeException();
                }

                Optional<Integer> arg2 = getVarOnlyAddress(ins, 1, virtualStack);
                arg2.ifPresent(arg -> instructions.add(
                        new Mov(arg1.get(),
                                arg
                        )
                ));

                if (arg2.isEmpty()) {
                    instructions.add(
                            new Mov(getVarAddress(ins, 0, virtualStack, instructions),
                                    ins.get(1).flatMap(Either::getRight).get()
                            )
                    );
                }
            }
            case CALL -> {
                Optional<Integer> returnArgsAddress = getVarOnlyAddress(ins, 0, virtualStack);

                // gather address of args
                int i = returnArgsAddress.isEmpty() ? 0 : 1;
                ArrayList<Integer> varAddress = new ArrayList<>();
                while (ins.get(i).isPresent()) {
                    varAddress.add(getVarAddress(ins, i, virtualStack, instructions));
                    i++;
                }

                // MOV instructions
                int functionFreeSpace = -virtualStack.size() - 1; // invert sign
                for (int e : IntStream.range(0, varAddress.size()).toArray()) {
                    instructions.add(new Mov(varAddress.get(e),
                            functionFreeSpace - e - 1
                    ));
                }

                // CALL instruction
                ins.functionName().ifPresent(functionName -> instructions.add(
                        new Call(functionName,
                                functionFreeSpace
                        )
                ));

                // SET return
                returnArgsAddress.ifPresent(returnAdr -> instructions.add(
                        new Mov(functionFreeSpace,
                                returnAdr
                        )
                ));
            }

            case RETURN -> instructions.add(
                    new Return(getVarAddress(ins, 0, virtualStack, instructions))
            );

            case IF -> {
                ins.get(0).flatMap(Either::getLeft).ifPresentOrElse(cmpType -> instructions.add(
                        new Jump(CompareTypes.fromSymbol(cmpType).invert(),
                                getVarAddress(ins, 1, virtualStack, instructions),
                                getVarAddress(ins, 2, virtualStack, instructions),
                                Integer.MAX_VALUE
                        )
                ), () -> {
                    System.out.println("if should have a cmp operand");
                    throw new RuntimeException();
                });

                blocks.add(new Block(instructions.size() - 1));
            }

            case ELIF -> {
                ins.get(0).flatMap(Either::getLeft).ifPresentOrElse(cmpType -> instructions.add(
                        new Jump(CompareTypes.fromSymbol(cmpType).invert(),
                                getVarAddress(ins, 1, virtualStack, instructions),
                                getVarAddress(ins, 2, virtualStack, instructions),
                                Integer.MAX_VALUE
                        )
                ), () -> {
                    System.out.println("if should have a cmp operand");
                    throw new RuntimeException();
                });

                blocks.getLast().mid.add(instructions.size() - 1);
            }

            case ELSE -> blocks.getLast().elseStart = Optional.of(instructions.size()); // TODO: check for correctness

            case ENDIF -> {
                blocks.getLast().end = instructions.size(); // TODO: check for correctness

                blocks.getLast().fixJumps(instructions);
                blocks.removeLast();
            }
        }
    }

    private static int getVarAddress(Instruction ins, int index, ArrayList<String> virtualStack, ArrayList<BytecodeInstruction> instructions) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                return -virtualStack.indexOf(arg.getLeft().get());
            } else if (arg.getRight().isPresent()) {
                Optional<Integer> lit = arg.getRight();
                return -orCreateStack(lit.get(), virtualStack, instructions);
            } else {
                System.out.println("all variants of either are not valid");
                throw new RuntimeException();
            }
        } else {
            System.out.printf("missing arg %d for %s\n", index, ins.type().toString());
            throw new RuntimeException();
        }
    }

    private static Optional<Integer> getVarOnlyAddress(Instruction ins, int index, ArrayList<String> virtualStack) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                int adr = virtualStack.indexOf(arg.getLeft().get());
                if (adr == -1) {
                    System.out.printf("var %s is not found on stack\n", arg.getLeft().get());
                    throw new RuntimeException();
                }
                return Optional.of(-adr);
            } else if (arg.getRight().isPresent()) {
                return Optional.empty();
            } else {
                System.out.println("all variants of either are not valid");
                throw new RuntimeException();
            }
        } else {
            System.out.printf("missing arg %d for %s\n", index, ins.type().toString());
            throw new RuntimeException();
        }
    }

    private static int orCreateStack(int literal, ArrayList<String> virtualStack, ArrayList<BytecodeInstruction> instructions) {
        int pos = virtualStack.indexOf("#" + literal);

        if (pos == -1) {
            virtualStack.add("#" + literal); // literals will have a # before them
            pos = (virtualStack.size()) - 1;
          
            instructions.add(new Set(-pos, literal));
        }
        
        return pos;
    }
}

class Block {
    int start;
    ArrayList<Integer> mid;
    Optional<Integer> elseStart;
    int end;

    Block(int start) {
        this.start = start;
        this.mid = new ArrayList<>();
        this.end = Integer.MAX_VALUE;
        this.elseStart = Optional.empty();
    }

    public void fixJumps(ArrayList<BytecodeInstruction> instructions) {
        assert this.end != Integer.MAX_VALUE;

        if (instructions.get(this.start) instanceof Jump) {
            ((Jump) instructions.get(this.start)).setJumpDestination(this.mid.getFirst() == null ? this.elseStart.orElseGet(() -> this.end) : this.mid.getFirst());
        } else {
            System.out.println("invalid if jmp instruction at index");
            throw new RuntimeException();
        }

        for (int i : this.mid) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.mid.size() > i + 1 ? this.elseStart.orElseGet(() -> this.end) : this.mid.get(i + 1));
            } else {
                System.out.println("invalid else if jmp instruction at index");
                throw new RuntimeException();
            }
        }
    }
}
