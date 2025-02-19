package ast;

public class ArrayAccessExpr extends Expr {

    public final Expr array;
    public final Expr index;

    public ArrayAccessExpr(Expr array, Expr index) {
        this.array = array;
        this.index = index;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }

    @Override
    public boolean isArrayAccessExpr() { return true; }

    @Override
    public Expr getInnermost() {
        return array.getInnermost();
    }

    @Override
    public Expr getInner() {
        return array;
    }
}
