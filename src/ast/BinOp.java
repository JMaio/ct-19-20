package ast;

public class BinOp extends Expr {

    public final Expr left;
    public final Op op;
    public final Expr right;

    public BinOp(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    // factory for negative expressions
    public static BinOp negative(Expr e) {
        return new BinOp(new IntLiteral(0), Op.SUB, e);
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }

}
