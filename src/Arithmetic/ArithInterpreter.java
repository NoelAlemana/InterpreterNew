package Arithmetic;

public class ArithInterpreter {
      /**
   * Calls the Tokenizer and Parser, and returns the evaluated result of the parsed token tree
   */
  public static double getResult(String inputString) throws Exception {
    ArithTokenNode expressionTree = ArithParser.parseTokens(MathTokenizer.generateTokens(inputString));
    return evaluateExpressionTree(expressionTree);
}

public static boolean getLogicalResult(String inputString) throws Exception {
    ArithTokenNode expressionTree = ArithParser.parseTokens(MathTokenizer.generateTokens(inputString));
    return evaluateLogicalExpressionTree(expressionTree);
}

/**
 * Evaluates the result of the mathematical expression by recursively going through all the TokenNodes in the Parsed
 * Token Tree and evaluate the results at each node
 *
 * @return the result of the mathematical expression
 */
private static double evaluateExpressionTree(ArithTokenNode node) {
    switch (node.type) {
        case NUMBER: {
            return node.isNegative() ? -node.nodeValue : node.nodeValue;
        }
        case MULTIPLY: {
            return evaluateExpressionTree(node.operand1) * evaluateExpressionTree(node.operand2);
        }
        case DIVIDE: {
            return evaluateExpressionTree(node.operand1) / evaluateExpressionTree(node.operand2);
        }
        case PLUS: {
            return evaluateExpressionTree(node.operand1) + evaluateExpressionTree(node.operand2);
        }
        case MINUS: {
            return evaluateExpressionTree(node.operand1) - evaluateExpressionTree(node.operand2);
        }
        case MODULO: {
            return evaluateExpressionTree(node.operand1) % evaluateExpressionTree(node.operand2);
        }
        default: {
            // if somehow an invalid token gets processed by the Parser/Tokenizer
            throw new InternalError("Unknown Error Encountered");
        }
    }
}

private static boolean evaluateLogicalExpressionTree(ArithTokenNode node){
    switch (node.type){
        case EQUAL: {
            return evaluateLogicalExpressionTree(node.operand1) == evaluateLogicalExpressionTree(node.operand2);       
        }
        default: {
            throw new InternalError("Wrong Method");
        } 
    }
}

}
