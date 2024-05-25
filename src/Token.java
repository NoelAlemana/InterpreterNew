import java.util.InputMismatchException;

class Token {
    enum Type {
        IDENTIFIER,
        NEWLINE,
        ASSIGNMENT,
        CONCAT,
        STRING,
        CHAR,
        KEYWORD,
        NUMBER,
        FLOAT,
        BOOL,
        OPERATOR,
        DELIMITER,
        INVALID,
        EOF // End of file
    }

    private Type type;
    private String value;
    private int line;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public void setType(Type type) {
        this.type = type;
    }
    public void setValue(String value) {
        // Set the value based on its type
        switch (type) {
            case CHAR:
                if (value.length() == 1) {
                    this.value = value;
                } else {
                    throw new IllegalArgumentException("Invalid CHAR value: " + value);
                }
                break;
            case FLOAT:
                try {
                    Float.parseFloat(value);
                    this.value = value;
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid FLOAT value: " + value);
                }
                break;
            case BOOL:
                if (value.equals("TRUE") || value.equals("FALSE")) {
                    this.value = value;
                } else {
                    throw new IllegalArgumentException("Invalid BOOL value: " + value);
                }
                break;
            case NUMBER:
                try {
                    Integer.parseInt(value);
                    this.value = value;
                } catch (NumberFormatException e) {
                    throw new InputMismatchException(value + " cannot be converted into INT");
                }
                break;
            default:
                throw new UnsupportedOperationException("Data type not supported for token type: " + type);
        }
    }


    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
    public Object getDataType() {
        switch (type) {
            case CHAR:
                if (value.length() == 1) {
                    return value.charAt(0);
                } else {
                    throw new IllegalArgumentException("Invalid CHAR token value: " + value);
                }
            case FLOAT:
                return Float.parseFloat(value);
            case BOOL:
                if (value != null) {
                    return value.toUpperCase();
                } else {
                    return null;
                }
            case NUMBER:
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    // If it's not an integer, try parsing as a float
                    return Float.parseFloat(value);
                }
            default:
                throw new UnsupportedOperationException(toString());
        }
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "(" + type + ": " + value + ") at line: " + line;
    }
}
