package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {
    public final StructType st;
    public final List<VarDecl> vds;

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

}
