package Parsing;

public record Struct(String name, Variable[] fields) {
    @Override
    public String toString() {
        String[] fields = new String[this.fields.length];
        for (int k = 0; k < this.fields.length; k++) {
            fields[k] = this.fields[k].toString();
        }
        return String.format("""
                        Struct %s:
                            VARS: %s
                        """, name,
                String.join(", ", fields)
        );
    }
}
