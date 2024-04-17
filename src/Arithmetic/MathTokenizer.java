package Arithmetic;

import java.util.ArrayList;
import java.util.HashMap;

public class MathTokenizer {
    private static final HashMap<Character, MathsToken.MatTokenType> operationChars = new HashMap<>();

    static {
        operationChars.put('+', MathsToken.MatTokenType.PLUS);
        operationChars.put('-', MathsToken.MatTokenType.MINUS);
        operationChars.put('*', MathsToken.MatTokenType.MULTIPLY);
        operationChars.put('/', MathsToken.MatTokenType.DIVIDE);
        operationChars.put('%', MathsToken.MatTokenType.MODULO);
        operationChars.put('(', MathsToken.MatTokenType.PAREN_OPEN);
        operationChars.put(')', MathsToken.MatTokenType.PAREN_CLOSE);
    }
  
    /**
     * Generates a list containing all the valid mathematical tokens in the input string.
     *
     * @throws Exception if an invalid number of unexpected character is found
     */
    public static ArrayList<MathsToken> generateTokens(String input) throws Exception {
        ArrayList<MathsToken> tokenList = new ArrayList<>();
        boolean containsDecimalPoint = false;
  
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // if character is a digit, create and add new token
            if (Character.isDigit(c) || c == '.') {
                int j;
                // iterate until the end of the number
                for (j = i; j < input.length(); j++) {
                    char curr = input.charAt(j);
                    // cannot contain 2 decimal points in a number
                    if (curr == '.') {
                        if (containsDecimalPoint) throw new Exception();
                        else containsDecimalPoint = true;
                    }
                    // end of number token reached
                    else if (!Character.isDigit(curr)) break;
                }
                // create a new number token and add to list
                String substring = "0" + input.substring(i, j); //from start to current character
                MathsToken token = new MathsToken(MathsToken.MatTokenType.NUMBER, Double.parseDouble(substring));
                tokenList.add(token);
                containsDecimalPoint = false; //update decimal tracker as end of number token reached
                // update the outer loop and start iterating from the end of the number token
                i = j - 1;
            }
            // if it is an operation, add to operation token
            else if (operationChars.containsKey(c)) {
                MathsToken token = new MathsToken(operationChars.get(c));
                tokenList.add(token);
            }
            // else if it isn't a valid character, throw exception
            else if (!Character.isWhitespace(c)) throw new Exception();
        }
        return tokenList;
    }
}
