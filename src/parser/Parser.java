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
            nextToken();
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

    private boolean isFunDecl() {
        // starting lookahead position
        int offset = 1;
        // struct has 1 extra token (IDENT)
        if (accept(TokenClass.STRUCT)) { offset++; }
        // reference has 1 extra token ("*")
        if (lookAhead(offset).tokenClass == TokenClass.ASTERIX) { offset++; }
        // final offset to next token
        offset++;
        TokenClass tc = lookAhead(offset).tokenClass;
        return tc == TokenClass.LPAR;
    }

    private boolean isVarDecl() {
        // starting lookahead position
        int offset = 1;
        // struct has 1 extra token (IDENT)
        if (accept(TokenClass.STRUCT)) { offset++; }
        // reference has 1 extra token ("*")
        if (lookAhead(offset).tokenClass == TokenClass.ASTERIX) { offset++; }
        // final offset to next token
        offset++;
        TokenClass tc = lookAhead(offset).tokenClass;
        return (tc == TokenClass.SC || tc == TokenClass.LSBR);
    }

    private void parseProgram() {
        parseIncludes();
        parseStructDecls();
        // parseVarDecls();
        parseGlobalVarDecls();
        // parseFunDecls();
        parseGlobalFunDecls();
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

    private void parseGlobalVarDecls() {
        if (acceptsType() && isVarDecl()) {
            parseVarDecls();
        }
    }
    
    private void parseVarDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        parseArrayDecl();
        expect(TokenClass.SC);
    }

    // can scoop up function declarations if no lookahead
    private void parseVarDecls() {
        if (acceptsType() && isVarDecl()) {
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

    private void parseGlobalFunDecls() {
        if (acceptsType() && isFunDecl()) {
            parseFunDecls();
        }
    }

    private void parseFunDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        parseBlock();
    }

    private void parseFunDecls() {
        if (acceptsType() && isFunDecl()) {
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
        if (accept(TokenClass.LPAR)) {
            nextToken();
            if (acceptsType()) {
                // typecast!!
                parseTypecast();
            } else {
                // just a _regular_ expression
                parseExp();
                expect(TokenClass.RPAR);
            }
        } else {
            parseExp8();
        }
    }

    private void parseExp8() {
        parseExp7();
        if (accept(TokenClass.OR)) {
            nextToken();
            parseExp8();
        }
    }
    
    private void parseExp7() {
        parseExp6();
        if (accept(TokenClass.AND)) {
            nextToken();
            parseExp7();
        }
    }

    private void parseExp6() {
        parseExp5();
        if (accept(
            TokenClass.EQ,
            TokenClass.NE
        )) {
            nextToken();
            parseExp6();
        }
    }

    private void parseExp5() {
        parseExp4();
        if (accept(
            TokenClass.LT,
            TokenClass.LE,
            TokenClass.GT,
            TokenClass.GE
        )) {
            nextToken();
            parseExp5();
        }
    }

    private void parseExp4() {
        parseExp3();
        if (accept(
            TokenClass.PLUS,
            TokenClass.MINUS
        )) {
            nextToken();
            parseExp4();
        }
    }

    private void parseExp3() {
        parseExp2();
        if (accept(
            TokenClass.ASTERIX,
            TokenClass.DIV,
            TokenClass.REM
        )) {
            nextToken();
            parseExp3();
        }
    }

    // does not consume first parenthesis as checking 
    // for typecast requires using nextToken()
    private void parseTypecast() {
        parseType();
        expect(TokenClass.RPAR);
        parseExp();
    }

    private void parseExp2() {
        if (accept(
            TokenClass.MINUS,
            TokenClass.ASTERIX
        )) {
            // minus or valueat
            nextToken();
            parseExp();
        } else if (accept(TokenClass.SIZEOF)) {
            nextToken();
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);
        } else if (accept(TokenClass.LPAR)) {
            // typecast #2
            nextToken();
            parseTypecast();
        } else {
            parseExp1();
        }
    }

    private void parseExp1() {
        // check for function calls
        TokenClass la = lookAhead(1).tokenClass;
        if (accept(TokenClass.IDENTIFIER) && la == TokenClass.LPAR) {
            parseFunCall();
        } else {
            parseExp0();
        }
        parseAccess();
    }

    private void parseAccess() {
        if (accept(TokenClass.LSBR)) {
            nextToken();
            parseExp();
            expect(TokenClass.RSBR);
            parseAccess();
        } else if (accept(TokenClass.DOT)) {
            nextToken();
            expect(TokenClass.IDENTIFIER);
            parseAccess();
        }
    }

    private void parseExp0() {
        expect(
            TokenClass.IDENTIFIER,
            TokenClass.INT_LITERAL,
            TokenClass.STRING_LITERAL,
            TokenClass.CHAR_LITERAL
        );
    }

    private void parseFunCall() {
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        if (!accept(TokenClass.RPAR)) {
            parseOptExpPlus();
        }
        expect(TokenClass.RPAR);
    }
    
    private void parseOptExpPlus() {
        parseExp();
        parseExpPlus();
    }

    private void parseExpPlus() {
        if (accept(TokenClass.COMMA)) {
            nextToken();
            parseExp();
            parseExpPlus();
        }
    }
}
