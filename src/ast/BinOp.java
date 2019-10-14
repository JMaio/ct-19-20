package ast;

public class BinOp implements ASTNode {

    public final Expr left;
    public final Op op;
    public final Expr right;

    public BinOp(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> v) {
        // TODO Auto-generated method stub
        return null;
    }

}
