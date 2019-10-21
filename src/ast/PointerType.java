package ast;

public class PointerType implements Type {

    public final Type t;

    public PointerType(Type t) {
        this.t = t;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), t);
    }
}
