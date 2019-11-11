package ast;

public class While extends Stmt {

    public Expr cond;
    public final Stmt stmt;

    public While(Expr cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitWhile(this);
    }

    @Override
    public String toString() {
        return String.format("while (%s)", cond);
    }

}
