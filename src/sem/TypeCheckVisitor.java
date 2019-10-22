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
		// returns!
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
		// v.vd can be null if not picked up by name visitor
		if (v.isVarExpr() && v.vd != null) { 
			v.type = v.vd.type;
		}
		return v.type;
	}

	public Type visitFunCallExpr(FunCallExpr fce) {
		try {
			// params *must* be in the same order! - so no need to worry
			int len = Math.max(fce.fd.params.size(), fce.args.size());
			for (int p = 0; p < len; p++) {
				// set type of this expression
				VarDecl param = fce.fd.params.get(p);
				Expr arg = fce.args.get(p);

				Type paramType = param.type;
				Type argType = arg.accept(this);

				// System.out.println(String.format("expected %s - got %s", paramType, argType));

				if (!Type.areTypesEqual(paramType, argType)) {
					error(String.format(
						"bad function argument passed to '%s': '%s' (type: %s) [expected '%s' (type: %s)]", 
						fce.name, 
						arg, arg.type,
						param.name, param.type));
				}
			}
			fce.type = fce.fd.type;

		} catch (Exception e) {
			/* ran out of args? */
			error("bad function call '" + fce.name + "()' -- wrong number of arguments?");
		}

		return fce.type;
	}

	public Type visitBinOp(BinOp bo) {
		Type l = bo.left.accept(this);
		Op o = bo.op;
		Type r = bo.right.accept(this);

		// System.out.println(l + " " + o + " " + r);
		if (!Type.areTypesEqual(l, r)) {
			error(String.format("incompatible types for comparison: '%s' , '%s'", 
				bo.left.type, bo.right.type));
		} else {
			if (o == Op.EQ || o == Op.NE) {
				if (!(l.isStructType() || l.isArrayType() || l == BaseType.VOID)) {
					// good!
				} else {
					error(String.format("bad types for comparison: '%s' %s '%s'", 
						bo.left.type, bo.op, bo.right.type));
				}
			} else {
				if (l == BaseType.INT) {
					// good!
				} else {
					error(String.format("cannot compare non-int types: '%s' %s '%s'", 
						bo.left.type, bo.op, bo.right.type));
				}
			}
		}

		return BaseType.INT;
	}

	public Type visitOp(Op o) {
		return null;
	}

	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		Type a = aae.array.accept(this);
		Type i = aae.index.accept(this);
		if (!(Type.isArrayType(a) || Type.isPointerType(a)) || i != BaseType.INT) {
			error(String.format("bad array access %s[%s]", a, i));
		} else {
			aae.type = Type.getElemType(a);
		}
		return aae.type;
	}

	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		return fae.type;
	}

	public Type visitValueAtExpr(ValueAtExpr vae) {
		Type et = vae.expr.accept(this);
		// should be pointer type
		if (et != null && et.isPointerType()) {
			// return the inner type (access the pointer)
			vae.type = et;
			return Type.getElemType(vae.type);
		}
		return null;
	}

	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return BaseType.INT;
	}

	public Type visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
		Type et = te.expr.accept(this);
		try {
			if (et == BaseType.CHAR && te.t == BaseType.INT) {
				// char -> int
			} else if (et.isArrayType() && te.t.isPointerType() && 
						Type.areTypesEqual(Type.getElemType(et), Type.getElemType(te.t))) {
				// array -> pointer
			} else if (et.isPointerType() && te.t.isPointerType() && 
						!Type.areTypesEqual(Type.getElemType(et), Type.getElemType(te.t))) {
				// pointer -> pointer
			} else {
				// et phone home
				// throw new Exception(String.format("illegal typecast expression [(%s) -/-> (%s)]", te.t, et));
				error(String.format("illegal typecast expression [(%s) -/-> (%s)]", et, te.t));
				return null;
			}
		} catch (Exception e) {
			error(String.format("error in typecast expression [(%s) -/-> (%s)]", et, te.t));
			return null;
		}

		return te.t;
	}

	public Type visitExprStmt(ExprStmt es) {
		es.expr.type = es.expr.accept(this);
		return es.expr.type;
	}

	public Type visitWhile(While w) {
		if (w.cond.accept(this) != BaseType.INT) {
			error("non-integer condition in while statement");
		}
		return null;
	}

	public Type visitIf(If i) {
		if (i.cond.accept(this) != BaseType.INT) {
			error("non-integer condition in if statement");
		}
		return null;
	}

	public Type visitAssign(Assign a) {
		Type l = a.left.accept(this);
		Type r = a.right.accept(this);
		try {
			if (
				(l == BaseType.VOID || l.isArrayType()) || 
				(r == BaseType.VOID || r.isArrayType())) {
				error("illegal type in assignment: " + l + " = " + r);
			} else if (!Type.areTypesEqual(l, r)) {
				error("incompatible types in assignment: " + l + " = " + r);
			}
		} catch (Exception e) {	
			error("illegal type in assignment: " + l + " = " + r);
		}
		return null;
	}

	public Type visitReturn(Return r) {		
		Type t = BaseType.VOID;
		if (r.expr != null) {
			// returns a typed expression
			t = r.expr.accept(this);
		}
		if (r.funReturnType != null && Type.areTypesEqual(r.funReturnType, t)) {
			// good return
		} else {
			error(String.format("bad return: %s should return '%s' but returns '%s'", r.fd.name, r.funReturnType, t));
		}
		return t;
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
