package ast;

public class IntLiteral extends Expr {
    
    public final int value;
    
    public IntLiteral(int i) {
        this.value = i;
    }
    
    public static IntLiteral fromString(String s) {
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {
            // bad integer
            i = -1;
        }
        return new IntLiteral(i);
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }

    public boolean isIntLiteral() { return true; }

}
