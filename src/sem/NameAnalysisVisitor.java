package sem;

import java.util.ArrayList;
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
		return null;
	}

	public Void visitStructType(StructType st) {
		Symbol s = currentScope.lookup(st.structType);
		if (s != null && s.isStruct()) {
			// cast to structsymbol
			StructSymbol ss = (StructSymbol) s;
			// set expression's structdecl field
			st.std = ss.std;
		} else {
			error("unknown struct usage");
		}

		return null;
	}

	public Void visitArrayType(ArrayType at) {
		return null;
	}

	public Void visitStructTypeDecl(StructTypeDecl std) {
		if (currentScope.lookupCurrent(std.st.structType) != null) {
			error("duplicate struct name in scope");
		} else {
			currentScope.put(new StructSymbol(std));
		}
		return null;
	}

	public Void visitVarDecl(VarDecl vd) {
		if (currentScope.lookupCurrent(vd.name) != null) {
			error("duplicate var name in scope");
		} else {
			currentScope.put(new VarSymbol(vd));
		}
		return null;
	}

	public Void visitFunDecl(FunDecl fd) {
		assert fd != null;
		assert fd.name != null;
		if (currentScope.lookupCurrent(fd.name) != null) {
			error("duplicate func name in scope");
		} else {
			currentScope.put(new FunSymbol(fd));
		}
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
			error("unknown variable access");
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
		} else {
			error("unknown function call");
		}

		return null;
	}

	public Void visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		return null;
	}

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
		// TODO Auto-generated method stub
		return null;
	}

}
