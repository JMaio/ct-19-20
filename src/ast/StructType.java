package ast;

import java.util.HashMap;

public class StructType implements Type {
    
    public final String structType;
    public StructTypeDecl std;
    public int size;

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
        if (size == 0) {
            size = std.structSize;
        }
        return size;
    }

    public int getFieldOffset(String field) {
        return std.structOffset.get(field);
    }

    public int getFieldSize(String field) {
        return std.getFieldType(field).size();
    }

    // public int getOffset(String varname) {
    //     int offset = 0;

    // }
    
}