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

    private boolean isStructDecl() {
        // starting lookahead position
        int offset = 1;
        // check is struct
        if (!accept(TokenClass.STRUCT) || !(lookAhead(offset).tokenClass == TokenClass.IDENTIFIER)) { 
            return false; 
        }
        offset++;
        
        // reference has 1 extra token ("*")
        if (lookAhead(offset).tokenClass == TokenClass.ASTERIX) { offset++; }
        // final offset to next token
        TokenClass tc = lookAhead(offset).tokenClass;
        return (tc == TokenClass.LBRA);
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
        // sane default
        String st = "-- invalid struct --";

        expect(TokenClass.STRUCT);
        Token t = expect(TokenClass.IDENTIFIER);
        // error handling for null token
        if (t != null) {
            st = t.data;
        }
        return new StructType(st);
    }

    private List<StructTypeDecl> parseStructDecls() {
        List<StructTypeDecl> stds = new ArrayList<>();

        if (isStructDecl()) {
            StructType st = parseStructType();
            List<VarDecl> vds = new ArrayList<>();
    
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
            // initialize bt to default to VOID
            BaseType bt = BaseType.VOID;
            if (t != null) {
                switch (t.tokenClass) {
                    case INT:  bt = BaseType.INT; break;
                    case CHAR: bt = BaseType.CHAR; break;
                    case VOID: bt = BaseType.VOID; break;
                    default:
                        break;
                }
                if(parseReference()) {
                    return new PointerType(bt); 
                }
            } 
            return bt;
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
        Token tok = expect(TokenClass.IDENTIFIER);
        
        // bad identifier
        String name = "-- invalid variable identifier --";
        if (tok != null) {
            name = tok.data;
        }
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
        int i = -1;
        if (accept(TokenClass.LSBR)) {
            nextToken();
            Token t = expect(TokenClass.INT_LITERAL);
            if (t != null) {
                try {
                    i = Integer.parseInt(t.data);
                } catch (Exception e) {
                    // oopsie
                }
            }
            expect(TokenClass.RSBR);
        }
        // no array declaration
        return i;
    }

    private List<FunDecl> parseGlobalFunDecls() {
        List<FunDecl> fds = new ArrayList<>();
        if (acceptsType() && isFunDecl()) {
            fds.addAll(parseFunDecls());
        }
        return fds;
    }

    private FunDecl parseFunDecl() {
        Type t = parseType();
        String name = expect(TokenClass.IDENTIFIER).data;
        expect(TokenClass.LPAR);
        List<VarDecl> params = parseParams();
        expect(TokenClass.RPAR);
        Block b = parseBlock();
        return new FunDecl(t, name, params, b);
    }

    private List<FunDecl> parseFunDecls() {
        List<FunDecl> fds = new ArrayList<>();
        if (acceptsType() && isFunDecl()) {
            fds.add(parseFunDecl());
            fds.addAll(parseFunDecls());
        }
        return fds;
    }
    
    private List<VarDecl> parseParams() {
        List<VarDecl> params = new ArrayList<>();
        if (acceptsType()) {
            Type t = parseType();
            String name = expect(TokenClass.IDENTIFIER).data;
            params.add(new VarDecl(t, name));
            if (accept(TokenClass.COMMA)) {
                nextToken();
                params.addAll(parseParams());
            }
        }
        return params;
    }

    private Block parseBlock() {
        List<VarDecl> vds = new ArrayList<>();
        List<Stmt> stmts = new ArrayList<>();
        expect(TokenClass.LBRA);
        vds.addAll(parseVarDecls());
        stmts.addAll(parseStmts());
        expect(TokenClass.RBRA);
        return new Block(vds, stmts);
    }

    private List<Stmt> parseStmts() {
        List<Stmt> stmts = new ArrayList<>();
        // if next token not a closing brace, still in block [also check if EOF - prevent infinite recursion]
        if (!accept(TokenClass.RBRA, TokenClass.EOF)) {
            stmts.add(parseStmt());
            stmts.addAll(parseStmts());
        }
        return stmts;
    }

    private Stmt parseElseStmt() {
        if (accept(TokenClass.ELSE)) {
            nextToken();
            return parseStmt();
        }
        return null;
    }

    private Stmt parseStmt() {
        Stmt stmt;
        if (accept(TokenClass.LBRA)) {
            stmt = parseBlock();
        } 
        else if (accept(TokenClass.WHILE)) {
            nextToken();
            expect(TokenClass.LPAR);
            Expr e = parseExp();
            expect(TokenClass.RPAR);
            Stmt s = parseStmt();
            stmt = new While(e, s);
        } 
        else if (accept(TokenClass.IF)) {
            nextToken();
            expect(TokenClass.LPAR);
            Expr e = parseExp();
            expect(TokenClass.RPAR);
            Stmt s = parseStmt();
            stmt = new If(e, s, parseElseStmt());
        } 
        else if (accept(TokenClass.RETURN)) {
            nextToken();
            Expr e = parseOptExp();
            expect(TokenClass.SC);
            stmt = new Return(e);
        } 
        else {
            Expr e = parseExp();
            if (accept(TokenClass.ASSIGN)) {
                nextToken();
                Expr right = parseExp();
                stmt = new Assign(e, right);
            } else {
                stmt = new ExprStmt(e);
            }
            expect(TokenClass.SC);
        }
        return stmt;
    }

    private Expr parseOptExp() {
        if (!accept(TokenClass.SC)) {
            return parseExp();
        }
        return null;
    }

    private Expr parseExp() {
        return parseExp8();
    }

    private Expr parseExp8() {
        Expr e = parseExp7();

        if (accept(TokenClass.OR)) {
            nextToken();
            Expr right = parseExp8();

            e = new BinOp(e, Op.OR, right);
        }

        return e;
    }
    
    private Expr parseExp7() {
        Expr e = parseExp6();

        if (accept(TokenClass.AND)) {
            nextToken();
            Expr right = parseExp7();

            e = new BinOp(e, Op.AND, right);
        }

        return e;
    }

    private Expr parseExp6() {
        Expr e = parseExp5();
        TokenClass opClass = token.tokenClass;

        if (accept(
            TokenClass.EQ,
            TokenClass.NE
        )) {
            nextToken();
            Expr right = parseExp6();
            Op op = null;

            switch (opClass) {
                case EQ: op = Op.EQ; break;
                case NE: op = Op.NE; break;

                default:
                    break;
            }
            
            e = new BinOp(e, op, right);
        }
        return e;
    }

    private Expr parseExp5() {
        Expr e = parseExp4();
        TokenClass opClass = token.tokenClass;

        if (accept(
            TokenClass.LT,
            TokenClass.LE,
            TokenClass.GT,
            TokenClass.GE
        )) {
            nextToken();
            Expr right = parseExp5();
            Op op = null;

            switch (opClass) {
                case LT: op = Op.LT; break;
                case LE: op = Op.LE; break;
                case GT: op = Op.GT; break;
                case GE: op = Op.GE; break;
            
                default:
                    break;
            }

            e = new BinOp(e, op, right);
        }
        return e;
    }

    private Expr parseExp4() {
        Expr e = parseExp3();
        TokenClass opClass = token.tokenClass;

        if (accept(
            TokenClass.PLUS,
            TokenClass.MINUS
        )) {
            nextToken();
            Expr right = parseExp4();
            Op op = null;

            switch (opClass) {
                case PLUS : op = Op.ADD; break;
                case MINUS: op = Op.SUB; break;

                default:
                    break;
            }

            e = new BinOp(e, op, right);
        }
        return e;
    }

    private Expr parseExp3() {
        Expr e = parseExp2();
        
        TokenClass opClass = token.tokenClass;
        if (accept(
            TokenClass.ASTERIX,
            TokenClass.DIV,
            TokenClass.REM
        )) {
            nextToken();
            Expr right = parseExp3();
            Op op = null;
            switch (opClass) {
                case ASTERIX: op = Op.MUL; break;
                case DIV    : op = Op.DIV; break;
                case REM    : op = Op.MOD; break;
                default:
                    break;
            }
            e = new BinOp(e, op, right);
        }
        return e;
    }

    private Type parseTypecast() {
        expect(TokenClass.LPAR);
        Type t = parseType();
        expect(TokenClass.RPAR);
        return t;
    }

    private Expr parseExp2() {
        Expr e;
        if (accept(TokenClass.ASTERIX)) {
            nextToken();
            e = new ValueAtExpr(parseExp2());
        }
        else if (accept(TokenClass.MINUS)){
            nextToken();
            e = BinOp.negative(parseExp2());
        }
        else if (accept(TokenClass.SIZEOF)) {
            nextToken();
            expect(TokenClass.LPAR);
            Type t = parseType();
            expect(TokenClass.RPAR);
            e = new SizeOfExpr(t);
        }
        else if (
            accept(TokenClass.LPAR) && 
            acceptWithLookahead(1, 
                TokenClass.INT,
                TokenClass.CHAR,
                TokenClass.VOID,
                TokenClass.STRUCT
            )) { 
                Type t = parseTypecast();
                Expr tce = parseExp2();
                e = new TypecastExpr(t, tce);
            }
        else {
            e = parseExp1();
        }
        return e;
    }

    private Expr parseExp1() {
        Expr e;
        TokenClass la = lookAhead(1).tokenClass;
        // check for function calls
        if (accept(TokenClass.IDENTIFIER) && la == TokenClass.LPAR) {
            e = parseFunCall();
        } else {
            e = parseExp0();
        }

        while (accept(TokenClass.LSBR, TokenClass.DOT)) {
            if (accept(TokenClass.LSBR)) {
                e = new ArrayAccessExpr(e, parseArrayAccess());
            } else if (accept(TokenClass.DOT)) {
                e = new FieldAccessExpr(e, parseFieldAccess());
            }
        }

        return e;
    }

    private String parseFieldAccess() {
        expect(TokenClass.DOT);
        String field = "-- invalid field access identifier --";
        Token t = expect(TokenClass.IDENTIFIER);
        if (t != null) {
            field = t.data;
        }
        return field;
    }

    private Expr parseArrayAccess() {
        expect(TokenClass.LSBR);
        Expr e = parseExp();
        expect(TokenClass.RSBR);
        return e;
    }

    private Expr parseExp0() {
        Expr e = null;
        if (accept(TokenClass.LPAR)) {
            nextToken();
            e = parseExp();
            expect(TokenClass.RPAR);
        } else {
            Token t = expect(
                TokenClass.IDENTIFIER,
                TokenClass.INT_LITERAL,
                TokenClass.STRING_LITERAL,
                TokenClass.CHAR_LITERAL
            );
            if (t != null) {
                switch (t.tokenClass) {
                    case IDENTIFIER    : e = new VarExpr(t.data); break;
                    case INT_LITERAL   : e = IntLiteral.fromString(t.data); break;
                    case STRING_LITERAL: e = new StrLiteral(t.data); break;
                    case CHAR_LITERAL  : e = ChrLiteral.fromString(t.data); break;
                    default: break;
                }
            }
        }

        return e;
    }

    private FunCallExpr parseFunCall() {
        String name = "-- invalid function name --";
        List<Expr> args = new ArrayList<>();

        Token t = expect(TokenClass.IDENTIFIER);
        if (t != null) {
            name = t.data;
        }

        expect(TokenClass.LPAR);
        if (!accept(TokenClass.RPAR)) {
            args.addAll(parseOptExpPlus());
        }
        expect(TokenClass.RPAR);
        return new FunCallExpr(name, args);
    }
    
    private List<Expr> parseOptExpPlus() {
        List<Expr> exprs = new ArrayList<>();
        exprs.add(parseExp());
        exprs.addAll(parseExpPlus());
        return exprs;
    }

    private List<Expr> parseExpPlus() {
        List<Expr> exprs = new ArrayList<>();
        if (accept(TokenClass.COMMA)) {
            nextToken();
            exprs.add(parseExp());
            exprs.addAll(parseExpPlus());
        }
        return exprs;
    }
}
