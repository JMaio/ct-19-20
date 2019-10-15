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

}
