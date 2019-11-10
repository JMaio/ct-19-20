package ast;

public class ArrayType implements Type {

    public final Type t;
    public final int size;

    public ArrayType(Type t, int size) {
        this.t = t;
        this.size = size;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

    public boolean isArrayType() { return true; }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getName(), t) ;
    }

    @Override
    public int size() {
        // array types should never be in sizeof, 
        // but can be in structs!
        return this.size * this.t.size();
    }

}
