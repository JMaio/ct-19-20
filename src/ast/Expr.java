package ast;

public abstract class Expr implements ASTNode {

    public Type type; // to be filled in by the type analyser
    public boolean isImmediate = false; // not an immediate by default
    public boolean isGlobal = false;    // not an immediate by default
    public abstract <T> T accept(ASTVisitor<T> v);

    public static boolean isVarExpr(Expr expr) {
        try { return expr.isVarExpr(); }
        catch (Exception e) { return false; }
    }

    public static boolean isFieldAccessExpr(Expr expr) {
        try { return expr.isFieldAccessExpr(); }
        catch (Exception e) { return false; }
    }

    public static boolean isArrayAccessExpr(Expr expr) {
        try { return expr.isArrayAccessExpr(); }
        catch (Exception e) { return false; }
    }
    
    public static boolean isValueatExpr(Expr expr) {
        try { return expr.isValueatExpr(); }
        catch (Exception e) { return false; }
    }

    public boolean isVarExpr() { return false; }
    public boolean isFieldAccessExpr() { return false; }
    public boolean isArrayAccessExpr() { return false; }
    public boolean isValueatExpr() { return false; }


    public static boolean isIntLiteral(Expr expr) {
        try { return expr.isIntLiteral(); }
        catch (Exception e) { return false; }
    }

    public static boolean isChrLiteral(Expr expr) {
        try { return expr.isChrLiteral(); }
        catch (Exception e) { return false; }
    }

    public boolean isIntLiteral() { return false; }
    public boolean isChrLiteral() { return false; }

    public Expr getInner() {
        return this;
    };

    public static Expr getInner(Expr expr) {
        try {
            return expr.getInner();
        } catch (Exception e) {
            return null;
        }
    }

    public Expr getInnermost() {
        return this;
    };

    public static Expr getInnermost(Expr expr) {
        try {
            return expr.getInnermost();
        } catch (Exception e) {
            return null;
        }
    }
}
