package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }
    
    public final String defaultDelimiter = ",";
    
    public Void visitProgram(Program p) {
        try {
            writer.print(p.getClass().getSimpleName() + "(");
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
        } catch (Exception e) {
            //TODO: handle exception
        }
        writer.flush();
        return null;
    }
    

    public Void visitBaseType(BaseType bt) {
        writer.write(bt.name());
        return null;
    }

    public Void visitPointerType(PointerType pt) {
        writer.write(pt.getClass().getSimpleName() + "(");

        pt.t.accept(this);

        writer.write(")");
        return null;
    }

    public Void visitStructType(StructType st) {
        writer.print(st.getClass().getSimpleName() + "(");

        writer.print(st.structType);

        writer.print(")");
        return null;
    }

    public Void visitArrayType(ArrayType at) {
        writer.print(at.getClass().getSimpleName() + "(");
        
        at.t.accept(this);
        writer.print(defaultDelimiter);

        writer.print(at.size);
        writer.print(")");
        return null;
    }


    public Void visitStructTypeDecl(StructTypeDecl std) {
        writer.print(std.getClass().getSimpleName() + "(");
        
        std.st.accept(this);
        
        for (VarDecl vd : std.vds) {
            writer.print(defaultDelimiter);
            vd.accept(this);
        }
        
        writer.print(")");
        return null;
    }


    public Void visitVarDecl(VarDecl vd) {
        writer.print(vd.getClass().getSimpleName() + "(");
        
        vd.type.accept(this);
        writer.print(defaultDelimiter);

        writer.print(vd.name);
        writer.print(")");
        return null;
    }

    public Void visitFunDecl(FunDecl fd) {
        writer.print(fd.getClass().getSimpleName() + "(");
        
        fd.type.accept(this);
        writer.print(defaultDelimiter);

        writer.print(fd.name);
        writer.print(defaultDelimiter);
        
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(defaultDelimiter);
        }

        fd.block.accept(this);
        writer.print(")");
        return null;
    }



    public Void visitIntLiteral(IntLiteral i) {
        writer.print(i.getClass().getSimpleName() + "(");
        writer.print(i.value);
        writer.print(")");
        return null;
    }

    public Void visitStrLiteral(StrLiteral s) {
        writer.print(s.getClass().getSimpleName() + "(");
        writer.print(s.value);
        writer.print(")");
        return null;
    }

    public Void visitChrLiteral(ChrLiteral c) {
        writer.print(c.getClass().getSimpleName() + "(");
        writer.print(c.value);
        writer.print(")");
        return null;
    }


    public Void visitVarExpr(VarExpr v) {
        writer.print(v.getClass().getSimpleName() + "(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    
    public Void visitFunCallExpr(FunCallExpr fce) {
        writer.print(fce.getClass().getSimpleName() + "(");
        writer.print(fce.name);
        
        for (Expr e : fce.args) {
            writer.print(defaultDelimiter);
            e.accept(this);
        }

        writer.print(")");
        return null;
    }

    
    public Void visitBinOp(BinOp bo) {
        writer.print(bo.getClass().getSimpleName() + "(");
        bo.left.accept(this);
        writer.print(defaultDelimiter);

        bo.op.accept(this);
        writer.print(defaultDelimiter);

        bo.right.accept(this);
        writer.print(")");
        return null;
    }

    public Void visitOp(Op o) {
        writer.print(o.name());
        return null;
    }



    public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
        writer.print(aae.getClass().getSimpleName() + "(");

        aae.array.accept(this);
        writer.print(defaultDelimiter);
        
        aae.index.accept(this);
        writer.print(")");
        return null;
    }

    public Void visitFieldAccessExpr(FieldAccessExpr fae) {
        writer.print(fae.getClass().getSimpleName() + "(");
        
        fae.struct.accept(this);
        writer.print(defaultDelimiter);
        
        writer.print(fae.field);
        writer.print(")");
        return null;
    }

    public Void visitValueAtExpr(ValueAtExpr vae) {
        writer.print(vae.getClass().getSimpleName() + "(");
        vae.expr.accept(this);
        writer.print(")");
        return null;
    }

    public Void visitSizeOfExpr(SizeOfExpr soe) {
        writer.print(soe.getClass().getSimpleName() + "(");
        soe.t.accept(this);
        writer.print(")");
        return null;
    }


    public Void visitTypecastExpr(TypecastExpr te) {
        writer.print(te.getClass().getSimpleName() + "(");

        te.t.accept(this);
        writer.print(defaultDelimiter);

        te.expr.accept(this);
        writer.print(")");
        return null;
    }



    public Void visitExprStmt(ExprStmt es) {
        writer.print(es.getClass().getSimpleName() + "(");
        es.expr.accept(this);
        writer.print(")");
        return null;
    }


    public Void visitWhile(While w) {
        writer.print(w.getClass().getSimpleName() + "(");

        w.expr.accept(this);
        writer.print(defaultDelimiter);

        w.stmt.accept(this);
        writer.print(")");
        return null;
    }


    public Void visitIf(If i) {
        writer.print(i.getClass().getSimpleName() + "(");

        i.cond.accept(this);
        writer.print(defaultDelimiter);

        i.stmt.accept(this);

        if (i.elseStmt != null) {
            writer.print(defaultDelimiter);
            i.elseStmt.accept(this);
        }

        writer.print(")");
        return null;
    }


    public Void visitAssign(Assign a) {
        writer.print(a.getClass().getSimpleName() + "(");

        a.left.accept(this);
        writer.print(defaultDelimiter);

        a.right.accept(this);
        writer.print(")");
        return null;
    }


    public Void visitReturn(Return r) {
        writer.print(r.getClass().getSimpleName() + "(");
        if (r.expr != null) {
            r.expr.accept(this);
        }
        writer.print(")");
        return null;
    }

    
    public Void visitBlock(Block b) {
        writer.print(b.getClass().getSimpleName() + "(");
        
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
