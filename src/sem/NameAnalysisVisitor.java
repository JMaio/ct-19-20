package sem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	public TypeCheckVisitor tcv = new TypeCheckVisitor();

	public FunSymbol funSymFromDecl(Type t, String name, List<VarDecl> params) {
		return new FunSymbol(new FunDecl(t, name, params));
	}
	public FunSymbol funSymFromDecl(Type t, String name) {
		return funSymFromDecl(t, name, new ArrayList<>());
	}

	// dummy library functions
	private Scope globalScope = new Scope() {{
		put(funSymFromDecl(BaseType.VOID, "print_s", new ArrayList<VarDecl>() {{ add(new VarDecl(new PointerType(BaseType.CHAR), "s")); }}));
		put(funSymFromDecl(BaseType.VOID, "print_i", new ArrayList<VarDecl>() {{ add(new VarDecl(BaseType.INT, "i")); }}));
		put(funSymFromDecl(BaseType.VOID, "print_c", new ArrayList<VarDecl>() {{ add(new VarDecl(BaseType.CHAR, "c")); }}));
		
		put(funSymFromDecl(BaseType.CHAR, "read_c"));
		put(funSymFromDecl(BaseType.INT , "read_i"));
		
		put(funSymFromDecl(new PointerType(BaseType.VOID), "mcmalloc", new ArrayList<VarDecl>() {{ add(new VarDecl(BaseType.INT, "size")); }}));
	}};

	private FunDecl currentFun;

	private Map<String, StructTypeDecl> structs = new HashMap<>();

	private Scope currentScope = globalScope;
	
	public Void visitProgram(Program p) {

		FunDecl main = null;

		for (StructTypeDecl std : p.structTypeDecls) {
			std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
			vd.accept(this);
			vd.global = true;
        }
        for (FunDecl fd : p.funDecls) {
			if (fd.name.equals("main")) {
				main = fd;
			}
			currentFun = fd;
			fd.accept(this);
		}

		if (main != null) {
			p.main = main;
		} else {
			error("no 'main' in program");
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
				// struct cannot self-reference unless it's a pointer to itself
				if (Type.isStructType(vd.type) && ((StructType) vd.type).structType.equals(std.st.structType)) {
					error(String.format("cannot self-reference struct '%s'", std.st.structType));
				} else {
					vd.accept(this);
				}
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
		
		if (currentScope.lookup(fd.name) != null) {
			error("duplicate func name in scope: " + fd.name);
		} else {
			currentScope.put(new FunSymbol(fd));
		}

		fd.type.accept(this);

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
		try {
			// get the typechecker to do the work here:
			// whatever is being checked *will* have been defined at this point,
			// otherwise it is not semantically correct
			StructType t = (StructType) fae.struct.accept(tcv);

			if (structs.get(t.structType) != null) {
				if (!t.std.hasField(fae.field)) {
					error("struct '" + t.structType + "' does not contain field '" + fae.field + "'");
				}
				fae.type = t.std.getFieldType(fae.field);
			}
		} catch (Exception e) {
			error("bad field access: " + fae.struct + "." + fae.field);
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
		if (currentFun != null) {
			r.fd = currentFun;
			r.funReturnType = currentFun.type;
		}
		if (r.expr != null) {
			r.expr.accept(this);
		}
		return null;
	}

	public Void visitBlock(Block b) {
		currentScope = new Scope(currentScope, currentScope.namespace + " -> block");
		for (VarDecl vd : b.vds) {
			if (currentFun.hasParam(vd.name)) {
				error(String.format("cannot shadow function parameter '%s'", vd.name));
			} else {
				vd.accept(this);
			}
		}
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		currentScope = currentScope.getOuter();
		return null;
	}

}
