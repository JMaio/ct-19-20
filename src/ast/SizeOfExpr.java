package ast;

public class SizeOfExpr extends Expr {

    public final Type t;

    public SizeOfExpr(Type t) {
        this.t = t;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitSizeOfExpr(this);
    }

    @Override
    public String toString() {
        return String.format("sizeof(%s)", t);
    }
}
