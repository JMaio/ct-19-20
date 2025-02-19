package ast;

public class TypecastExpr extends Expr {

    public final Type t;
    public final Expr expr;

    public TypecastExpr(Type t, Expr expr) {
        this.t = t;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecastExpr(this);
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", t, expr);
    }

    @Override
    public Expr getInnermost() {
        return expr.getInnermost();
    }

    @Override
    public Expr getInner() {
        return expr;
    }

}
