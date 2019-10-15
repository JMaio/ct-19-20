package ast;

public class ChrLiteral extends Expr {

    public final char value; 

    public ChrLiteral(char c) {
        this.value = c;
    }
    
    public static ChrLiteral fromString(String s) {
        assert s.length() == 1;
        return new ChrLiteral(s.toCharArray()[0]);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

}
