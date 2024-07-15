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

        for (Instruction ins : func.instructions()) {
            switch (ins.type()) {
                case ADD -> file.add_instructions(
                        new Add(getVarAddress(ins, 0, virtualStack, file),
                                getVarAddress(ins, 1, virtualStack, file),
                                getVarAddress(ins, 2, virtualStack, file))
                );


                case SUB -> file.add_instructions(
                        new Sub(getVarAddress(ins, 0, virtualStack, file),
                                getVarAddress(ins, 1, virtualStack, file),
                                getVarAddress(ins, 2, virtualStack, file))
                );

                case DIV -> file.add_instructions(
                        new Div(getVarAddress(ins, 0, virtualStack, file),
                                getVarAddress(ins, 1, virtualStack, file),
                                getVarAddress(ins, 2, virtualStack, file))
                );

                case MUL -> file.add_instructions(
                        new Mul(getVarAddress(ins, 0, virtualStack, file),
                                getVarAddress(ins, 1, virtualStack, file),
                                getVarAddress(ins, 2, virtualStack, file))
                );

                case ASSIGN -> {
                    Optional<Integer> arg1 = getVarOnlyAddress(ins, 0, virtualStack);
                    if (arg1.isEmpty()) {
                        System.out.println();
                        throw new RuntimeException();
                    }

                    Optional<Integer> arg2 = getVarOnlyAddress(ins, 1, virtualStack);
                    arg2.ifPresent(arg -> file.add_instructions(
                            new Mov(arg1.get(),
                                    arg
                            )
                    ));

                    if (arg2.isEmpty()) {
                        file.add_instructions(
                                new Mov(getVarAddress(ins, 0, virtualStack, file),
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
                        varAddress.add(getVarAddress(ins, i, virtualStack, file));
                        i++;
                    }

                    // MOV instructions
                    int functionFreeSpace = -virtualStack.size() - 1; // invert sign
                    for (int e : IntStream.range(0, varAddress.size()).toArray()) {
                        file.add_instructions(new Mov(varAddress.get(e),
                                functionFreeSpace - e - 1
                        ));
                    }

                    // CALL instruction
                    ins.functionName().ifPresent(functionName -> file.add_instructions(
                            new Call(functionName,
                                    functionFreeSpace
                            )
                    ));

                    // SET return
                    returnArgsAddress.ifPresent(returnAdr -> file.add_instructions(
                            new Mov(functionFreeSpace,
                                    returnAdr
                            )
                    ));
                }

                case RETURN -> file.add_instructions(
                        new Return(getVarAddress(ins, 0, virtualStack, file))
                );
            }
        }

        if (func.instructions()[func.instructions().length - 1].type() != InstructionType.RETURN) {
            file.add_instructions(new Return(0));
        }
    }

    private static int getVarAddress(Instruction ins, int index, ArrayList<String> virtualStack, BytecodeFile file) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                return -virtualStack.indexOf(arg.getLeft().get());
            } else if (arg.getRight().isPresent()) {
                Optional<Integer> lit = arg.getRight();
                return -orCreateStack(lit.get(), virtualStack, file);
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

    private static int orCreateStack(int literal, ArrayList<String> virtualStack, BytecodeFile file) {
        int pos = virtualStack.indexOf("#" + literal);

        if (pos == -1) {
            virtualStack.add("#" + literal); // literals will have a # before them
            pos = virtualStack.size() - 1;
        }
        file.add_instructions(new Set(pos, literal));
        return pos;
    }
}
