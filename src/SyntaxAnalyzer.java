import java.util.*;

class SyntaxAnalyzer {
    private List<Token> tokens;
    private int currentTokenIndex;
    private boolean begin_code;
    private Map<String, Token> variables;
    Scanner scanner;
    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.begin_code = false;
        this.variables = new HashMap<>();
        this.scanner = new Scanner(System.in);
;    }
    private Token peek() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex + 1);
        } else {
            return null; // No more tokens left
        }
    }
    private Token currToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        } else {
            return null; // No more tokens left
        }
    }
    public void parse() {
        while (currentTokenIndex < tokens.size()) {
            statement();
        }
        if (currentTokenIndex < tokens.size()) {
            error("Unexpected tokens after end of program " + currentTokenIndex +":"+tokens.size());
        }
    }

    private void statement() {
        if(!begin_code && !tokens.get(currentTokenIndex).getValue().equals("begin") && !tokens.get(currentTokenIndex).getValue().equals("code")) {
            consume();
            return;
        }
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == Token.Type.IDENTIFIER) {
                assignmentStatement();
            } else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("begin")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("code")){
                    consume();
                    System.out.println("Code is now Running");
                    begin_code = true;
                    if(currToken().getType() == Token.Type.NEWLINE) consume();
                    else error("Expected a NEWLINE");
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("if")){ // BEGIN IF

                }
            } else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("end")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("code")){
                    consume();
                    System.out.println("\nFinished Coding");
                    if(currToken().getType() == Token.Type.NEWLINE) consume();
                    else error("Expected a NEWLINE");
                    if(currToken().getType() == Token.Type.EOF) consume();
                    else error("Expected a EOF");
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("if")){ // BEGIN IF

                }
            } else if (currentToken.getType() == Token.Type.KEYWORD && (currentToken.getValue().equals("int") || currentToken.getValue().equals("char") || currentToken.getValue().equals("bool") || currentToken.getValue().equals("float"))) {
                declareStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("display")) {
                displayStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("scan")) {
                scanStatement();
            }// TODO: make all the different handlers
            else {
                error("Invalid statement:" + currentToken);
            }
        }
    }
    private void scanStatement() {
//        System.out.println("Display");
        match(Token.Type.KEYWORD,"scan");
        match(Token.Type.DELIMITER,":");
        while(currToken().getType() == Token.Type.IDENTIFIER || currToken().getType() == Token.Type.DELIMITER){
            if(currToken().getType() == Token.Type.IDENTIFIER){
                try{
                    System.out.println(currToken().getValue());
                    if(variables.containsKey(currToken().getValue())){
                        String newValue = scanner.nextLine();
                        variables.get(currToken().getValue()).setValue(newValue);
                    }else throw new RuntimeException("Variable: "+ currToken().getValue() + " is not yet declared");
                }catch (NullPointerException e){
                }
                consume();
            }else if(currToken().getType() == Token.Type.DELIMITER){
                consume();
            }else error("Unexpected Token in Scan:" + currToken());
        }
        match(Token.Type.NEWLINE);
    }
    private void displayStatement() {
//        System.out.println("Display");
        consume();
        consume();
        while(currToken().getType() == Token.Type.IDENTIFIER || currToken().getType() == Token.Type.STRING|| currToken().getType() == Token.Type.CONCAT){
            if(currToken().getType() == Token.Type.IDENTIFIER){
                try{
                    System.out.print(variables.get(currToken().getValue()).getDataType());
                }catch (NullPointerException e){
                    if(variables.containsKey(currToken().getValue()))
                        System.out.print("null");
                    else throw new RuntimeException("Varible: "+ currToken().getValue() + " is not yet declared");
                }
                consume();
            }else if(currToken().getType() == Token.Type.CONCAT){
                consume();
            }else if(currToken().getType() == Token.Type.STRING){
                if(currToken().getValue().equals("$")) System.out.println("");
                else System.out.print(currToken().getValue());
                consume();
            }
        }
        match(Token.Type.NEWLINE);
    }
    private void declareStatement() {
        String datatype = currToken().getValue();
        Token.Type expectedDataType = null;
        switch (datatype){
            case "int":
                expectedDataType = Token.Type.NUMBER;
                break;
            case "float":
                expectedDataType = Token.Type.FLOAT;
                break;
            case "bool":
                expectedDataType = Token.Type.BOOL;
                break;
            case "char":
                expectedDataType = Token.Type.CHAR;
                break;
        }
//        System.out.println("expected data type: " + datatype);
        consume(); //Consume INT,FLOAT,CHAR,BOOL token
        while(currToken().getType() != Token.Type.NEWLINE){
            if(currToken().getValue().equals(","))match(Token.Type.DELIMITER);
            String varname = currToken().getValue();
//            System.out.println(varname+ "Varname");
            match(Token.Type.IDENTIFIER);
            if(currToken().getValue() == "="){
                consume(); // Consume ASSIGNMENT token
                if(variables.containsKey(varname))throw new IllegalArgumentException("Variable name: " + varname + " is already declared");
                if(expectedDataType == Token.Type.FLOAT && currToken().getType() == Token.Type.NUMBER) currToken().setType(Token.Type.FLOAT);
                if(currToken().getType() != expectedDataType) throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken().getType());
                variables.put(varname,currToken());
//                System.out.println("Declared "+varname+": " +initializedVariables.get(varname));
                consume();
            }else if(currToken().getValue().equals(",")){
                consume(); // Consume DELIMITER token
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
//                System.out.println(currToken()+"ASdasdasdsdad");
            }else if(currToken().getType() == Token.Type.NEWLINE){
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
//                System.out.println(currToken()+"ASdasdasdsdad");
            }else error("Unexpected token type: " + currToken());
        }
        consume(); // Consume NEWLINE token
    }
    private void assignmentStatement() {
        ArrayList<Token> identifiers = new ArrayList<>();
        identifiers.add(currToken());
        match(Token.Type.IDENTIFIER);
        match(Token.Type.ASSIGNMENT, "=");

        List<Token> tokens = new LinkedList<>();

        while (currToken().getType() != Token.Type.NEWLINE) {
            if (currToken().getType() == Token.Type.ASSIGNMENT) {
                consume(); // Consume the ASSIGNMENT token
            }else if (currToken().getType() == Token.Type.IDENTIFIER) {
                identifiers.add(currToken());
                consume(); // Consume the ASSIGNMENT token
            }else if (currToken().getType() == Token.Type.NUMBER || currToken().getType() == Token.Type.FLOAT || currToken().getType() == Token.Type.DELIMITER || currToken().getType() == Token.Type.BOOL){
                while(currToken().getType() != Token.Type.NEWLINE){
                    if(currToken().getType() == Token.Type.IDENTIFIER){
                        System.out.println(variables.get(currToken().getValue()));
                        tokens.add(variables.get(currToken().getValue()));
                        consume();
                    }else{
                        //TODO: Add new Token(DELIMITER,()) when peek.getType() == OPERATOR.
                        tokens.add(currToken());
                        consume();
                    }
                }
            }
        }
        for(Token token: tokens) System.out.println(token+"TOKEN EXPRESSION");
        for(Token var: identifiers){
            if(variables.containsKey(var.getValue())){
                if(isLogicalStatement(tokens)){
                    LogicalCalculator logicalCalculator = new LogicalCalculator();
                    variables.get(var.getValue()).setValue(Boolean.toString(logicalCalculator.evaluate(tokens)));
                }else if(containsFloat(tokens))
                    variables.get(var.getValue()).setValue(Double.toString(Calculator.evaluateArithmeticExpression(tokens,Token.Type.FLOAT)));
                else
                    variables.get(var.getValue()).setValue(Integer.toString(Calculator.evaluateArithmeticExpression(tokens)));
            }
            else error("Variable: " + var +" must be declared first");
        }
        match(Token.Type.NEWLINE);

    }

    private void ifStatement() {
        match(Token.Type.KEYWORD, "if");
        expression();
        match(Token.Type.KEYWORD, "then");
        statement();
    }

    private void expression() {
        literal();
    }

    private void literal() {
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            switch (currentToken.getType()) {
                case CHAR:
                case NUMBER:
                case FLOAT:
                case BOOL:
                    consume();
                    break;
                default:
                    error("Invalid literal");
            }
        }
    }

    private void match(Token.Type expectedType) {
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == expectedType) {
                consume();
            } else {
                error("Unexpected token type, expected " + expectedType + " Token: " + currentToken);
            }
        }
    }
    private String matchIdentifier(Token token) {
        if (currentTokenIndex < tokens.size()) {
            consume();
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == Token.Type.IDENTIFIER) {
                consume();
                return currentToken.getValue();
            } else {
                error("Unexpected token type, expected IDENTIFIER" + " Token: " + currentToken);
            }
        }
        return null;
    }

    private void match(Token.Type expectedType, String expectedValue) {
        if (currentTokenIndex < tokens.size()) {
            Token currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType() == expectedType && currentToken.getValue().equals(expectedValue)) {
                consume();
            } else {
                error("Unexpected token type or value, expected " + expectedType + " '" + expectedValue + "'");
            }
        }
    }

    private void consume() {
//        System.out.print("Consumed Token:");
//        System.out.println(tokens.get(currentTokenIndex));
        currentTokenIndex++;
    }

    private void error(String message) {
        throw new RuntimeException("Syntax error: " + message);
    }
    public static boolean containsFloat(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == Token.Type.FLOAT) {
                return true;
            }
        }
        return false;
    }
    public static boolean isLogicalStatement(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getValue().equals("==")||token.getValue().equals("<>")||token.getValue().equals(">=")||token.getValue().equals("<=")||token.getValue().equals(">")||token.getValue().equals("<")||token.getValue().equals("and")||token.getValue().equals("or")||token.getValue().equals("not")) {
                return true;
            }
        }
        return false;
    }
}