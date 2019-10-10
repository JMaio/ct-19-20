package ast;

public class IntLiteral implements ASTNode {
    public final int i;
    
    public IntLiteral(int i) {
        this.i = i;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }

}
