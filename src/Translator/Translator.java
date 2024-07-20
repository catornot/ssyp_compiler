package Translator;

import Parsing.*;

import java.util.*;
import java.util.stream.IntStream;

public class Translator {
    public static void translate(Program prog, BytecodeFile file) {
        for (Function func : prog.functions()) {
            generateFunction(func, prog.structs(), file);
        }

        file.add_func("print");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("print_array");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("cprint_array");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("len");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));
      
        file.add_func("range");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("assert_eq");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));
    }

    private static void generateFunction(Function func, Struct[] structs, BytecodeFile file) {
        ArrayList<String> virtualStack = new ArrayList<>();
        virtualStack.addAll(Arrays.stream(func.arguments()).map(Variable::name).toList());
        virtualStack.addAll(Arrays.stream(func.locals()).map(Variable::name).toList());

        HashMap<String, String[]> typeMap = new HashMap<>();
        Arrays.stream(func.arguments()).forEach(arg -> typeMap.put(arg.name(), arg.type()));
        Arrays.stream(func.locals()).forEach(arg -> typeMap.put(arg.name(), arg.type()));

        file.add_func(func.name());

        ArrayList<Block> block = new ArrayList<>();
        ArrayList<BytecodeInstruction> instructions = new ArrayList<>();

        for (Instruction ins : func.instructions()) {
            for (int i = 0; ins.get(i).isPresent(); i++) {
                if (ins.get(i).get().getRight().isEmpty()) {
                    continue;
                }

                String lit = STR."#\{ins.get(i).get().getRight().get()}";
                if (!virtualStack.contains(lit)) {
                    virtualStack.add(lit); // literals will have a # before them
                    instructions.add(new Set(-(virtualStack.size() - 1), ins.get(i).get().getRight().get()));
                }
            }
        }

        for (Variable local : func.locals()) {
            if (local.type()[0].equals("Array") && local.type()[1].equals("#")) {
                String lit = STR."#\{local.type()[2]}";
                if (!virtualStack.contains(lit)) {
                    virtualStack.add(lit); // literals will have a # before them
                    instructions.add(new Set(-(virtualStack.size() - 1), Integer.parseInt(local.type()[2])));
                }
            }
        }

        for (Variable local : func.locals()) {
            if (local.type()[0].equals("Array") && local.type()[1].equals("#")) {
                instructions.add(new CreateArray(
                        -virtualStack.indexOf(local.name()),
                        -orCreateStack(Integer.parseInt(local.type()[2]), virtualStack, instructions))
                );

            } else if (local.type()[0].equals("Array")
                    && Arrays.stream(func.arguments()).anyMatch(arg -> arg.type()[0].equals("Array") && arg.name().equals(local.type()[1]))) {

                int lenPos = -virtualStack.indexOf(STR."#\{local.type()[1]}");
                if (lenPos == 1) {
                    virtualStack.add(STR."#\{local.type()[1]}");
                    lenPos = -(virtualStack.size() - 1);

                    long argIndex = Arrays.stream(func.arguments()).takeWhile(arg -> arg.type()[0].equals("Array") && arg.name().equals(local.type()[1])).count() - 1;

                    // get size of args array
                    instructions.add(new Mov(-(int) argIndex, -(virtualStack.size() + 1)));
                    instructions.add(new Call("len", -virtualStack.size()));
                    instructions.add(new Mov(-virtualStack.size(), lenPos));
                }

                instructions.add(new CreateArray(-virtualStack.indexOf(local.name()), lenPos));
            } else if (local.type()[0].equals("Array")) {
                if (Arrays.stream(func.arguments()).noneMatch(variable -> variable.name().equals(local.type()[1]))) {
                    System.out.println(STR."var \{local.type()[1]} doesn't exists in arguements");
                }

                instructions.add(new CreateArray(-virtualStack.indexOf(local.name()), -virtualStack.indexOf(local.type()[1])));
            } else if (Arrays.stream(structs).anyMatch(struct -> struct.name().equals(local.type()[0]))) {
                Struct struct = getStruct(structs, local.type());

                assert struct != null;
                String literalLength = STR."#\{struct.fields().length}";
                if (!virtualStack.contains(literalLength)) {
                    virtualStack.add(literalLength);
                    instructions.add(new Set(-virtualStack.indexOf(literalLength), struct.fields().length));
                }

                instructions.add(new CreateArray(-virtualStack.indexOf(local.name()), -virtualStack.indexOf(literalLength)));
            }
        }

        for (Instruction ins : func.instructions()) {
            generateInstruction(ins, block, instructions, virtualStack, typeMap, structs);
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

    private static void generateInstruction(Instruction ins, ArrayList<Block> blocks, ArrayList<BytecodeInstruction> instructions, ArrayList<String> virtualStack, HashMap<String, String[]> typeMap, Struct[] structs) {
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
                if (getVarType(ins, 0, typeMap).equals("Int")) {
                    Optional<Integer> arg1 = getVarOnlyAddress(ins, 0, virtualStack);
                    if (arg1.isEmpty()) {
                        System.out.println();
                        throw new RuntimeException();
                    }

                    Optional<Integer> arg2 = getVarOnlyAddress(ins, 1, virtualStack);
                    arg2.ifPresent(arg -> instructions.add(
                            new Mov(arg, arg1.get())
                    ));

                    if (arg2.isEmpty()) {
                        instructions.add(
                                new Set(getVarAddress(ins, 0, virtualStack, instructions),
                                        ins.get(1).flatMap(Either::getRight).get()
                                )
                        );
                    }
                } else {
                    System.out.println("cannot assign arrays");
                    throw new RuntimeException();
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

                blocks.add(new Block(instructions.size() - 1, true));
            }

            case ELIF -> {
                instructions.add(new Jump(CompareTypes.NoCmp, 0, 0, 0));
                Block.LastIf(blocks).addJmpInfo(instructions.size() - 1);

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

                Block.LastIf(blocks).mid.add(instructions.size() - 1);
            }

            case ELSE -> {
                instructions.add(new Jump(CompareTypes.NoCmp, 0, 0, 0));
                Block.LastIf(blocks).addJmpInfo(instructions.size() - 1);
                Block.LastIf(blocks).elseStart = Optional.of(instructions.size()); // TODO: check for correctness #2
            }

            case ENDIF -> {
                if (Block.LastIf(blocks).startJump == Integer.MAX_VALUE) {
                    instructions.add(new Jump(CompareTypes.NoCmp, 0, 0, 0));
                    Block.LastIf(blocks).addJmpInfo(instructions.size() - 1);
                }

                Block.LastIf(blocks).end = instructions.size(); // TODO: check for correctness

                Block.LastIf(blocks).fixIfJumps(instructions);
                blocks.removeLast();
            }

            case ARRAY_IN -> {
                if (!getVarType(ins, 0, typeMap).equals("Array")) {
                    System.out.println("Ошибка: У ARRAY_IN первый аргумент должен быть массивом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 1, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_IN второй аргумент должен быть числом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 2, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_IN третий аргумент должен быть числом.");
                    throw new RuntimeException();
                } else {
                    instructions.add(new ArrayIn(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions)
                    ));
                }
            }

            case ARRAY_OUT -> {
                if (!getVarType(ins, 0, typeMap).equals("Array")) {
                    System.out.println("Ошибка: У ARRAY_OUT первый аргумент должен быть массивом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 1, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_OUT второй аргумент должен быть числом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 2, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_OUT третий аргумент долже быть числом.");
                    throw new RuntimeException();
                } else {
                    instructions.add(new ArrayOut(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions)
                    ));
                }
            }

            case WHILE_BEGIN -> {
                ins.get(0).flatMap(Either::getLeft).ifPresentOrElse(cmpType -> instructions.add(
                        new Jump(CompareTypes.fromSymbol(cmpType).invert(),
                                getVarAddress(ins, 1, virtualStack, instructions),
                                getVarAddress(ins, 2, virtualStack, instructions),
                                Integer.MAX_VALUE
                        )
                ), () -> {
                    System.out.println("While should have a cmp operand");
                    throw new RuntimeException();
                });

                blocks.add(new Block(instructions.size() - 1, false));
            }

            case WHILE_END, FOR_END -> {
                blocks.getLast().end = instructions.size();
                blocks.getLast().fixWhileJumps(instructions);
                instructions.add(
                        new Jump(CompareTypes.NoCmp,
                                0,
                                0,
                                Block.LastWhile(blocks).start
                        )
                );
                blocks.removeLast();
            }

            case BREAK -> {
                instructions.add(
                        new Jump(CompareTypes.NoCmp,
                                0,
                                0,
                                Integer.MAX_VALUE
                        )
                );

                assert Block.LastWhile(blocks) != null;
                Block.LastWhile(blocks)
                        .breaks.add(instructions.size() - 1);
            }

            case CONTINUE -> {
                instructions.add(
                        new Jump(CompareTypes.NoCmp,
                                0,
                                0,
                                Integer.MAX_VALUE
                        )
                );

                assert Block.LastWhile(blocks) != null;
                Block.LastWhile(blocks).continues.add(instructions.size() - 1);
            }

            case FOR -> {
                // Для Optional
                if (ins.get(0).isPresent()) {
                    if (ins.get(0).get().getLeft().isEmpty()) {
                        throw new RuntimeException();
                    }
                } else {
                    throw new RuntimeException();
                }
                if (ins.get(1).isPresent()) {
                    if (ins.get(1).get().getLeft().isPresent()) {
                        if (!ins.get(1).get().getLeft().get().equals("IN")) {
                            System.out.println("Отсутствует IN в FOR.");
                            throw new RuntimeException();
                        }
                    } else {
                        throw new RuntimeException();
                    }
                } else {
                    throw new RuntimeException();
                }
                if (!(ins.get(2).isPresent() || ins.get(2).get().getLeft().isPresent())) {
                    throw new RuntimeException();
                }
                // SETЫ
                String var_name = STR."##\{virtualStack.size()}";
                if (!virtualStack.contains(var_name)) {
                    virtualStack.add(var_name);
                    instructions.add(new Set(
                                    -virtualStack.indexOf(var_name),
                                    0
                            )
                    );
                }
                if (!virtualStack.contains("##ONE")) {
                    virtualStack.add("##ONE");
                }
                instructions.add(new Set(
                        -virtualStack.indexOf("##ONE"),
                        1
                ));
                String array_length_name = STR."##\{ins.get(2).get().getLeft().get()}_LENGTH";
                if (!virtualStack.contains(array_length_name)) {
                    virtualStack.add(array_length_name);
                }

                instructions.add(new Mov(-virtualStack.indexOf(ins.get(2).get().getLeft().get()), -(virtualStack.size() + 1)));
                instructions.add(new Call("len", -virtualStack.size()));
                instructions.add(new Mov(-virtualStack.size(), -virtualStack.indexOf(array_length_name)));

                Collections.addAll(virtualStack, "#", "#");
                // WHILE + ADD + ARRAY_OUT
                instructions.add(
                        new Jump(CompareTypes.GreaterEqual,
                                -virtualStack.indexOf(var_name),
                                -virtualStack.indexOf(array_length_name),
                                Integer.MAX_VALUE
                        )
                );
                blocks.add(new Block(instructions.size() - 1, false));
                instructions.add(
                        new ArrayOut(-virtualStack.indexOf(ins.get(2).get().getLeft().get()),
                                -virtualStack.indexOf(var_name),
                                -virtualStack.indexOf(ins.get(0).get().getLeft().get())
                        )
                );
                instructions.add(
                        new Add(-virtualStack.indexOf(var_name),
                                -virtualStack.indexOf(var_name),
                                -virtualStack.indexOf("##ONE")
                        )
                );
            }

            case STRUCT_ACCESS -> {
                Struct struct = getStruct(structs, new String[]{getVarType(ins, 0, typeMap)});
                if (struct == null) {
                    System.out.println("cannot use non struct for STRUCT_ACCESS");
                    throw new RuntimeException();
                }

                int index = -1;
                if (ins.get(1).isPresent()
                        && ins.get(1).get().getLeft().isPresent()
                        && Arrays.stream(struct.fields()).anyMatch(variable -> variable.name().equals(ins.get(1).flatMap(Either::getLeft).get()))) {
                    String field = ins.get(1).flatMap(Either::getLeft).get();
                    for (int i = 0; i < struct.fields().length; i++) {
                        if (field.equals(struct.fields()[i].name())) {
                            index = i;
                        }
                    }
                    assert index != -1;
                } else {
                    System.out.println("arg 1 has to be a struct field");
                    throw new RuntimeException();
                }

                String literalIndex = STR."#\{index}";
                if (!virtualStack.contains(literalIndex)) {
                    virtualStack.add(literalIndex);
                }
                instructions.add(new Set(-virtualStack.indexOf(literalIndex), index));

                // TODO: type checking

                if (getVarOnlyAddress(ins, 0, virtualStack).isPresent() && getVarOnlyAddress(ins, 2, virtualStack).isPresent()) {
                    instructions.add(new ArrayOut(getVarOnlyAddress(ins, 0, virtualStack).get(), -virtualStack.indexOf(literalIndex), getVarOnlyAddress(ins, 2, virtualStack).get()));
                } else {
                    System.out.println("arg 2 has to be a valid var");
                    throw new RuntimeException();
                }
            }

            case STRUCT_ASSIGN -> {
                Struct struct = getStruct(structs, new String[]{getVarType(ins, 0, typeMap)});
                if (struct == null) {
                    System.out.println("cannot use non struct for STRUCT_ACCESS");
                    throw new RuntimeException();
                }

                int index = -1;
                if (ins.get(1).isPresent()
                        && ins.get(1).get().getLeft().isPresent()
                        && Arrays.stream(struct.fields()).anyMatch(variable -> variable.name().equals(ins.get(1).flatMap(Either::getLeft).get()))) {
                    String field = ins.get(1).flatMap(Either::getLeft).get();
                    for (int i = 0; i < struct.fields().length; i++) {
                        if (field.equals(struct.fields()[i].name())) {
                            index = i;
                        }
                    }
                    assert index != -1;
                } else {
                    System.out.println("arg 1 has to be a struct field");
                    throw new RuntimeException();
                }

                String literalIndex = STR."#\{index}";
                if (!virtualStack.contains(literalIndex)) {
                    virtualStack.add(literalIndex);
                }
                instructions.add(new Set(-virtualStack.indexOf(literalIndex), index));


                if (getVarOnlyAddress(ins, 0, virtualStack).isPresent()) {
                    instructions.add(new ArrayIn(getVarOnlyAddress(ins, 0, virtualStack).get(), -virtualStack.indexOf(literalIndex), getVarAddress(ins, 2, virtualStack, instructions)));
                } else {
                    assert false;
                }
            }
        }
    }

    private static Struct getStruct(Struct[] structs, String[] type) {
        int structIndex = -1;
        for (int i = 0; i < structs.length; i++) {
            if (type[0].equals(structs[i].name())) {
                structIndex = i;
            }
        }
        return structIndex != -1 ? structs[structIndex] : null;
    }


    private static String getVarType(Instruction ins, int index, HashMap<String, String[]> typeMap) {
        Optional<Either<String, Integer>> var = ins.get(index);
        if (var.isPresent() && var.get().getLeft().isPresent()) {
            if (typeMap.get(var.get().getLeft().get()) == null) {
                System.out.println(STR."potentially invalid var \{var.get().getLeft().get()}");
                return "Int";
            }

            return typeMap.get(var.get().getLeft().get())[0];
        } else {
            return "Int";
        }
    }

    private static int getVarAddress(Instruction ins, int index, ArrayList<String> virtualStack, ArrayList<BytecodeInstruction> instructions) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                int adr = virtualStack.indexOf(arg.getLeft().get());
                if (adr == -1) {
                    System.out.printf("var %s is not found on stack\n", arg.getLeft().get());
                    throw new RuntimeException();
                }
                return -adr;
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
        int pos = virtualStack.indexOf(STR."#\{literal}");

        assert pos != -1;
        // stack literals are init at the start of the function

        return pos;
    }
}

class Block {
    boolean IsIfBlock;
    int start;
    int startJump;
    ArrayList<Integer> mid;
    ArrayList<Integer> midJump;
    Optional<Integer> elseStart;
    int end;
    ArrayList<Integer> breaks;
    ArrayList<Integer> continues;


    Block(int start, boolean IsIfBlock) {
        this.IsIfBlock = IsIfBlock;
        this.start = start;
        this.startJump = Integer.MAX_VALUE;
        this.mid = new ArrayList<>();
        this.midJump = new ArrayList<>();
        this.end = Integer.MAX_VALUE;
        this.elseStart = Optional.empty();
        this.breaks = new ArrayList<>();
        this.continues = new ArrayList<>();
    }

    static Block LastWhile(ArrayList<Block> blocks) {
        for (int i = blocks.size() - 1; i > -1; i--) {
            if (!blocks.get(i).IsIfBlock)
                return blocks.get(i);
        }

        assert false;
        return blocks.getFirst();
    }

    static Block LastIf(ArrayList<Block> blocks) {
        for (int i = blocks.size() - 1; i > -1; i--) {
            if (blocks.get(i).IsIfBlock)
                return blocks.get(i);
        }

        assert false;
        return blocks.getFirst();
    }

    public void fixIfJumps(ArrayList<BytecodeInstruction> instructions) {
        assert this.end != Integer.MAX_VALUE;
        assert this.IsIfBlock;

        if (instructions.get(this.start) instanceof Jump) {
            ((Jump) instructions.get(this.start)).setJumpDestination(this.mid.isEmpty() ? this.elseStart.orElseGet(() -> this.end) : this.mid.getFirst());
        } else {
            System.out.println("invalid if jmp instruction at index");
            throw new RuntimeException();
        }

        if (instructions.get(this.startJump) instanceof Jump) {
            ((Jump) instructions.get(this.startJump)).setJumpDestination(this.end);
        } else {
            System.out.println("invalid if jmp jmp instruction at index");
            throw new RuntimeException();
        }

        for (int i : this.mid) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.mid.size() < i + 1 ? this.elseStart.orElseGet(() -> this.end) : this.mid.get(i + 1));
            } else {
                System.out.println("invalid else if jmp instruction at index");
                throw new RuntimeException();
            }
        }

        for (int i : this.midJump) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.end);
            } else {
                System.out.println("invalid else if jmp jmp instruction at index");
                throw new RuntimeException();
            }
        }
    }

    public void fixWhileJumps(ArrayList<BytecodeInstruction> instructions) {
        assert this.end != Integer.MAX_VALUE;
        assert !this.IsIfBlock;

        if (instructions.get(this.start) instanceof Jump) {
            ((Jump) instructions.get(this.start)).setJumpDestination(this.end + 1);
        } else {
            System.out.println("invalid while jmp instruction at index");
            throw new RuntimeException();
        }

        for (int i : this.breaks) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.end + 1);
            } else {
                System.out.println("invalid break jmp instruction at index");
                throw new RuntimeException();
            }
        }

        for (int i : this.continues) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.end);
            } else {
                System.out.println("invalid continue jmp instruction at index");
                throw new RuntimeException();
            }
        }
    }

    void addJmpInfo(int instructionIndex) {
        if (this.midJump.isEmpty()) {
            this.startJump = instructionIndex;
        } else {
            midJump.add(instructionIndex);
        }
    }
}
