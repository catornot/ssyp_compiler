package Translator;

public enum CompareTypes {
    NoCmp,
    Equal,
    NoEqual,
    Greater,
    GreaterEqual,
    Lower,
    LowerEqual;

    static CompareTypes fromSymbol(String symbol) {
        switch (symbol) {
            case "=":
            case "==":
                return Equal;
            case "!=":
                return NoEqual;
            case ">":
                return Greater;
            case ">=":
                return GreaterEqual;
            case "<":
                return Lower;
            case "<=":
                return LowerEqual;
            default:
                assert false;
        }
        return NoCmp;
    }

    public CompareTypes invert() {
        switch (this) {
            case Lower -> {
                return GreaterEqual;
            }
            case LowerEqual -> {
                return Greater;
            }
            case Greater -> {
                return LowerEqual;
            }
            case GreaterEqual -> {
                return Lower;
            }
            case Equal -> {
                return NoEqual;
            }
            case NoEqual -> {
                return Equal;
            }
            case NoCmp -> {
                return NoCmp;
            }
        }

        assert false;
        return NoCmp;
    }
}
