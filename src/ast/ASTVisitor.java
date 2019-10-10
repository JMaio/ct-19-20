package ast;

public interface ASTVisitor<T> {
    
    public T visitProgram(Program p);
    


    // public T visitType(Type t);
    
    public T visitBaseType(BaseType bt);
    // public T visitPointerType(PointerType pt);
    // public T visitStructType(StructType st);
    // public T visitArrayType(ArrayType at);
    
    public T visitStructTypeDecl(StructTypeDecl std);
    
    public T visitVarDecl(VarDecl vd);
    
    public T visitFunDecl(FunDecl fd);
    


    // public T visitExpr(Expr e);

    public T visitIntLiteral(IntLiteral i);
    public T visitStrLiteral(StrLiteral s);
    public T visitChrLiteral(ChrLiteral c);
    
    public T visitVarExpr(VarExpr v);

    // public T visitFunCallExpr(FunCallExpr fce);

    // public T visitBinOp(BinOp bo);
    // public T visitOp(Op o);

    // public T visitArrayAccessExpr(ArrayAccessExpr aae);

    // public T visitFieldAccessExpr(FieldAccessExpr fae);

    // public T visitValueAtExpr(ValueAtExpr vae);

    // public T visitSizeOfExpr(SizeOfExpr soe);

    // public T visitTypecastExpr(TypecastExpr te);



    // public T visitStmt(Stmt s);

    // public T visitExprStmt(ExprStmt es);
    

    
    public T visitBlock(Block b);


    // to complete ... (should have one visit method for each concrete AST node class)
}
