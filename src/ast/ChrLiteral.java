package ast;

public class ChrLiteral extends Expr {

    public final char value; 

    public ChrLiteral(char c) {
        this.value = c;
    }
    
    public static ChrLiteral fromString(String s) {
        // assert s.length() == 1;
        char c = '\0';
        if (s.length() == 1) { c = s.charAt(0); }
        try {
            c = s.charAt(0);    
        } catch (Exception e) {
            //TODO: handle exception
        }
        return new ChrLiteral(c);
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

    @Override
    public boolean isChrLiteral() { return true; }

}
