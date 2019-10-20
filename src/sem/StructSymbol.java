package sem;

import ast.StructTypeDecl;

public class StructSymbol extends Symbol {

    public StructTypeDecl std;

    public StructSymbol(StructTypeDecl std) {
        super(std.st.structType);
        this.std = std;
    }

    @Override
    public boolean isStruct() { return true; }
}