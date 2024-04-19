import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

class Calculator {

    private static final String ARITHMETIC_OPERATORS = "+-*/%";
    private static final String LOGICAL_OPERATORS = "&|!";
    public static float evaluateArithmeticExpression(List<Token> expression, Token.Type floatType) {
        LinkedList<Token> postfix = infixToPostfix(expression, ARITHMETIC_OPERATORS);
        Stack<Float> stack = new Stack<>();

        for (Token token : postfix) {
            if (token.getType() == Token.Type.FLOAT || token.getType() == Token.Type.NUMBER) {
                if (token.getType() == Token.Type.NUMBER) {
                    stack.push(Float.parseFloat(token.getValue())); // Convert NUMBER to FLOAT
                } else {
                    stack.push(Float.parseFloat(token.getValue()));
                }
            } else if (ARITHMETIC_OPERATORS.contains(token.getValue())) {
                float operand2 = stack.pop();
                float operand1 = stack.pop();
                switch (token.getValue()) {
                    case "+":
                        stack.push(operand1 + operand2);
                        break;
                    case "-":
                        stack.push(operand1 - operand2);
                        break;
                    case "*":
                        stack.push(operand1 * operand2);
                        break;
                    case "/":
                        stack.push(operand1 / operand2);
                        break;
                    case "%":
                        stack.push(operand1 % operand2);
                        break;
                }
            }
        }
        return stack.pop();
    }

    public static int evaluateArithmeticExpression(List<Token> expression) {
        LinkedList<Token> postfix = infixToPostfix(expression, ARITHMETIC_OPERATORS);
        Stack<Integer> stack = new Stack<>();

        for (Token token : postfix) {
            if (token.getType() == Token.Type.NUMBER) {
                stack.push(Integer.parseInt(token.getValue()));
            } else if (ARITHMETIC_OPERATORS.contains(token.getValue())) {
                int operand2 = stack.pop();
                int operand1 = stack.pop();
                switch (token.getValue()) {
                    case "+":
                        stack.push(operand1 + operand2);
                        break;
                    case "-":
                        stack.push(operand1 - operand2);
                        break;
                    case "*":
                        stack.push(operand1 * operand2);
                        break;
                    case "/":
                        stack.push(operand1 / operand2);
                        break;
                    case "%":
                        stack.push(operand1 % operand2);
                        break;
                }
            }
        }

        return stack.pop();
    }

    public static boolean evaluateLogicalExpression(List<Token> expression) {
        LinkedList<Token> postfix = infixToPostfix(expression, LOGICAL_OPERATORS);
        Stack<Object> stack = new Stack<>();

        for (Token token : postfix) {
            if (token.getType() == Token.Type.BOOL) {
                stack.push(Boolean.parseBoolean(token.getValue()));
            } else if (token.getType() == Token.Type.NUMBER) {
                stack.push(Integer.parseInt(token.getValue()));
            } else if (token.getType() == Token.Type.FLOAT) {
                stack.push(Float.parseFloat(token.getValue()));
            } else if (ARITHMETIC_OPERATORS.contains(token.getValue())) {
                // Evaluate arithmetic sub-expression
                LinkedList<Token> subExpr = new LinkedList<>();
                int count = 0;
                int index = postfix.indexOf(token);
                while (count < 2) {
                    Token t = postfix.get(index - 1);
                    subExpr.addFirst(t);
                    postfix.remove(index - 1);
                    index--;
                    count++;
                }
                int result = evaluateArithmeticExpression(subExpr);
                stack.push(result);
            } else if (LOGICAL_OPERATORS.contains(token.getValue())) {
                if (token.getValue().equals("!")) {
                    boolean operand = (boolean) stack.pop();
                    stack.push(!operand);
                } else {
                    boolean operand2 = (boolean) stack.pop();
                    boolean operand1 = (boolean) stack.pop();
                    switch (token.getValue()) {
                        case "&":
                            stack.push(operand1 && operand2);
                            break;
                        case "|":
                            stack.push(operand1 || operand2);
                            break;
                    }
                }
            }
        }

        return (boolean) stack.pop();
    }

    private static LinkedList<Token> infixToPostfix(List<Token> expression, String operators) {
        LinkedList<Token> postfix = new LinkedList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : expression) {
            if (token.getType() == Token.Type.NUMBER || token.getType() == Token.Type.BOOL || token.getType() == Token.Type.FLOAT) {
                postfix.add(token);
            } else if (operators.contains(token.getValue())) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek().getValue()) >= precedence(token.getValue())) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.getType() == Token.Type.DELIMITER) {
                if (token.getValue().equals("(")) {
                    operatorStack.push(token);
                } else if (token.getValue().equals(")")) {
                    while (!operatorStack.isEmpty() && !operatorStack.peek().getValue().equals("(")) {
                        postfix.add(operatorStack.pop());
                    }
                    operatorStack.pop(); // Pop "("
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            postfix.add(operatorStack.pop());
        }

        return postfix;
    }

    private static int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            case "&":
                return 3;
            case "|":
                return 4;
            case "!":
                return 5;
            default:
                return 0;
        }
    }
}
