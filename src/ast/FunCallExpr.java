package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FunCallExpr extends Expr {

    public final String name;
    public List<Expr> args;
    public FunDecl fd;
    public List<Expr> regArgs = new ArrayList<Expr>();
    public List<Expr> stackArgs = new ArrayList<Expr>();

    public FunCallExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }

    @Override
    public String toString() {
        return String.format("%s ()", fd.name);
    }

}
