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

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        //  identifiers, types, keywords
        if (Character.isLetter(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isLetterOrDigit(c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }

            switch (sb.toString()) {
                // types
                case "int"   : return new Token(TokenClass.INT,    line, column);
                case "void"  : return new Token(TokenClass.VOID,   line, column);
                case "char"  : return new Token(TokenClass.CHAR,   line, column);
            
                // keywords
                case "if"    : return new Token(TokenClass.IF,     line, column);
                case "else"  : return new Token(TokenClass.ELSE,   line, column);
                case "while" : return new Token(TokenClass.WHILE,  line, column);
                case "return": return new Token(TokenClass.RETURN, line, column);
                case "struct": return new Token(TokenClass.STRUCT, line, column);
                case "sizeof": return new Token(TokenClass.SIZEOF, line, column);

                // just an identifier
                default: return new Token(TokenClass.IDENTIFIER, sb.toString(), line, column);
            }
        }

        // #include
        if (c == '#') {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            
            while(Character.isLetterOrDigit(c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }

            if (sb.toString().equals("#include")) {
                return new Token(TokenClass.INCLUDE, line, column);
            } else {
                return new Token(TokenClass.INVALID, sb.toString(), line, column);
            }
        }

        // string literals
        // "abcdef\""
        if (c == '"') {
            StringBuilder sb = new StringBuilder();
            c = scanner.next();
            char n = scanner.peek();
            
            while (c != '"') {
                if (c != '\\') {
                    sb.append(c);
                }
                String nextPair = new String(new char[] {c, n});
                if (nextPair.equals("\\\"")) {
                    sb.append(scanner.next());
                    c = scanner.peek();
                }
                c = scanner.next();
            }
            return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
        }

        // int literals
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            // read digits
            while (Character.isDigit(c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            // finished reading digits, check proper termination
            if (!Character.isLetter(c)) {
                return new Token(TokenClass.INT_LITERAL, sb.toString(), line, column);
            } else {
                // improper termination, get whole invalid token
                while (Character.isLetterOrDigit(c)) {
                    scanner.next();
                    c = scanner.peek();
                }
                return new Token(TokenClass.INVALID, line, column);
            }
        }

        // attempt to grab next character ahead of time for 2-character tokens
        try {
            char n = scanner.peek();

            String nextPair = new String(new char[] {c, n});
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

                /** comparisons */
                case "!=": {
                    scanner.next();
                    return new Token(TokenClass.NE, line, column);
                }
                case "==": {
                    scanner.next();
                    return new Token(TokenClass.EQ, line, column);
                }
                case "<=": {
                    scanner.next();
                    return new Token(TokenClass.LE, line, column);
                }
                case ">=": {
                    scanner.next();
                    return new Token(TokenClass.GE, line, column);
                }

                /** logical operators */
                case "&&": {
                    scanner.next();
                    return new Token(TokenClass.AND, line, column);
                }
                case "||": {
                    scanner.next();
                    return new Token(TokenClass.OR, line, column);
                }
            }
            
        } catch (EOFException e) {
            // end of file reached, only match basic tokens
        }

        /** --- basic tokens --- */
        switch (c) {
            /** delimiters */
            case '{': return new Token(TokenClass.LBRA,    line, column);
            case '}': return new Token(TokenClass.RBRA,    line, column);
            case '(': return new Token(TokenClass.LPAR,    line, column);
            case ')': return new Token(TokenClass.RPAR,    line, column);
            case '[': return new Token(TokenClass.LSBR,    line, column);
            case ']': return new Token(TokenClass.RSBR,    line, column);
            case ';': return new Token(TokenClass.SC,      line, column);
            case ',': return new Token(TokenClass.COMMA,   line, column);
            
            /** operators */
            case '+': return new Token(TokenClass.PLUS,    line, column);
            case '-': return new Token(TokenClass.MINUS,   line, column);
            case '*': return new Token(TokenClass.ASTERIX, line, column);
            case '/': return new Token(TokenClass.DIV,     line, column);
            case '%': return new Token(TokenClass.REM,     line, column);
            
            /** struct member access */
            case '.': return new Token(TokenClass.DOT,     line, column);
            
            /** comparisons */
            case '=': return new Token(TokenClass.ASSIGN,  line, column);
            case '<': return new Token(TokenClass.LT,      line, column);
            case '>': return new Token(TokenClass.GT,      line, column);

        }

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

}
