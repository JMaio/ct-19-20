package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	public Type visitProgram(Program p) {
		for (StructTypeDecl std : p.structTypeDecls) {
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            fd.accept(this);
        }
		return null;
	}

	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	public Type visitPointerType(PointerType pt) {
		return pt;
	}

	public Type visitStructType(StructType st) {
		return st;
	}

	public Type visitArrayType(ArrayType at) {
		return at;
	}

	public Type visitStructTypeDecl(StructTypeDecl std) {
		for (VarDecl vd : std.vds) {
			vd.accept(this);
		}
		// not necessary to return the structtype, but do it anyways
		return std.st;
	}

	public Type visitVarDecl(VarDecl vd) {
		Type t = vd.type.accept(this);
		if (t == BaseType.VOID) {
			error("var '" + vd.name + "' cannot be of type void");
		}
		return t;
	}

	public Type visitFunDecl(FunDecl fd) {
		for (VarDecl vd : fd.params) {
			vd.accept(this);
		}
		fd.block.accept(this);
		return fd.type;
	}

	public Type visitIntLiteral(IntLiteral i) {
		i.type = BaseType.INT;
		return i.type;
	}

	public Type visitStrLiteral(StrLiteral s) {
		s.type = new ArrayType(BaseType.CHAR, s.value.length() + 1); // account for null terminator!
		return s.type;
	}

	public Type visitChrLiteral(ChrLiteral c) {
		c.type = BaseType.CHAR;
		return c.type;
	}

	public Type visitVarExpr(VarExpr v) {
		v.type = v.vd.type;
		return v.type;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		Type a = aae.array.accept(this);
		Type i = aae.index.accept(this);
		if (!(a.isArrayType() || a.isPointerType()) || i != BaseType.INT) {
			error(String.format("bad array access %s[%s]", a, i));
		} else {
			aae.type = a.getElemType();
		}
		return aae.type;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		return es.expr.accept(this);
	}

	@Override
	public Type visitWhile(While w) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitIf(If i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		// TODO Auto-generated method stub
		return null;
	}

	public Type visitBlock(Block b) {
		for (VarDecl vd : b.vds) {
			vd.accept(this);
		}
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		return null;
	}


}
