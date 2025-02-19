package ast;

public class ValueAtExpr extends Expr {

    public final Expr expr;

    public ValueAtExpr(Expr expr) {
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitValueAtExpr(this);
    }

    @Override
    public boolean isValueatExpr() { return true; }

    @Override
    public String toString() {
        return String.format("(*%s)", expr);
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
