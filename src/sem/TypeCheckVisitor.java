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
		return pt.t.accept(this);
	}

	public Type visitStructType(StructType st) {
		return st;
	}

	public Type visitArrayType(ArrayType at) {
		return at.accept(this);
	}

	public Type visitStructTypeDecl(StructTypeDecl std) {
		for (VarDecl vd : std.vds) {
			vd.accept(this);
		}
		// not necessary to return the structtype, but do it anyways
		return std.st;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl fd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitStrLiteral(StrLiteral s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Type visitBlock(Block b) {
		// TODO Auto-generated method stub
		return null;
	}


}
