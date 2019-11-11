package ast;

public class VarDecl implements ASTNode {

    public final Type type;
    public final String name;
    public boolean global = false;
    public int offset;

    public VarDecl(Type type, String name) {
	    this.type = type;
	    this.name = name;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitVarDecl(this);
    }

    @Override
    public String toString() {
        String s = "%s %s";
        if (global) s += " (global)";
        return String.format(s, type, name);
    }
    
}
