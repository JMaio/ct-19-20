package ast;

import java.util.ArrayList;
import java.util.List;

public class Block extends Stmt {

    public final List<VarDecl> vds;
    public final List<Stmt> stmts;

    public Block(List<VarDecl> vds, List<Stmt> stmts) {
        this.vds = vds;
        this.stmts = stmts;
    }

    public Block() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
    
}
