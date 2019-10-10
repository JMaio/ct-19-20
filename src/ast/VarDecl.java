package ast;

public class VarDecl implements ASTNode {

    public final Type type;
    public final String name;

    public VarDecl(Type type, String name) {
	    this.type = type;
	    this.name = name;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitVarDecl(this);
    }
    
}
