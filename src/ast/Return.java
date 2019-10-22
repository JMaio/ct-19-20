package ast;

public class Return extends Stmt {

    public final Expr expr;
    public FunDecl fd;
    public Type funReturnType;

    public Return() {
        this(null);
    }

    public Return(Expr expr) {
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }

}
