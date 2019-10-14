package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }
    
    public final String defaultDelimiter = ",";
    
    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
        writer.flush();
        return null;
    }
    
    // Types --------------

    public Void visitBaseType(BaseType bt) {
        writer.write(bt.name());
        return null;
    }

    public Void visitPointerType(PointerType pt) {
        writer.write(pt.getClass().getSimpleName());
        writer.write("(");
        pt.t.accept(this);
        writer.write(")");
        return null;
    }

    public Void visitStructType(StructType st) {
        // TODO Auto-generated method stub
        return null;
    }

    public Void visitArrayType(ArrayType at) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print(st.getClass().getSimpleName() + "(");
        
        st.st.accept(this);
        
        for (VarDecl vd : st.vds) {
            writer.print(defaultDelimiter);
            vd.accept(this);
        }
        
        writer.print(")");
        return null;
    }


    public Void visitVarDecl(VarDecl vd) {
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.name);
        writer.print(")");
        return null;
    }

    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }



    public Void visitIntLiteral(IntLiteral i) {
        writer.print(i.value);
        return null;
    }

    public Void visitStrLiteral(StrLiteral s) {
        writer.print(s.value);
        return null;
    }

    public Void visitChrLiteral(ChrLiteral c) {
        writer.print(c.value);
        return null;
    }


    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    
    @Override
    public Void visitFunCallExpr(FunCallExpr fce) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public Void visitBinOp(BinOp bo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr vae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr soe) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitTypecastExpr(TypecastExpr te) {
        // TODO Auto-generated method stub
        return null;
    }



    public Void visitExprStmt(ExprStmt es) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitWhile(While w) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitIf(If i) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitAssign(Assign a) {
        // TODO Auto-generated method stub
        return null;
    }


    public Void visitReturn(Return r) {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Void visitBlock(Block b) {
        writer.print("Block(");
        String d = "";
        for (VarDecl vd : b.vds) {
            writer.print(d);
            d = defaultDelimiter;
            vd.accept(this);
        }
        
        for (Stmt stmt : b.stmts) {
            writer.print(d);
            d = defaultDelimiter;
            stmt.accept(this);
        }

        writer.print(")");
        return null;
    }

}
