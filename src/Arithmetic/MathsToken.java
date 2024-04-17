package Arithmetic;

public class MathsToken {
    
    public enum MatTokenType {
        NUMBER,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        MODULO,
        PAREN_OPEN,
        PAREN_CLOSE
    }

    public final MatTokenType type;
    public final double val;
   
    /**
     * Create a new token with double value, {@code val}
     */
    public MathsToken(MatTokenType type, double val) {
        this.type = type;
        this.val = val;
    }
   
    /**
     * Create a new token with no double value
     */
    public MathsToken(MatTokenType type) {
        this.type = type;
        this.val = Double.NaN;
    }
   
    @Override
    public String toString() {
        return String.format("Token{ type: %s, value: %s }", this.type, this.val);
    }

}
