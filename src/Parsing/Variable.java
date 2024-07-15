package Parsing;

public record Variable(String name, String type) {

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
