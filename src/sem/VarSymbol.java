package sem;

import ast.VarDecl;

public class VarSymbol extends Symbol {

    public final VarDecl vd;

    public VarSymbol(VarDecl vd) {
        super(vd.name);
        this.vd = vd;
    }
    
    @Override
    public boolean isVar() { return true; }

}