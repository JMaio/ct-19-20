package ast;

public abstract class Expr implements ASTNode {

    public Type type; // to be filled in by the type analyser
    public abstract <T> T accept(ASTVisitor<T> v);

    public boolean isVarExpr() { return false; }
    public boolean isFieldAccessExpr() { return false; }
    public boolean isArrayAccessExpr() { return false; }

    public boolean isIntLiteral() { return false; }
    public boolean isChrLiteral() { return false; }
}
