package ast;

import java.util.HashMap;

public class StructType implements Type {
    
    public final String structType;
    public StructTypeDecl std;
    public HashMap<String, Integer> structOffset = new HashMap<>();

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
            int inner = vd.type.size();
            // inner = 5, 5%4 = 1 ==> add [4 - (n%4)] % 4
            // pad to align to 4 byte boundary
            s += Type.alignTo4Byte(inner);
            structOffset.put(vd.name, s);
        }
        return s;
    }

    // public int getOffset(String varname) {
    //     int offset = 0;

    // }
    
}