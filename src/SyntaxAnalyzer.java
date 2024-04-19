import java.util.*;
import Arithmetic.ArithInterpreter;

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
                else if(tokens.get(currentTokenIndex).getValue().equals("if")){ // BEGIN IF

                }
            } else if (currentToken.getType() == Token.Type.KEYWORD && currentToken.getValue().equals("end")) {
                consume();
                if(tokens.get(currentTokenIndex).getValue().equals("code")){
                    consume();
//                    System.out.println("\nFinished Coding");
                    if(currToken().getType() == Token.Type.NEWLINE) consume();
                    else error("Expected a NEWLINE");
                    if(currToken().getType() == Token.Type.EOF) consume();
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
    private void whileStatement(){
        match(Token.Type.KEYWORD,"while");
        List<Token> expressionTokens = new LinkedList<>();
        while(currToken().getType()!= Token.Type.NEWLINE){
//            System.out.println("Current: "+currToken());
            expressionTokens.add(currToken());
            consume();
        }
        match(Token.Type.NEWLINE);
        for (Token token: expressionTokens) {
//            System.out.println(token+" Expression tokens");
        }
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
//        System.out.println("Cur: "+ currToken());
//        System.out.println("Peek: "+ peek());

        int endwhileIndex = currentTokenIndex;

//        System.out.println("First Result: "+expression(expressionTokens));

        while (Boolean.parseBoolean(expression(expressionTokens).toString())){
//            System.out.println("Result: "+expression(expressionTokens));
            currentTokenIndex = startwhileIndex;
//            parse();
//            parseFromTo(startwhileIndex,endwhileIndex);
            for(int i=0;i< countNewline(startwhileIndex,endwhileIndex);i++){
                statement();
            }
        }
//        System.out.println("Start: "+ startwhileIndex);
//        System.out.println("End: "+ endwhileIndex);

        //match(Token.Type.NEWLINE);
        currentTokenIndex = endwhileIndex;

        //System.out.println(currToken());


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
                    else throw new RuntimeException("Variable: "+ currToken().getValue() + " is not yet declared");
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
                if(currToken().getType() != expectedDataType ){
                    if(currToken().getType() == Token.Type.IDENTIFIER){
                        Token.Type varDataType = variables.get(currToken().getValue()).getType();
                        switch (varDataType){
                            case CHAR:
                                if(expectedDataType != Token.Type.CHAR)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                            case NUMBER:
                            case FLOAT:
                                if(expectedDataType == Token.Type.CHAR || expectedDataType == Token.Type.BOOL)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                            case BOOL:
                                if(expectedDataType != Token.Type.BOOL)
                                    throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+varDataType);
                                break;
                        }
                    }
                    if((expectedDataType == Token.Type.CHAR || expectedDataType == Token.Type.BOOL) && (currToken().getType() == Token.Type.NUMBER || currToken().getType() == Token.Type.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken().getType());
                    if((currToken().getType() == Token.Type.CHAR || currToken().getType() == Token.Type.BOOL) && (expectedDataType == Token.Type.NUMBER || expectedDataType == Token.Type.FLOAT))
                        throw new IllegalArgumentException("Unmatched datatype Expected datatype: "+expectedDataType + " Defined datatype: "+currToken().getType());
                }
//                System.out.println(currToken());
                if(peek().getType() != Token.Type.OPERATOR && currToken().getType() != Token.Type.IDENTIFIER) {
                    if(expectedDataType == Token.Type.NUMBER){
                        currToken().setValue(Integer.toString((int)currToken().getDataType()));
                    }
                    variables.put(varname, currToken());
//                System.out.println("Declared "+varname+": " +initializedVariables.get(varname));
                    consume();
                }else {
                    StringBuilder tokenValuesBuilder = new StringBuilder();
                    while (currToken().getType() != Token.Type.NEWLINE && currToken().getType() != Token.Type.DELIMITER) {

                        if(currToken().getType() == Token.Type.IDENTIFIER) {
                            tokenValuesBuilder.append(variables.get(currToken().getValue()).getDataType());
                        }
                        else
                            tokenValuesBuilder.append(currToken().getValue());
                        consume();
                    }
//                    System.out.println(tokenValuesBuilder.toString());
                    try {
                        if(expectedDataType == Token.Type.NUMBER)
                            variables.put(varname, new Token(expectedDataType,Integer.toString((int)ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                        else
                            variables.put(varname, new Token(expectedDataType,Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString()))));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }else if(currToken().getValue().equals(",")){
                consume(); // Consume DELIMITER token
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
            }else if(currToken().getType() == Token.Type.NEWLINE){
                variables.put(varname,new Token(expectedDataType,null));
//                System.out.println(initializedVariables.containsKey(varname)+varname);
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
                consume(); // Consume the ASSIGNMENT token
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
//                        System.out.println(variables.get(currToken().getValue()));
                        tokens.add(variables.get(currToken().getValue()));
                        consume();
                    }else{
                        tokens.add(currToken());
                        consume();
                    }
                }
            }
        }
//        for(Token token: tokens) System.out.println(token+"TOKEN EXPRESSION");
        for(Token var: identifiers){
            if(variables.containsKey(var.getValue())){
                StringBuilder tokenValuesBuilder = new StringBuilder();
                for (Token token : tokens) {
                    // Append the value of each token to the StringBuilder
                    if(token.getType() == Token.Type.IDENTIFIER) tokenValuesBuilder.append(variables.get(token.getValue()));
                    else tokenValuesBuilder.append(token.getValue());
                }
                if(isLogicalStatement(tokens)){
                    LogicalCalculator logicalCalculator = new LogicalCalculator();
                    variables.get(var.getValue()).setValue(Boolean.toString(logicalCalculator.evaluate(tokens)));
                }else if(containsFloat(tokens))
                    try{
                        variables.get(var.getValue()).setValue(Double.toString(ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                else
                    try{
                        variables.get(var.getValue()).setValue(Integer.toString((int)ArithInterpreter.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
            }
            else error("Variable: " + var +" must be declared first");
        }
        match(Token.Type.NEWLINE);
    }

    private void ifStatement() {
        //Issues: IF(True), ELSE IF(T). Both will execute
        match(Token.Type.KEYWORD, "if");
        match(Token.Type.DELIMITER, "(");
        Boolean parseStatement = ifExpression();
        match(Token.Type.DELIMITER, ")");
        consume(); //the consume function calls just consume the newlines (it throws an error if you dont consume the newline)
        match(Token.Type.KEYWORD, "begin");
        match(Token.Type.KEYWORD, "if");
        consume();
        if(parseStatement){
            while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
                statement();
            }
        } else{
//            while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
//                consume();
//            }
            int nestedCount = 0;
            while(true){
//                System.out.println("Concomed: " + currToken() + " Count: " + nestedCount);
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
//                        while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
//                            consume();
//                        }
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
//                    System.out.println(ifelseResult.contains(true));
                    if(ifelseResult.contains(true)){
//                        while (currToken().getType() != Token.Type.KEYWORD || !currToken().getValue().equals("end")) {
//                            consume();
//                        }
                        int nestedCount = 0;
                        while(true){
//                System.out.println("Concomed: " + currToken() + " Count: " + nestedCount);
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
            if(token.getType() == Token.Type.IDENTIFIER)
                tokens.add(variables.get(token.getValue()));
            else tokens.add(token);
        }

//        for (Token token: tokens){
//            System.out.println("Valued Tokens: "+token);
//        }

        ListIterator<Token> iterator = tokens.listIterator();

        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.getType() == Token.Type.OPERATOR) {
                // Insert a new token two steps after the current position
                for (int i = 0; i < 1 && iterator.hasNext(); i++) {
                    iterator.next();
                }
                iterator.add(new Token(Token.Type.DELIMITER, ")")); // Adjust the type and value accordingly
                // Move the iterator back to the original position
                for (int i = 0; i < 2 && iterator.hasPrevious(); i++) {
                    iterator.previous();
                }
                // Insert a new token two steps before the current position
                for (int i = 0; i < 2 && iterator.hasPrevious(); i++) {
                    iterator.previous();
                }
                iterator.add(new Token(Token.Type.DELIMITER, "(")); // Adjust the type and value accordingly
                // Move the iterator back to the original position
                for (int i = 0; i < 2 && iterator.hasNext(); i++) {
                    iterator.next();
                }
            }
        }

//        for (Token token: tokens){
//            System.out.println("Enclosed Tokens: "+token);
//        }
        Object result = null;
        if(isLogicalStatement(tokens)){
            LogicalCalculator logicalCalculator = new LogicalCalculator();
            result = Boolean.toString(logicalCalculator.evaluate(tokens));
        }else if(containsFloat(tokens))
            result = Double.toString(Calculator.evaluateArithmeticExpression(tokens,Token.Type.FLOAT));
        else
            result = Integer.toString(Calculator.evaluateArithmeticExpression(tokens));
        return result;
    }

    private boolean ifExpression(){
//        System.out.println("nisud sa if");
        LogicalCalculator logicalCalculator = new LogicalCalculator();
        List<Token> tokensForIf = new LinkedList<>();
        while(currToken().getType()!=Token.Type.DELIMITER && currToken().getValue()!=")"){
//            System.out.print(currToken().getValue());
             tokensForIf.add(currToken());
            consume();
        }
        for (Token token : tokensForIf) {
//            System.out.println(token);
        }
//        System.out.println("\nResult: "+ logicalCalculator.evaluate(tokensForIf));
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
                error("Unexpected token type or value, expected " + expectedType + " '" + expectedValue + "'"
                + " Current token: " + currToken().getType() + ", " + currToken().getValue());
            }
        }
    }

    private void consume() {
//        System.out.print("Consumed Token:");
//        System.out.println(tokens.get(currentTokenIndex));
        currentTokenIndex++;
    }

    private void consumePrint() {
        System.out.print("Consumed Token:");
        System.out.println(tokens.get(currentTokenIndex));
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