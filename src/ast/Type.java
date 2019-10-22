package ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);

    // https://dzone.com/articles/interface-default-methods-java
    default public boolean isBaseType() { return false; }
    default public boolean isStructType() { return false; }
    default public boolean isArrayType() { return false; }
    default public boolean isPointerType() { return false; }

    public static boolean isBaseType(Type t) {
        try { return t.isBaseType(); }
        catch (Exception e) { return false; }
    }

    public static boolean isStructType(Type t) {
        try { return t.isStructType(); }
        catch (Exception e) { return false; }
    }

    public static boolean isArrayType(Type t) {
        try { return t.isArrayType(); }
        catch (Exception e) { return false; }
    }

    public static boolean isPointerType(Type t) {
        try { return t.isPointerType(); }
        catch (Exception e) { return false; }
    }

    public static Type getElemType(Type upper) {
        Type t = null;
        try {
            if (upper.isArrayType()) {
                t = ((ArrayType) upper).t;
            } else if (upper.isPointerType()) {
                t = ((PointerType) upper).t;
            }
        } catch (Exception e) {
            // null type? bad cast? ...
        }
        return t;
    }

    public static boolean areTypesEqual(Type self, Type other) {
        boolean equal = false;
        try {
            if (self.isBaseType() && other.isBaseType()) {
                equal = self == other;
            } else if (
                self.isArrayType()   && other.isArrayType() ||
                self.isPointerType() && other.isPointerType()
            ) {
                equal = Type.areTypesEqual(Type.getElemType(self), Type.getElemType(other));
            }
            else if (self.isStructType() && other.isStructType()) {
                equal = ((StructType) self).structType.equals(((StructType) other).structType);
            }
        } catch (Exception e) {}
        
        return equal;
    }

}
