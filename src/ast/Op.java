package ast;

public enum Op implements ASTNode {
    ADD, SUB, MUL, DIV, MOD,
    GT, LT, 
    GE, LE, 
    NE, EQ,
    OR, AND;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitOp(this);
    }

    @Override
    public String toString() {
        switch (this) {
            case ADD: return "+";
            case SUB: return "-";
            case MUL: return "*";
            case DIV: return "/";
            case MOD: return "%";
            case GT : return ">";
            case LT : return "<";
            case GE : return ">=";
            case LE : return "<=";
            case NE : return "!=";
            case EQ : return "==";
            case OR : return "||";
            case AND: return "&&";
            
            default : return "?";
        }
    }
    
}
