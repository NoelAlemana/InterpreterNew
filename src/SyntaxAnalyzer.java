import java.util.*;
import Arithmetic.ArithInterpreter;

class SyntaxAnalyzer {
    private List<Token> tokens;
    private int currentTokenIndex;
    private boolean begin_code;
    private boolean displayed;
    private Map<String, Token> variables;
    Scanner scanner;
    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.begin_code = false;
        this.displayed = false;
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
    public int countNewline(int from, int to) {
        int count =0;
        for(int i = from; i < to+1;i++){
            if(tokens.get(i).getType() == Token.Type.NEWLINE) count++;
        }
        return count;
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
            } else if(currentToken.getType() == Token.Type.NEWLINE){
                consume();
            } else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("begin")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("code")){
                    consume();
//                    System.out.println("Code is now Running");
                    begin_code = true;
                    if(currToken().getType() == Token.Type.NEWLINE) consume();
                    else error("Expected a NEWLINE");
                }
            } else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("end")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("code")){
                    consume();
//                    System.out.println("\nFinished Coding");
                    if(currToken().getType() == Token.Type.NEWLINE) consume();
                    else error("Expected a NEWLINE");
                    if(currToken().getType() == Token.Type.EOF) {
                        consume();
                        if(displayed == false) System.out.println("No Error");
                    }
                    else error("Expected a EOF");
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("if")){ // END IF
                }
                else if(tokens.get(currentTokenIndex).getValue().equals("while")){ // END WHILE
                consume();
                }
            } else if (currentToken.getType() == Token.Type.KEYWORD && (currentToken.getValue().equals("int") || currentToken.getValue().equals("char") || currentToken.getValue().equals("bool") || currentToken.getValue().equals("float"))) {
                declareStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("display")) {
                displayStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("scan")) {
                scanStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("if")){
                ifStatement();
            }else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("while")){
                whileStatement();
            }
            // TODO: make all the different handlers
            else {
                error("Invalid statement:" + currentToken);
            }
        }
    }
    private void displayStatement(){
        this.displayed = true;
        match(Token.Type.KEYWORD,"display");
        match(Token.Type.DELIMITER,":");

        while(currToken().getType() != Token.Type.NEWLINE){
            List<Token> tokens = new LinkedList<>();
            while(currToken().getType() != Token.Type.CONCAT){
                tokens.add(currToken());
                consume();
                if(currToken().getType() == Token.Type.NEWLINE) break;
            }
            if(currToken().getType() != Token.Type.NEWLINE)
                match(Token.Type.CONCAT);
            if(tokens.size() == 1){
                if(tokens.get(0).getType() == Token.Type.IDENTIFIER){
                    try{
                        System.out.print(variables.get(tokens.get(0).getValue()).getDataType());
                    }catch (NullPointerException e){
                        if(variables.containsKey(tokens.get(0).getValue()))
                            System.out.print("null");
                        else throw new RuntimeException("Variable: "+ tokens.get(0) + " is not yet declared");
                    }
                }else if(tokens.get(0).getType() == Token.Type.STRING || tokens.get(0).getType() == Token.Type.CHAR){
                    if(tokens.get(0).getValue().equals("$")) System.out.println("");
                    else System.out.print(tokens.get(0).getValue());
                }
            }else{
                Object result = expression(tokens);
                if(result.toString().equals("true") || result.toString().equals("false"))
                    System.out.print(result.toString().toUpperCase());
                else
                    System.out.print(result);
            }
            tokens.clear();
        }
        match(Token.Type.NEWLINE);
    }
    private void whileStatement(){
        match(Token.Type.KEYWORD,"while");
        List<Token> expressionTokens = new LinkedList<>();
        while(currToken().getType()!= Token.Type.NEWLINE){
            expressionTokens.add(currToken());
            consume();
        }
        match(Token.Type.NEWLINE);
        match(Token.Type.KEYWORD,"begin");
        match(Token.Type.KEYWORD,"while");
        match(Token.Type.NEWLINE);
        int startwhileIndex = currentTokenIndex;
        int nestedCount = 0;
        while(true){
            if(currToken().getValue().equals("begin") && peek().getValue().equals("while"))
                nestedCount++;
            if(currToken().getValue().equals("end") && peek().getValue().equals("while"))
                nestedCount--;
            if(nestedCount == -1){
                break;
            }
            consume();
        }
        int endwhileIndex = currentTokenIndex;
        while (Boolean.parseBoolean(expression(expressionTokens).toString())){
            currentTokenIndex = startwhileIndex;
            for(int i=0;i< countNewline(startwhileIndex,endwhileIndex);i++){
                statement();
            }
        }
        currentTokenIndex = endwhileIndex;
    }
    private void scanStatement() {
        match(Token.Type.KEYWORD,"scan");
        match(Token.Type.DELIMITER,":");
        while(currToken().getType() == Token.Type.IDENTIFIER || currToken().getType() == Token.Type.DELIMITER){
            if(currToken().getType() == Token.Type.IDENTIFIER){
                try{
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
        consume(); //Consume INT,FLOAT,CHAR,BOOL token
        while(currToken().getType() != Token.Type.NEWLINE){
            if(currToken().getValue().equals(","))match(Token.Type.DELIMITER);
            String varname = currToken().getValue();
            match(Token.Type.IDENTIFIER);
            if(currToken().getValue() == "="){
                consume(); // Consume ASSIGNMENT token
                if(variables.containsKey(varname))throw new IllegalArgumentException("Variable name: " + varname + " is already declared");
                if(currToken().getType() != expectedDataType ){
                    if(currToken().getType() == Token.Type.IDENTIFIER){
                        try {
                            Token.Type varDataType = variables.get(currToken().getValue()).getType();
                            switch (varDataType) {
                                case CHAR:
                                    if (expectedDataType != Token.Type.CHAR)
                                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: " + expectedDataType + " Defined datatype: " + varDataType + " " + currToken());
                                    break;
                                case NUMBER:
                                case FLOAT:
                                    if (expectedDataType == Token.Type.CHAR || expectedDataType == Token.Type.BOOL)
                                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: " + expectedDataType + " Defined datatype: " + varDataType + " " + currToken());
                                    break;
                                case BOOL:
                                    if (expectedDataType != Token.Type.BOOL)
                                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: " + expectedDataType + " Defined datatype: " + varDataType + " " + currToken());
                                    break;
                            }
                        }catch (NullPointerException e){
                            error("Error causing token:" +currToken());
                        }
                    }
                    // Checking Unmatched Datatype
                    if((expectedDataType == Token.Type.CHAR || expectedDataType == Token.Type.BOOL) && (currToken().getType() == Token.Type.NUMBER || currToken().getType() == Token.Type.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken());
                    else if((currToken().getType() == Token.Type.CHAR || currToken().getType() == Token.Type.BOOL) && (expectedDataType == Token.Type.NUMBER || expectedDataType == Token.Type.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken());
                    else if ((expectedDataType == Token.Type.BOOL && currToken().getType() == Token.Type.CHAR) || (expectedDataType == Token.Type.CHAR && currToken().getType() == Token.Type.BOOL))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken());

                }
                if(currToken().getType() == Token.Type.CHAR) {
                    variables.put(varname, currToken());
                    consume();
                }else {
                    StringBuilder tokenValuesBuilder = new StringBuilder();
                    List<Token> logicalTokens = new LinkedList<>();
                    while (currToken().getType() != Token.Type.NEWLINE && !currToken().getValue().equals(",")) {
                        logicalTokens.add(currToken());
                        if(currToken().getType() == Token.Type.IDENTIFIER) {
                            tokenValuesBuilder.append(variables.get(currToken().getValue()).getDataType());
                        }
                        else
                            tokenValuesBuilder.append(currToken().getValue());
                        consume();
                    }
//                    System.out.println(tokenValuesBuilder.toString());
                    try {
                        if(expectedDataType == Token.Type.NUMBER) {
                            variables.put(varname, new Token(expectedDataType, Integer.toString((int) ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                        }else if(expectedDataType == Token.Type.FLOAT)
                            variables.put(varname, new Token(expectedDataType,Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                        else if(expectedDataType == Token.Type.BOOL) {
                            Object res = expression(logicalTokens);
                            variables.put(varname, new Token(expectedDataType, res.toString()));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }else if(currToken().getValue().equals(",")){
                consume(); // Consume DELIMITER token
                variables.put(varname,new Token(expectedDataType,null));
            }else if(currToken().getType() == Token.Type.NEWLINE){
                variables.put(varname,new Token(expectedDataType,null));
            }else error("Unexpected token type: " + currToken());
        }
        match(Token.Type.NEWLINE);
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
            }else if (currToken().getType() == Token.Type.IDENTIFIER && peek().getType() != Token.Type.OPERATOR&& peek().getType() != Token.Type.NEWLINE&& peek().getType() != Token.Type.DELIMITER) {
                identifiers.add(currToken());
                consume(); // Consume the IDENTIFIER token
            }else if (currToken().getType() == Token.Type.NUMBER || currToken().getType() == Token.Type.FLOAT || currToken().getType() == Token.Type.DELIMITER || currToken().getType() == Token.Type.BOOL|| currToken().getType() == Token.Type.IDENTIFIER){
                while(currToken().getType() != Token.Type.NEWLINE){
                    if(peek().getValue().equals(">") ||peek().getValue().equals("<") ||peek().getValue().equals("<>") ||peek().getValue().equals("==") ||peek().getValue().equals(">=") ||peek().getValue().equals("<=")||peek().getValue().equals("and")||peek().getValue().equals("or")){
                        tokens.add(new Token(Token.Type.DELIMITER,"("));
                        if(currToken().getType() == Token.Type.IDENTIFIER) {
                            tokens.add(variables.get(currToken().getValue()));
                        }else tokens.add(currToken());
                        consume();
                        tokens.add(currToken());
                        consume();
                        if(currToken().getType() == Token.Type.IDENTIFIER) {
                            tokens.add(variables.get(currToken().getValue()));
                        }else tokens.add(currToken());
                        consume();
                        tokens.add(new Token(Token.Type.DELIMITER,")"));
                    }
                    if(currToken().getType() == Token.Type.IDENTIFIER){
                        tokens.add(variables.get(currToken().getValue()));
                        consume();
                    }else{
                        tokens.add(currToken());
                        consume();
                    }
                }
            }else if (currToken().getType() == Token.Type.CHAR){
                tokens.add(currToken());
                consume();
            }
        }
        for(Token var: identifiers){
            if(variables.containsKey(var.getValue())){
                StringBuilder tokenValuesBuilder = new StringBuilder();
                for (Token token : tokens) {
                    // Append the value of each token to the StringBuilder
                    if(token.getType() == Token.Type.IDENTIFIER) tokenValuesBuilder.append(variables.get(token.getValue()));
                    else tokenValuesBuilder.append(token.getValue());
                }
                if(tokens.size() == 1){
                    variables.get(var.getValue()).setValue(tokens.get(0).getValue());
                }else if(isLogicalStatement(tokens)){
                    LogicalCalculator logicalCalculator = new LogicalCalculator();
                    variables.get(var.getValue()).setValue(Boolean.toString(logicalCalculator.evaluate(tokens)).toUpperCase());
                }else if(containsFloat(tokens)) {
                    try {
                        variables.get(var.getValue()).setValue(Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                }else {
                    try {
                        variables.get(var.getValue()).setValue(Integer.toString((int) ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                }
            }
            else error("Variable: " + var +" must be declared first");
        }
        match(Token.Type.NEWLINE);
    }

    private void ifStatement() {
        match(Token.Type.KEYWORD, "if");
        match(Token.Type.DELIMITER, "(");
        Boolean parseStatement = ifExpression();
        match(Token.Type.DELIMITER, ")");
        consume();
        match(Token.Type.KEYWORD, "begin");
        match(Token.Type.KEYWORD, "if");
        consume();
        if(parseStatement){
            while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
                statement();
            }
        } else{
            int nestedCount = 0;
            while(true){
                if(currToken().getValue().equals("begin") && peek().getValue().equals("if"))
                    nestedCount++;
                if(currToken().getValue().equals("end") && peek().getValue().equals("if"))
                    nestedCount--;
                if(nestedCount == -1){
                    break;
                }
                consume();
            }
        }
        match(Token.Type.KEYWORD, "end");
        match(Token.Type.KEYWORD, "if"); //first if statement finished
        consume();
        List<Boolean> ifelseResult = new LinkedList<>();
        ifelseResult.add(parseStatement);

//        check for multiple alternatives
        while (true) {
            if(currToken().getType() == Token.Type.KEYWORD && currToken().getValue().equals("else")){ //if else keyword encountered
                consume();
                if(currToken().getType() == Token.Type.KEYWORD && currToken().getValue().equals("if")){ //should keep checking for else ifs
                    consume();
                    match(Token.Type.DELIMITER, "(");
                    Boolean parseStatement2 = ifExpression();
                    match(Token.Type.DELIMITER, ")");
                    consume();
                    match(Token.Type.KEYWORD, "begin");
                    match(Token.Type.KEYWORD, "if");
                    consume();
                    if(parseStatement2 && !ifelseResult.contains(true)){
                        while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
                            statement();
                        }
                    } else {
                        int nestedCount = 0;
                        while(true){
                            if(currToken().getValue().equals("begin") && peek().getValue().equals("if"))
                                nestedCount++;
                            if(currToken().getValue().equals("end") && peek().getValue().equals("if"))
                                nestedCount--;
                            if(nestedCount == -1){
                                break;
                            }
                            consume();
                        }
                    }
                    ifelseResult.add(parseStatement2);
                    match(Token.Type.KEYWORD, "end");
                    match(Token.Type.KEYWORD, "if");
                    consume();
                } else {
                    consume();
                    match(Token.Type.KEYWORD, "begin");
                    match(Token.Type.KEYWORD, "if");
                    consume();
                    if(ifelseResult.contains(true)){
                        int nestedCount = 0;
                        while(true){
                            if(currToken().getValue().equals("begin") && peek().getValue().equals("if"))
                                nestedCount++;
                            if(currToken().getValue().equals("end") && peek().getValue().equals("if"))
                                nestedCount--;
                            if(nestedCount == -1){
                                break;
                            }
                            consume();
                        }
                    } else {
                        while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
                            statement();
                        }
                    }
                    match(Token.Type.KEYWORD, "end");
                    match(Token.Type.KEYWORD, "if");
                    consume();
                    break;
                }
            }else{
                break;
            }
        }

    }

    private Object expression(List<Token> expressionTokens) {
        List<Token> tokens = new LinkedList<>();
        for (Token token: expressionTokens){
//            System.out.println("Original Tokens: "+token);
            if(token.getType() == Token.Type.IDENTIFIER) {
                Token tok = variables.get(token.getValue());
                if(tok != null)
                tokens.add(variables.get(token.getValue()));
                else error(token + " is not declared");
            }
            else tokens.add(token);
        }
        StringBuilder tokenValuesBuilder = new StringBuilder();
        for (Token token: tokens){
            tokenValuesBuilder.append(token.getValue());
        }
        Object result = null;
        if(isLogicalStatement(tokens)){
            LogicalCalculator logicalCalculator = new LogicalCalculator();
            List<Token> logicalTokens = new LinkedList<>();
            for(int i=0;i< tokens.size();i++){
                if(i<tokens.size()-1) {
                    if (isNumberorFloat(tokens.get(i)) && isArithOperator(tokens.get(i + 1))) {
                        StringBuilder arithmeticBuilder = new StringBuilder();
                        arithmeticBuilder.append(tokens.get(i).getValue());
                        arithmeticBuilder.append(tokens.get(i + 1).getValue());
                        arithmeticBuilder.append(tokens.get(i + 2).getValue());
//                        System.out.println(arithmeticBuilder.toString() + "result");
                        double res = 0;
                        try {
                            res = ArithInterpreter.getResult(arithmeticBuilder.toString());
                        } catch (Exception e) {
                            error("Invalid operation: " + arithmeticBuilder.toString());
                        }
                        Token newToken = new Token(Token.Type.FLOAT, Double.toString(res));
                        logicalTokens.add(newToken);
                        i+=2;
                    }else{
                        logicalTokens.add(tokens.get(i));
                    }
                }
                if(i == tokens.size()-1) logicalTokens.add(tokens.get(i));
            }

            result = Boolean.toString(logicalCalculator.evaluate(logicalTokens));
        }else if(containsFloat(tokens)) {
            try {
                result = Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.err.println("Invalid operation: " + tokenValuesBuilder.toString());
            }
        }
        else{
            try {
                result = Integer.toString((int)ArithInterpreter.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.err.println("Invalid operation: " + tokenValuesBuilder.toString());
            }
        }
        return result;
    }

    private boolean ifExpression(){
        LogicalCalculator logicalCalculator = new LogicalCalculator();
        List<Token> tokensForIf = new LinkedList<>();
        while(peek().getType()!=Token.Type.NEWLINE && currToken().getValue()!=")"){
             tokensForIf.add(currToken());
            consume();
        }
        return Boolean.parseBoolean(expression(tokensForIf).toString());
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
                error("Unexpected token: expected " + expectedType + " '" + expectedValue + "'"
                + " Current token: " + currToken());
            }
        }
    }

    private void consume() {
        currentTokenIndex++;
    }
    private void error(String message) {
        throw new RuntimeException("Error Occurred: " + message);
    }
    public static boolean containsFloat(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == Token.Type.FLOAT) {
                return true;
            }
        }
        return false;
    }
    public static boolean isNumberorFloat(Token token) {
        if(token.getType() == Token.Type.NUMBER || token.getType() == Token.Type.FLOAT)
            return true;
        return false;
    }
    public static boolean isArithOperator(Token token) {
        if(token.getValue().equals("+")||token.getValue().equals("-")||token.getValue().equals("/")||token.getValue().equals("*")||token.getValue().equals("%"))
            return true;
        return false;
    }
    public static boolean isLogicalStatement(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getValue().equals("==")||token.getValue().equals("<>")||token.getValue().equals(">=")||token.getValue().equals("<=")||token.getValue().equals(">")||token.getValue().equals("<")||token.getValue().equals("and")||token.getValue().equals("or")||token.getValue().equals("not")||token.getType() == Token.Type.BOOL) {
                return true;
            }
        }
        return false;
    }
}