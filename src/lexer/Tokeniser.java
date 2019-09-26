package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    // more flexible string overload
    private void error(String s, int line, int col) {
        System.out.println("Lexing error: unrecognised character (" + s + ") at " + line + ":" + col);
        error++;
    }

    private void error(char c, int line, int col) {
        error(Character.toString(c), line, col);
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

    // https://www.baeldung.com/java-initialize-hashmap - section 2.
    private static final Map<Character, Character> escapedChars = new HashMap<Character, Character>() {{
        put('t',  '\t');
        put('b',  '\b');
        put('n',  '\n');
        put('r',  '\r');
        put('f',  '\f');
        put('0',  '\0');
        put('\'', '\'');
        put('"',  '\"');
        put('\\', '\\');
    }};

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        //  identifiers, types, keywords
        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            // peek to make sure scanner remains in correct position
            c = scanner.peek();

            while (Character.isLetterOrDigit(c) || c == '_') {
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
            
            // keep going for letters
            while(Character.isLetter(c)) {
                sb.append(c);
                scanner.next();
                
                if (sb.toString().equals("#include")) {
                    return new Token(TokenClass.INCLUDE, line, column);
                }
                
                c = scanner.peek();
            }

        }

        // string literals
        // "abcdef\""
        if (c == '"') {
            try {
                StringBuilder sb = new StringBuilder();
                // delimited by double quotes so no need to peek
                c = scanner.next();
                
                // until current character is string terminator
                while (c != '"') {
                    if (c == '\\') {
                        // escaped character
                        c = scanner.next();
                        if (escapedChars.containsKey(c)) {
                            // valid escape character
                            sb.append(escapedChars.get(c));
                        } else {
                            error("\\" + c, line, column);
                        }
                    } else {
                        sb.append(c);
                    }
                    c = scanner.next();
                }
                return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
            } catch (EOFException e) {
                // unclosed string - invalid
                error("unclosed string literal", line, column);
                return new Token(TokenClass.INVALID, "unclosed string literal", line, column);
            }
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

            return new Token(TokenClass.INT_LITERAL, sb.toString(), line, column);
            
        }
        
        // character literals
        // 'a', '\\', '\n', ...
        if (c == '\'') {
            StringBuilder sb = new StringBuilder();
            c = scanner.next();

            while (c != '\'') {
                if (c == '\\') {
                    c = scanner.next();
                    if (escapedChars.containsKey(c)) {
                        sb.append(escapedChars.get(c));
                    } else {
                        // invalid escape sequence
                        error("\\" + c, line, column);
                        sb.append('\\' + c);
                    }
                } else {
                    sb.append(c);
                }
                c = scanner.next();
            }

            if (sb.length() == 1) {
                return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
            } else {
                error("illegal char literal", line, column);
                return new Token(TokenClass.INVALID, "illegal char literal: " + sb.toString(), line, column);
            }

        }

        // attempt to grab next character ahead of time for 2-(non-alpha)character tokens
        // comments
        try {
            char n = scanner.peek();
            String nextPair = new String(new char[] {c, n});
            try {
                switch (nextPair) {
                    /** line comment */
                    case "//": {
                        // System.out.println("line comment!");
                        // stop comment at newline char '\n'
                        while (scanner.peek() != '\n') {
                            c = scanner.next();
                        }
                        // skip two characters
                        c = scanner.next();
                        return next();
                    }
    
                    /** multiline comment */
                    case "/*": {
                        // System.out.println("multiline comment!");
                        // skip to inside comment
                        scanner.next();
                        c = scanner.next();
                        while (!(c == '*' && scanner.peek() == '/')) {
                            c = scanner.next();
                        }
                        // skip two characters
                        c = scanner.next();
                        return next();
                    }
                }
            } catch (EOFException e) {
                return new Token(TokenClass.EOF, line, column);
            }
        } catch (EOFException e) {}
        // if EOF reached, simply continue to check for 1 more char

        try {
            char n = scanner.peek();
            String nextPair = new String(new char[] {c, n});
            switch(nextPair) {
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
            
        } catch (EOFException e) {}
        // end of file reached, only match basic tokens

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

            // match newline characters before EOF
            case '\n': return next();
        }

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

}
