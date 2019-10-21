package sem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	public FunSymbol funSymFromDecl(Type t, String name, List<VarDecl> params) {
		return new FunSymbol(new FunDecl(t, name, params));
	}
	public FunSymbol funSymFromDecl(Type t, String name) {
		return funSymFromDecl(t, name, new ArrayList<>());
	}

	// dummy library functions
	private Scope globalScope = new Scope() {{
		put(funSymFromDecl(BaseType.VOID, "print_s", new ArrayList<VarDecl>() {{ new VarDecl(new PointerType(BaseType.CHAR), "s"); }}));
		put(funSymFromDecl(BaseType.VOID, "print_i", new ArrayList<VarDecl>() {{ new VarDecl(BaseType.INT, "i"); }}));
		put(funSymFromDecl(BaseType.VOID, "print_c", new ArrayList<VarDecl>() {{ new VarDecl(BaseType.CHAR, "c"); }}));
		
		put(funSymFromDecl(BaseType.CHAR, "read_c"));
		put(funSymFromDecl(BaseType.INT , "read_i"));
		
		put(funSymFromDecl(new PointerType(BaseType.VOID), "mcmalloc", new ArrayList<VarDecl>() {{ new VarDecl(BaseType.INT, "size"); }}));
	}};

	private Map<String, StructTypeDecl> structs = new HashMap<>();

	private Scope currentScope = globalScope;
	
	public Void visitProgram(Program p) {

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

	public Void visitBaseType(BaseType bt) {
		return null;
	}

	public Void visitPointerType(PointerType pt) {
		pt.t.accept(this);
		return null;
	}

	public Void visitStructType(StructType st) {
		StructTypeDecl std = structs.get(st.structType);
		if (std != null) {
			st.std = std;
		} else {
			error("unknown struct usage: " + st.structType);
		}

		return null;
	}

	public Void visitArrayType(ArrayType at) {
		at.t.accept(this);
		return null;
	}

	public Void visitStructTypeDecl(StructTypeDecl std) {
		String name = std.st.structType;
		if (structs.get(name) != null) {
			error("duplicate struct name in scope: " + name);
		} else {
			structs.put(name, std);
			// name analysis on variables in struct scope
			currentScope = new Scope(currentScope, "struct " + name);
			for (VarDecl vd : std.vds) {
				vd.accept(this);
			}
			currentScope = currentScope.getOuter();
		}
		return null;
	}

	public Void visitVarDecl(VarDecl vd) {
		if (currentScope.lookupCurrent(vd.name) != null) {
			error("duplicate var name in scope: " + vd.name);
		} else {
			vd.type.accept(this);
			currentScope.put(new VarSymbol(vd));
		}
		return null;
	}

	public Void visitFunDecl(FunDecl fd) {
		if (currentScope.lookupCurrent(fd.name) != null) {
			error("duplicate func name in scope: " + fd.name);
		} else {
			currentScope.put(new FunSymbol(fd));
		}
		// create new empty scope for function parameters
		currentScope = new Scope(currentScope, "function " + fd.name);
		for (VarDecl vd : fd.params) {
			vd.accept(this);
		}

		fd.block.accept(this);

		currentScope = currentScope.getOuter();
		return null;
	}

	public Void visitIntLiteral(IntLiteral i) {
		return null;
	}

	public Void visitStrLiteral(StrLiteral s) {
		return null;
	}

	public Void visitChrLiteral(ChrLiteral c) {
		return null;
	}

	public Void visitVarExpr(VarExpr v) {
		Symbol s = currentScope.lookup(v.name);
		if (s != null && s.isVar()) {
			// cast to varsymbol
			VarSymbol vs = (VarSymbol) s;
			// set expression's vardecl field
			v.vd = vs.vd;
		} else {
			error("unknown variable access: " + v.name);
		}

		return null;
	}

	public Void visitFunCallExpr(FunCallExpr fce) {
		Symbol s = currentScope.lookup(fce.name);
		if (s != null && s.isFun()) {
			// cast to funsymbol
			FunSymbol fs = (FunSymbol) s;
			// set expression's fundecl field
			fce.fd = fs.fd;
			for (Expr e : fce.args) {
				e.accept(this);
			}
		} else {
			error("unknown function call: " + fce.name);
		}

		return null;
	}

	public Void visitBinOp(BinOp bo) {
		bo.left.accept(this);
		bo.right.accept(this);
		return null;
	}

	public Void visitOp(Op o) {
		return null;
	}

	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		aae.array.accept(this);
		aae.index.accept(this);
		return null;
	}

	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		// recursively test left side for field
		fae.struct.accept(this);
		if (!fae.struct.isVarExpr() && !fae.struct.isFieldAccessExpr()) {
			error("expression does not contain field '" + fae.field + "'");
		} else {
			Type left = fae.struct.type;
			if (fae.struct.isVarExpr()) {
				// has a struct type
				VarExpr ve = (VarExpr) fae.struct;
				left = (StructType) ve.vd.type;
			}

			if (left != null) {
				try {
					String name = ((StructType) left).structType;
					StructTypeDecl std = structs.get(name);
					if (!std.hasField(fae.field)) {
						error("struct '" + name + "' does not contain field '" + fae.field + "'");
					} else {
						fae.type = std.getFieldType(fae.field);
					}
				} catch (Exception e) {}
			}
		}
		return null;
	}

	public Void visitValueAtExpr(ValueAtExpr vae) {
		vae.expr.accept(this);
		return null;
	}

	public Void visitSizeOfExpr(SizeOfExpr soe) {
		soe.t.accept(this);
		return null;
	}

	public Void visitTypecastExpr(TypecastExpr te) {
		te.t.accept(this);
		te.expr.accept(this);
		return null;
	}

	public Void visitExprStmt(ExprStmt es) {
		es.expr.accept(this);
		return null;
	}

	public Void visitWhile(While w) {
		w.cond.accept(this);
		
		currentScope = new Scope(currentScope, currentScope.namespace + " -> while");
		w.stmt.accept(this);
		currentScope = currentScope.getOuter();
		return null;
	}

	public Void visitIf(If i) {
		i.cond.accept(this);
		
		currentScope = new Scope(currentScope, currentScope.namespace + " -> if");
		i.stmt.accept(this);
		currentScope = currentScope.getOuter();

		if (i.elseStmt != null) {
			currentScope = new Scope(currentScope, currentScope.namespace + " -> else");
			i.elseStmt.accept(this);
			currentScope = currentScope.getOuter();
		}
		return null;
	}

	public Void visitAssign(Assign a) {
		a.left.accept(this);
		a.right.accept(this);
		return null;
	}

	public Void visitReturn(Return r) {
		if (r.expr != null) {
			r.expr.accept(this);
		}
		return null;
	}

	public Void visitBlock(Block b) {
		for (VarDecl vd : b.vds) {
			vd.accept(this);
		}
		currentScope = new Scope(currentScope, currentScope.namespace + " -> stmt");
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		currentScope = currentScope.getOuter();
		return null;
	}

}
