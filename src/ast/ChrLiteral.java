package ast;

public class ChrLiteral implements ASTNode {

    public final char value; 

    public ChrLiteral(char c) {
        this.value = c;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

}
