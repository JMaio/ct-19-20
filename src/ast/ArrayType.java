package ast;

public class ArrayType implements Type {

    public final Type t;
    public final int size;

    public ArrayType(Type t, int size) {
        this.t = t;
        this.size = size;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

}
