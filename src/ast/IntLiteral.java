package ast;

public class IntLiteral extends Expr {
    
    public final int value;
    
    public IntLiteral(int i) {
        this.value = i;
    }
    
    public IntLiteral(String s) {
        this(Integer.parseInt(s));
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }

}
