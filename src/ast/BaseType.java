package ast;

public enum BaseType implements Type {
    INT, CHAR, VOID;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }

    @Override
    public String toString() {
        return String.format("%s", name());
    }

    @Override
    public boolean isBaseType() { return true; }

    @Override
    public int size() {
        switch (this) {
            case INT : return 4;
            case CHAR: return 1;
            case VOID: return 0;
        }
        return 0;
    }
}
