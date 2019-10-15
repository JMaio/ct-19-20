package ast;

public class Assign extends Stmt {

    public final Expr left;
    public final Expr right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitAssign(this);
    }

}
