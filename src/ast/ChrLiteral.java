package ast;

public class ChrLiteral extends Expr {

    public final char value; 

    public ChrLiteral(char c) {
        this.value = c;
    }
    
    public static ChrLiteral fromString(String s) {
        // assert s.length() == 1;
        ChrLiteral c = new ChrLiteral('0');
        try {
            c = new ChrLiteral(s.toCharArray()[0]);    
        } catch (Exception e) {
            //TODO: handle exception
        }
        return c;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

}
