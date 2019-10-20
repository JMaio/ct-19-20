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
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitArrayType(ArrayType at) {
		return null;
	}

	public Void visitStructTypeDecl(StructTypeDecl std) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitVarDecl(VarDecl vd) {
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitFunDecl(FunDecl fd) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	public Void visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
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
