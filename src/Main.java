import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import Arithmetic.ArithInterpreter;
import Arithmetic.MathTokenizer;

public class Main {

    static Scanner scanner = new Scanner(System.in);

    // Main method
    public static void main(String[] args) {
        // Define the filename containing the CODE program
        String filename = "src/sourceCode.txt";

        try {
            // Create a FileReader to read the file
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Create a StringBuilder to store the CODE program
            StringBuilder codeBuilder = new StringBuilder();
            String line;
            // Read each line from the file and append it to the StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                codeBuilder.append(line).append("@\n");
            }

            // Close the BufferedReader
            bufferedReader.close();
            // Create a Lexer instance
            //System.out.println(codeBuilder.toString());
            Lexer lexer = new Lexer(codeBuilder.toString());
            List<Token> tokens = new ArrayList<>();
            System.out.println("Tokens:-----------------");
            Token token;
            int index = 0; // Initialize index counter

            do {
                token = lexer.getNextToken();
                tokens.add(token);
                System.out.println(token + " Index: " + index);
                index++;
            } while (token.getType() != Token.Type.EOF);
            System.out.println("End of Tokens----------");


            SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
            syntaxAnalyzer.parse();

//            Lexer lexer1 = new Lexer(codeBuilder.toString());
////             Create a SyntaxAnalyzer instance
//            SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexer1);
////             Parse the source code
//            System.out.println("Parsing the source code...");
//
//            System.out.println(syntaxAnalyzer.lexer.input);
//            syntaxAnalyzer.parse();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        System.out.println("Simple Arithmetic Interpreter. Enter an arithmetic expression[e.g.\"(2+3)*5\"] to evaluate the result.");
        System.out.println("type END to quit program.\n");

       
       
        run();

    }

   

    /**
     * Runs the interpreter and prints result to console
     */
    public static void run() {
        boolean flag = true;
        while (flag) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equals("END")) {
                flag = false;
            }
            else if (input.equals("")) System.out.println(0);
            else {
                try {
                    //System.out.println(ArithInterpreter.getResult(input));

                    System.out.println(ArithInterpreter.getLogicalResult(input));
                } catch (Exception ignored) {
                    System.out.println("Invalid Input");
                }
            }
        }
    }

}
