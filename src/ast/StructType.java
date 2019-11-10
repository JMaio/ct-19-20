package ast;

public class StructType implements Type {
    
    public final String structType;
    public StructTypeDecl std;

    public StructType(String structType) {
        this.structType = structType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    public boolean isStructType() { return true; }

    @Override
    public String toString() {
        return String.format("%s", getClass().getName());
    }

    @Override
    public int size() {
        int s = 0;
        for (VarDecl vd : std.vds) {
            s += vd.type.size();
        }
        return s;
    }
    
}