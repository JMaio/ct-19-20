package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {
    public final StructType st;
    public final List<VarDecl> vds;

    public StructTypeDecl(StructType st, List<VarDecl> vds) {
        this.st = st;
        this.vds = vds.isEmpty() ? null : vds;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
