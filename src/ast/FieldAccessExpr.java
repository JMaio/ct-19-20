package ast;

public class FieldAccessExpr extends Expr {

    public final Expr struct;
    public final String field;
    public int totalOffset = 0;

    public FieldAccessExpr(Expr struct, String field) {
        this.struct = struct;
        this.field = field;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }

    @Override
    public boolean isFieldAccessExpr() { return true; }

    @Override
    public String toString() {
        return String.format("%s.%s", struct, field);
    }

    @Override
    public Expr getInnermost() {
        return struct.getInnermost();
    }

    @Override
    public Expr getInner() {
        return struct;
    }
}
