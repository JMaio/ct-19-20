package ast;

public class StructType implements ASTNode {
    public final String structType;

    public StructType(String structType) {
        this.structType = structType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return null;
    }
    
}