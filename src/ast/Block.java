package ast;

import java.util.List;

public class Block extends Stmt {

    public final List<VarDecl> vds;
    public final List<Stmt> stmts;

    public Block(List<VarDecl> vds, List<Stmt> stmts) {
        this.vds = vds.isEmpty() ? null : vds;
        this.stmts = stmts.isEmpty() ? null : stmts;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
    
}
