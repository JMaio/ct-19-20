package ast;

import java.util.List;

public class FunDecl implements ASTNode {

    public final Type type;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;

    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
        this.type = type;
        this.name = name;
        this.params = params;
        this.block = block;
    }

    public FunDecl(Type type, String name, List<VarDecl> params) {
        this(type, name, params, new Block());
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunDecl(this);
    }

    public boolean hasParam(String p) {
        for (VarDecl vd : params) {
            if (vd.name.equals(p)) { return true; };
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("def: %s %s", type, name);
    }
}
