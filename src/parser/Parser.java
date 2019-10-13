package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
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

    /**
     * utility function for accept with arbitrary lookahead
     * @param i lookahead value
     * @param expected list of TokenClasses to be accepted
     * @return true if lookahead token's class matches expected
     */
    private boolean acceptWithLookahead(int i, TokenClass...expected) {
        boolean result = false;
        TokenClass la = lookAhead(i).tokenClass;
        for (TokenClass e : expected)
            result |= (e == la);
        return result;
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


    // ------------------- parse functions ---------------

    private Program parseProgram() {
        parseIncludes();
        List<StructTypeDecl> stds = parseStructDecls();
        List<VarDecl> vds = parseGlobalVarDecls();
        List<FunDecl> fds = parseGlobalFunDecls();
        expect(TokenClass.EOF);
        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private StructType parseStructType() {
        expect(TokenClass.STRUCT);
        String st = expect(TokenClass.IDENTIFIER).data;

        return new StructType(st);
    }

    private List<StructTypeDecl> parseStructDecls() {
        List<StructTypeDecl> stds = new ArrayList<StructTypeDecl>();

        if (accept(TokenClass.STRUCT)) {
            StructType st = parseStructType();
            List<VarDecl> vds = new ArrayList<VarDecl>();

            expect(TokenClass.LBRA);

            vds.add(parseVarDecl());     // at least one var
            vds.addAll(parseVarDecls());    // extra vars

            expect(TokenClass.RBRA);
            expect(TokenClass.SC);

            stds.add(new StructTypeDecl(st, vds));

            stds.addAll(parseStructDecls());
        }
        return stds;
    }
    
    private Type parseType() {
        if (accept(TokenClass.STRUCT)) {
            StructType st = parseStructType();
            if (parseReference()) {
                return new PointerType(st);
            } else {
                return st;
            }
        } else {
            Token t = expect(
                TokenClass.INT,
                TokenClass.CHAR,
                TokenClass.VOID
            );
            BaseType bt = null;
            switch (t.tokenClass) {
                case INT:  bt = BaseType.INT;
                case CHAR: bt = BaseType.CHAR;
                case VOID: bt = BaseType.VOID;
                default:
                    break;
            }
            if(parseReference()) {
               return new PointerType(bt); 
            } else {
                return bt;
            }
        }
    }

    private boolean parseReference() {
        if (accept(TokenClass.ASTERIX)) {
            nextToken();
            return true;
        }
        return false;
    }

    private List<VarDecl> parseGlobalVarDecls() {
        List<VarDecl> vds = new ArrayList<>();
        if (acceptsType() && isVarDecl()) {
            vds.addAll(parseVarDecls());
        }
        return vds;
    }
    
    private VarDecl parseVarDecl() {
        Type t = parseType();
        String name = expect(TokenClass.IDENTIFIER).data;
        int i = parseArrayDecl();
        expect(TokenClass.SC);
        if (i > -1) {
            t = new ArrayType(t, i);
        }
        return new VarDecl(t, name);
    }

    // can scoop up function declarations if no lookahead
    private List<VarDecl> parseVarDecls() {
        List<VarDecl> vds = new ArrayList<>();
        if (acceptsType() && isVarDecl()) {
            vds.add(parseVarDecl());
            vds.addAll(parseVarDecls());
        }
        return vds;
    }

    private int parseArrayDecl() {
        if (accept(TokenClass.LSBR)) {
            nextToken();
            int i = Integer.parseInt(expect(TokenClass.INT_LITERAL).data);
            expect(TokenClass.RSBR);
            return i;
        }
        // no array declaration
        return -1;
    }

    private List<FunDecl> parseGlobalFunDecls() {
        List<FunDecl> fds = new ArrayList<>();
        if (acceptsType() && isFunDecl()) {
            fds.addAll(parseFunDecls());
        }
        return fds;
    }

    private FunDecl parseFunDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        parseBlock();
        return new FunDecl(
            BaseType.INT, 
            "no name mista", 
            new ArrayList<>(), 
            new Block(new ArrayList<>(), new ArrayList<>())
        );
    }

    private List<FunDecl> parseFunDecls() {
        List<FunDecl> fds = new ArrayList<>();
        if (acceptsType() && isFunDecl()) {
            fds.add(parseFunDecl());
            fds.addAll(parseFunDecls());
        }
        return fds;
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
        // if next token not a closing brace, still in block [also check if EOF - prevent infinite recursion]
        if (!accept(TokenClass.RBRA, TokenClass.EOF)) {
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
        parseExp8();
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

    private void parseTypecast() {
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
        parseExp2();
    }

    private void parseExp2() {
        if (accept(
            TokenClass.MINUS,
            TokenClass.ASTERIX
        )) {
            // minus or valueat
            nextToken();
            parseExp2();
        } else if (accept(TokenClass.SIZEOF)) {
            nextToken();
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);
        } else if (
            accept(TokenClass.LPAR) && 
            acceptWithLookahead(1, 
                TokenClass.INT,
                TokenClass.CHAR,
                TokenClass.VOID,
                TokenClass.STRUCT
            )) { parseTypecast(); }
        else {
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
        if (accept(TokenClass.LPAR)) {
            nextToken();
            parseExp();
            expect(TokenClass.RPAR);
        } else {
            expect(
                TokenClass.IDENTIFIER,
                TokenClass.INT_LITERAL,
                TokenClass.STRING_LITERAL,
                TokenClass.CHAR_LITERAL
            );
        }
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
