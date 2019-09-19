package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	    error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */
    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // attempt to grab next character ahead of time
        try {
            char p = scanner.peek();

            String nextPair = new String(new char[] {c, p});
            switch (nextPair) {
                /** line comment */
                case "//": {
                    char curr = scanner.next();
                    // brute-force newline as '\n'
                    while (curr != '\n') {
                        curr = scanner.next();
                    }
                    return next();
                }
                /** multiline comment */
                case "/*": {
                    char curr = scanner.next();
                    String end = new String(new char[] {curr, scanner.peek()});
                    while (!end.equals("*/")) {
                        curr = scanner.next();
                        end = new String(new char[] {curr, scanner.peek()});
                    }
                    // skip two characters
                    scanner.next();
                    return next();
                }
                case "==": {
                    scanner.next();
                    return new Token(TokenClass.EQ, line, column);
                }
            }
            
            // if (nextPair.equals("//")) {
            //     char curr = scanner.next();
            //     // brute-force newline as '\n'
            //     while (curr != '\n') {
            //         curr = scanner.next();
            //     }
            //     return next();
            // }
            // if (nextPair.equals("/*")) {
            //     char curr = scanner.next();
            //     String end = new String(new char[] {curr, scanner.peek()});
            //     while (!end.equals("*/")) {
            //         curr = scanner.next();
            //         end = new String(new char[] {curr, scanner.peek()});
            //     }
            //     // skip two characters
            //     scanner.next();
            //     return next();
            // }
        } catch (EOFException e) {
            // end of file reached, only match basic tokens
        }

        /** --- basic tokens --- */
        switch (c) {
            /** delimiters */
            case '{': return new Token(TokenClass.LBRA, line, column);
            case '}': return new Token(TokenClass.RBRA, line, column);
            case '(': return new Token(TokenClass.LPAR, line, column);
            case ')': return new Token(TokenClass.RPAR, line, column);
            case '[': return new Token(TokenClass.LSBR, line, column);
            case ']': return new Token(TokenClass.RSBR, line, column);
            case ';': return new Token(TokenClass.SC, line, column);
            case ',': return new Token(TokenClass.COMMA, line, column);
            
            /** operators */
            case '+': return new Token(TokenClass.PLUS, line, column);
            case '-': return new Token(TokenClass.MINUS, line, column);
            case '*': return new Token(TokenClass.ASTERIX, line, column);
            case '/': return new Token(TokenClass.DIV, line, column);
            case '%': return new Token(TokenClass.REM, line, column);
            
            /** struct member access */
            case '.': return new Token(TokenClass.DOT, line, column);
            
            /** comparisons */
            case '=': {
                if (p == '=') {
                    scanner.next();
                    return new Token(TokenClass.EQ, line, column);
                }
                // not an EQ, default to assign
                return new Token(TokenClass.ASSIGN, line, column);
            }
            case '<': {
                if (p == '=') {
                    scanner.next();
                    return new Token(TokenClass.LE, line, column);
                }
                return new Token(TokenClass.LT, line, column);                
            }
            case '>': {
                if (p == '=') {
                    scanner.next();
                    return new Token(TokenClass.GE, line, column);
                }
                return new Token(TokenClass.GT, line, column);                
            }

            /** logical operators */
            case '&': {
                if (p == '&') {
                    scanner.next();
                    return new Token(TokenClass.AND, line, column);
                }
                // dereference?
                // return new Token(TokenClass.?, line, column);
            }
            case '|': {
                if (p == '|') {
                    scanner.next();
                    return new Token(TokenClass.OR, line, column);
                }
            }
        }

        /** --- text tokens --- */
        if (Character.isLetter(c)) {
            /** types */

            /** keywords */

            /** include */

            /** literals */
        }




        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

}
