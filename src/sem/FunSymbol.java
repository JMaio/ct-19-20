package sem;

import ast.FunDecl;

public class FunSymbol extends Symbol {

    public final FunDecl fd;

    public FunSymbol(FunDecl fd) {
        super(fd.name);
        this.fd = fd;
    }

    @Override
    public boolean isFun() { return true; }

}