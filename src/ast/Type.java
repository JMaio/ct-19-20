package ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);

    // https://dzone.com/articles/interface-default-methods-java
    default public boolean isStructType() { return false; }
    default public boolean isArrayType() { return false; }

}
