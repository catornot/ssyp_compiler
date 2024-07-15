package Translator;

import Parsing.Either;
import Parsing.Function;
import Parsing.Instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Translator {
    private static BytecodeFile translate(Function[] functions, BytecodeFile file) {
        for (Function func : functions) {
            generateFunction(func, file);
        }

        return file;
    }

    private static void generateFunction(Function func, BytecodeFile file) {
        ArrayList<String> virtualStack = new ArrayList<>();
        virtualStack.addAll(Arrays.stream(func.arguments).map(arg -> arg.name).toList());
        virtualStack.addAll(Arrays.stream(func.locals).map(local -> local.name).toList());

        file.add_func(func.name);

        for (Instruction ins : func.instructions) {
            switch (ins.type()) {
                case ADD -> {
                    file.add_instructions(
                            new Add(getVarAddress(ins, 0, virtualStack, file),
                                    getVarAddress(ins, 1, virtualStack, file),
                                    getVarAddress(ins, 2, virtualStack, file))
                    );

                }
                case SUB -> {
                    file.add_instructions(
                            new Sub(getVarAddress(ins, 0, virtualStack, file),
                                    getVarAddress(ins, 1, virtualStack, file),
                                    getVarAddress(ins, 2, virtualStack, file))
                    );
                }
                case DIV -> {
                    file.add_instructions(
                            new Div(getVarAddress(ins, 0, virtualStack, file),
                                    getVarAddress(ins, 1, virtualStack, file),
                                    getVarAddress(ins, 2, virtualStack, file))
                    );
                }
                case MUL -> {
                    file.add_instructions(
                            new Mul(getVarAddress(ins, 0, virtualStack, file),
                                    getVarAddress(ins, 1, virtualStack, file),
                                    getVarAddress(ins, 2, virtualStack, file))
                    );
                }
                case ASSIGN -> {

                    Optional<Integer> arg1 = getVarOnlyAddress(ins, 0, virtualStack, file);
                    if (arg1.isEmpty()) {
                        System.out.println();
                        throw new RuntimeException();
                    }

                    Optional<Integer> arg2 = getVarOnlyAddress(ins, 1, virtualStack, file);
                    arg2.ifPresent(arg -> file.add_instructions(
                            new Mov(arg1.get(),
                                    arg
                            )
                    ));

                    if (arg2.isEmpty()) {
                        file.add_instructions(
                                new Mov(getVarAddress(ins, 0, virtualStack, file),
                                        ins.get(1).flatMap(Either::getLeft).get()
                                )
                        );
                    }
                }
                case CALL -> {
                    Optional<Integer> returnArgsAddress = getVarOnlyAddress(ins, 0, virtualStack, file);

                    // gather address of args
                    int i = 1;
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
            }
        }

        // TODO: add a check to return if it's needed
        file.add_instructions(new Return(0));
    }

    private static int getVarAddress(Instruction ins, int index, ArrayList<String> virtualStack, BytecodeFile file) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getRight().isPresent()) {
                return -virtualStack.indexOf(arg.getRight().get());
            } else if (arg.getLeft().isPresent()) {
                return -orCreateStack(arg.getLeft().get(), virtualStack, file);
            } else {
                System.out.println("all variants of either are not valid");
                throw new RuntimeException();
            }
        } else {
            System.out.printf("missing arg %d for %s\n", index, ins.type().toString());
            throw new RuntimeException();
        }
    }

    private static Optional<Integer> getVarOnlyAddress(Instruction ins, int index, ArrayList<String> virtualStack, BytecodeFile file) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getRight().isPresent()) {
                return Optional.of(-virtualStack.indexOf(arg.getRight().get()));
            } else if (arg.getLeft().isPresent()) {
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
