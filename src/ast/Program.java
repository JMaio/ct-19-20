package ast;

import java.util.List;

public class Program implements ASTNode {

    public final List<StructTypeDecl> structTypeDecls;
    public final List<VarDecl> varDecls;
    public final List<FunDecl> funDecls;
    public FunDecl main;

    public Program(List<StructTypeDecl> structTypeDecls, List<VarDecl> varDecls, List<FunDecl> funDecls) {
        this.structTypeDecls = structTypeDecls;
        this.varDecls = varDecls;
        this.funDecls = funDecls;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitProgram(this);
    }
}
