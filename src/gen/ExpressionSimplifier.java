package gen;

import java.util.ArrayList;

import ast.*;


public class ExpressionSimplifier implements ASTVisitor<Expr> {

    @Override
    public Expr visitProgram(Program p) {
        for (FunDecl fd : p.funDecls) {
            fd.accept(this);
        }
        return null;
    }

    @Override
    public Expr visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Expr visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Expr visitStructType(StructType st) {
        return null;
    }

    @Override
    public Expr visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Expr visitStructTypeDecl(StructTypeDecl std) {
        return null;
    }

    @Override
    public Expr visitVarDecl(VarDecl vd) {
        return null;
    }

    @Override
    public Expr visitFunDecl(FunDecl fd) {
        fd.block.accept(this);
        return null;
    }

    @Override
    public Expr visitIntLiteral(IntLiteral i) {
        return i;
    }

    @Override
    public Expr visitStrLiteral(StrLiteral s) {
        return s;
    }

    @Override
    public Expr visitChrLiteral(ChrLiteral c) {
        return c;
    }

    @Override
    public Expr visitVarExpr(VarExpr v) {
        return v;
    }

    @Override
    public Expr visitFunCallExpr(FunCallExpr fce) {
        ArrayList<Expr> simplifiedArgs = new ArrayList<Expr>();
        for (Expr arg : fce.args) {
            Expr simplified = arg.accept(this);
            if (simplified != null) {
                simplifiedArgs.add(simplified);
            } else {
                simplifiedArgs.add(arg);
            }
        }

        fce.args = simplifiedArgs;

        return null;
    }

    @Override
    public Expr visitBinOp(BinOp bo) {
        Expr left = bo.left.accept(this);
        Expr right = bo.right.accept(this);

        Integer val = null;

        if (Expr.isIntLiteral(left) && Expr.isIntLiteral(right)) {
            int l = ((IntLiteral) left).value;
            int r = ((IntLiteral) right).value;

            switch (bo.op) {
                case ADD: val = l + r; break;
                case SUB: val = l - r; break;
                case MUL: val = l * r; break;
                // no div as no floating point literals exist here directly
                case DIV: val = l / r; break;
                case MOD: val = l % r; break;

                case GT : val = l > r ? 1 : 0; break;
                case LT : val = l < r ? 1 : 0; break;

                case GE : val = l <= r ? 1 : 0; break;
                case LE : val = l >= r ? 1 : 0; break;

                case NE : val = l != r ? 1 : 0; break;
                case EQ : val = l == r ? 1 : 0; break;

                case OR : val = (l != 0 || r != 0) ? 1 : 0; break;
                case AND: val = (l != 0 && r != 0) ? 1 : 0; break;
            
                default: break;
            }
        } else if (Expr.isChrLiteral(left) && Expr.isChrLiteral(right)) {
            int l = ((ChrLiteral) left).value;
            int r = ((ChrLiteral) right).value;

            switch (bo.op) {
                case NE : val = l != r ? 1 : 0; break;
                case EQ : val = l == r ? 1 : 0; break;
            
                default: break;
            }
        }

        if (val != null) {
            return new IntLiteral(val);
        } else {
            return null;
        }
    }

    @Override
    public Expr visitOp(Op o) {
        return null;
    }

    @Override
    public Expr visitArrayAccessExpr(ArrayAccessExpr aae) {
        aae.array.accept(this);
        aae.index.accept(this);
        return null;
    }

    @Override
    public Expr visitFieldAccessExpr(FieldAccessExpr fae) {
        fae.struct.accept(this);
        return null;
    }

    @Override
    public Expr visitValueAtExpr(ValueAtExpr vae) {
        vae.expr.accept(this);
        return null;
    }

    @Override
    public Expr visitSizeOfExpr(SizeOfExpr soe) {
        // TODO
        // Integer s = null;
        // if (Type.isBaseType(soe.t)) {
        //     switch ((BaseType) soe.t) {
        //         case CHAR : s = 1; break;
        //         case INT: s = 4; break;
        //         default: break;
        //     }
        // } else if (Type.isArrayType(soe.t)) {
        //     ArrayType at = ((ArrayType) soe.t);
        //     int innerSize = ((IntLiteral) new SizeOfExpr(at.t).accept(this)).value;
        //     s = innerSize * at.size;
        // } else if (Type.isPointerType(soe.t)) {
        //     // point to anything -> that's an address!
        //     // 32 bits = 4 bytes
        //     s = 4;
        // } else if (Type.isStructType(soe.t)) {
        //     // cannot contain struct, only pointer(struct)
        //     s = 0;
        //     for (VarDecl vd : ((StructType) soe.t).std.vds) {
        //         s += ((IntLiteral) new SizeOfExpr(vd.type).accept(this)).value;
        //     }
        // } else {
        //     // big f
        // }
        return new IntLiteral(soe.t.size());

        // if (s != null) {
        //     return new IntLiteral(s);
        // } else {
        //     return new IntLiteral(0);
        // }

    }

    @Override
    public Expr visitTypecastExpr(TypecastExpr te) {
        // TODO Auto-generated method stub
        // to simplify!
        if (te.t == BaseType.INT && Expr.isChrLiteral(te.expr)) {
            return new IntLiteral(((ChrLiteral) te.expr).value);
        }
        // else if (Type.isArrayType(te.expr.type)) {
        //     // other types? pointers / arrays?
        // }
        // if (te.t == BaseType.INT &&) {
        // }
        return null;
    }

    @Override
    public Expr visitExprStmt(ExprStmt es) {
        es.expr.accept(this);
        return null;
    }

    @Override
    public Expr visitWhile(While w) {
        w.stmt.accept(this);
        return null;
    }

    @Override
    public Expr visitIf(If i) {
        i.stmt.accept(this);
        if (i.elseStmt != null) {
            i.elseStmt.accept(this);
        }
        return null;
    }

    @Override
    public Expr visitAssign(Assign a) {
        a.left.accept(this);
        a.right.accept(this);
        return null;
    }

    @Override
    public Expr visitReturn(Return r) {
        if (r.expr != null) {
            r.expr.accept(this);
        }
        return null;
    }

    @Override
    public Expr visitBlock(Block b) {
        for (Stmt s : b.stmts) {
            s.accept(this);
        }
        return null;
    }  
    
}