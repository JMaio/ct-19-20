package ast;

public class If extends Stmt {

    public Expr cond;
    public final Stmt stmt;
    public final Stmt elseStmt;

    public If(Expr expr, Stmt stmt) {
        this(expr, stmt, null);
    }

    public If(Expr expr, Stmt stmt, Stmt elseStmt) {
        this.cond = expr;
        this.stmt = stmt;
        this.elseStmt = elseStmt;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }

    @Override
    public String toString() {
        return String.format("if (%s)", cond);
    }
}
