package parser;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;



    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();

        parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }

    private boolean acceptsType() {
        return accept(
            TokenClass.INT,
            TokenClass.CHAR,
            TokenClass.VOID,
            TokenClass.STRUCT
        );
    }


    private void parseProgram() {
        parseIncludes();
        parseStructDecls();
        parseVarDecls();
        parseFunDecls();
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructType() {
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseStructDecls() {
        if (accept(TokenClass.STRUCT)) {
            parseStructType();
            expect(TokenClass.LBRA);
            parseVarDecl();     // at least one var
            parseVarDecls();    // extra vars
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            parseStructDecls();
        }
    }
    
    private void parseType() {
        if (accept(TokenClass.STRUCT)) {
            parseStructType();
        } else {
            expect(
                TokenClass.INT,
                TokenClass.CHAR,
                TokenClass.VOID
            );
        }
        parseReference();
    }

    private void parseReference() {
        if (accept(TokenClass.ASTERIX)) {
            nextToken();
        }
    }
    
    private void parseVarDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        parseArrayDecl();
        expect(TokenClass.SC);
    }

    private void parseVarDecls() {
        if (acceptsType()) {
            parseVarDecl();
            parseVarDecls();
        }
    }

    private void parseArrayDecl() {
        if (accept(TokenClass.LSBR)) {
            nextToken();
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
        }
    }

    private void parseFunDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        // parseBlock();
    }

    private void parseFunDecls() {
        if (acceptsType()) {
            parseFunDecl();
            parseFunDecls();
        }
    }
    
    private void parseParams() {
        if (acceptsType()) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            parseParamsPlus();
        }
    }

    private void parseParamsPlus() {
        if (accept(TokenClass.COMMA)) {
            nextToken();
            parseType();
            expect(TokenClass.IDENTIFIER);
            parseParamsPlus();
        }
    }

    private void parseBlock() {
        expect(TokenClass.LBRA);
        parseVarDecls();
        parseStmts();
        expect(TokenClass.RBRA);
    }

    private void parseStmts() {
        // if next token not a closing brace, still in block
        if (!accept(TokenClass.RBRA)) {
            parseStmt();
            parseStmts();
        }
    }

    private void parseElseStmt() {
        if (accept(TokenClass.ELSE)) {
            nextToken();
            parseStmt();
        }
    }

    private void parseStmt() {
        if (accept(TokenClass.LBRA)) {
            parseBlock();
        } else if (accept(TokenClass.WHILE)) {
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
        } else if (accept(TokenClass.IF)) {
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
            parseElseStmt();
        } else if (accept(TokenClass.RETURN)) {
            nextToken();
            parseOptExp();
            expect(TokenClass.SC);
        } else {
            parseExp();
            if (accept(TokenClass.ASSIGN)) {
                nextToken();
                parseExp();
            }
            expect(TokenClass.SC);
        }
    }

    private void parseOptExp() {
        if (!accept(TokenClass.SC)) {
            parseExp();
        }
    }

    private void parseExp() {
    }
}
