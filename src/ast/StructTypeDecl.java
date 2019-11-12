package ast;

import java.util.HashMap;
import java.util.List;

public class StructTypeDecl implements ASTNode {
    public final StructType st;
    public final List<VarDecl> vds;
    public HashMap<String, Integer> structOffset = new HashMap<>();
    public int structSize;

    public StructTypeDecl(StructType st, List<VarDecl> vds) {
        this.st = st;
        this.vds = vds;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

    public boolean hasField(String f) {
        for (VarDecl vd : vds) {
            if (vd.name.equals(f)) { return true; }
        }
        return false;
    }

    public Type getFieldType(String f) {
        for (VarDecl vd : vds) {
            if (vd.name.equals(f)) { return vd.type; }
        }
        return null;
    }

    public void populateOffsets() {
        // should only be called after types are validated
        int s = 0;        
        for (VarDecl vd : vds) {
            int inner = vd.type.size();
            // inner = 5, 5%4 = 1 ==> add [4 - (n%4)] % 4
            // pad to align to 4 byte boundary
            s += Type.alignTo4Byte(inner);
            structOffset.put(vd.name, s);
        }
        structSize = s;
    }


}
