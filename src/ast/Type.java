package ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);

    // https://dzone.com/articles/interface-default-methods-java
    default public boolean isStructType() { return false; }
    default public boolean isArrayType() { return false; }
    default public boolean isPointerType() { return false; }

    default public Type getElemType() {
        Type t = null;
        if (isArrayType()) {
            t = ((ArrayType) this).t;
        } else if (isPointerType()) {
            t = ((PointerType) this).t;
        }
        return t;
    }

}
