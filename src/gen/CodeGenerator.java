package gen;

import ast.*;
import sem.Scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Map.Entry;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    // 'total' stack offset
    int stackOffset = 0;
    // offset from last stack offset,
    // added to stackOffset and reset when entering a new block
    int frameOffset = 0;
    // private HashMap<String, Register> vars = new HashMap<>();

    public CodeGenerator() {
        freeAllRegisters();
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }

    private void freeAllRegisters() {
        freeRegs.removeAllElements();
        freeRegs.addAll(Register.tmpRegs);
    }


    private PrintWriter writer; // use this writer to output the assembly instructions
    
    
    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);
        
        ExpressionSimplifier es = new ExpressionSimplifier();
        program.accept(es);

        visitProgram(program);
        writer.close();
    }
    
    private void writeDataSection() {
        writer.write("    .data\n");
    }
    private void writeTextSection() {
        writer.write("    .text\n");
    }


    private void writeSysFunction(String name, int opcode) {
        writer.write(name + ":\n");

        writer.write(Instruction.li(Register.v0, opcode));
        writer.write(Instruction.syscall());
        writer.write(Instruction.jr(Register.ra));
    }

    private void writeHeading(String h) {
        writer.write("# -------------------------------\n");
        writer.write("# " + h + "\n");
        writer.write("# -------------------------------\n");
        writer.write("\n");
    }

    @Override
    public Register visitProgram(Program p) {
        

        writeHeading("jwow compiler v0.0.1");
        writeDataSection();
        for (VarDecl vd : p.varDecls) {
            vd.accept(this);
        }

        writer.write("\n");
        writeTextSection();
        writer.write("    .globl main            # set main\n");
        writer.write("\n");
        
        writeHeading("library functions");

        writer.write(LibFunc.printSysFuncs());

        writeHeading("program functions");

        for (FunDecl fd : p.funDecls) {
            if (!fd.name.equals("main")) {
                fd.accept(this);
            }
        }

        writer.write("exec_main:\n");
        writer.write(Instruction.j("main"));
        writer.write("\n");
        // main as a label
        writer.write("main:\n");
        // no args in main
        p.main.block.accept(this);

        writer.write(Instruction.j("exit"));

        writer.write("exit:                       #\n");
        writer.write(Instruction.li(Register.v0, 10));
        writer.write(Instruction.syscall());

        return null;
    }

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        // TODO: to complete
        // reset stack offset
        stackOffset += frameOffset;
        frameOffset = 0;
        // writer.write("    # " + b.getClass().getSimpleName() + " \n");
        // List<Register> regs = new ArrayList<>();
        for (VarDecl vd : b.vds) {
            vd.accept(this);
            // vd.offset = stackOffset + frameOffset;
            // int s = vd.type.size();
        }
        for (Stmt s : b.stmts) {
            s.accept(this);
        }
        

        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl fd) {

        // create the function label
        List<String> params = new ArrayList<>();
        for (VarDecl v : fd.params) {
            params.add("%" + v.name);
        }

        writer.write(String.format("%s:\n", fd.name));
        
        fd.block.accept(this);
                
        freeAllRegisters();

        writer.write("\n");
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if (vd.global) {
            // TODO should all vars be aligned to 4 bytes?
            writer.write(String.format("%-11s .space %d\n", vd.name + ":", vd.type.size()));
        } else {
            // save stack offset
            writer.write(String.format("# '%s' stack offset = %d\n", vd.name, vd.offset));
            vd.offset = stackOffset + frameOffset;
            frameOffset += vd.type.size();
        }
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: fix this
        Register r = getRegister();
        if (v.vd.global) {
            // load global variable from label
            writer.write(Instruction.la(r, v.name));
        } else {
            // v.vd.
            // load from stack
        }

        return r;        
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral i) {
        // inefficiency of assigning register here no matter what
        Register r = getRegister();

        // writer.write("    # " + i.getClass().getSimpleName() + " \n");
        writer.write(Instruction.li(r, i.value));
        // writer.write("    li   " + r + ", " + i.value + "\n");
        return r;
    }

    @Override
    public Register visitStrLiteral(StrLiteral s) {
        // TODO Auto-generated method stub
        Register r = getRegister();
        
        writer.write(Instruction.InstrFmt("load_str_lit(%s, \"%s\")\n", r, s.value));
        // writer.write("# " + s.value + "\n");

        return r;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral c) {
        // inefficiency of assigning register here no matter what
        Register r = getRegister();

        // writer.write("    # " + c.getClass().getSimpleName() + " \n");
        writer.write(Instruction.li(r, c.value));
        // writer.write("    li   " + r + ", " + (int) c.value + "\n");
        return r;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fce) {
        // use argument registers. TODO: use the stack?

        if (LibFunc.isLibFunc(fce.name)) {
            // String i = "    " + fce.name;
            // String delimiter = "";
            // System.out.println("function: " +fce.name);
            ArrayList<String> args = new ArrayList<>();

            if (fce.args.size() > 0) {
                // i += "(";
                for (Expr arg : fce.args) {
                    args.add(arg.accept(this).toString());
                }
                // i += ")";
            }
            writer.write(
                Instruction.InstrFmt("%s (%s)\n", fce.name, String.join(", ", args)));
            writer.write("\n");
        } else {
            // TODO
        }
        // int aIndex = 0;
        // for (Expr arg : fce.args) {
        //     Register aReg = Register.paramRegs[aIndex++];
        //     Register thisReg = arg.accept(this);
        //     writer.write(Instruction.la(aReg, thisReg));
        // }
        // writer.write(Instruction.jal(fce.name) + "\n");
        // String i = "    " + fce.name;
        // String delimiter = "";

        // if (fce.args.size() > 0) {
        //     i += "(";
        //     for (Expr arg : fce.args) {
        //         i += arg.accept(this);
        //     }
        //     i += ")";
        // }
        
        // free temporary registers?

        // result should have been returned in $v0
        return Register.v0;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        // TODO Auto-generated method stub

        Register result = getRegister();
        Register l = bo.left.accept(this);
        Register r = bo.right.accept(this);

        String i = "";

        switch (bo.op) {
            // add and store in left register, saving one register
            case ADD: i = Instruction.add(result, l, r); break;
            case SUB: i = Instruction.sub(result, l, r); break;
            case MUL: i = Instruction.mul(result, l, r); break;
            case DIV: i = Instruction.div(result, l, r); break;
            case MOD: i = Instruction.mod(result, l, r); break;

            // case GT : i = Instruction.(); break;
            // case LT : i = Instruction.(); break;

            // case GE : i = Instruction.(); break;
            // case LE : i = Instruction.(); break;

            // case NE : i = Instruction.(); break;
            // case EQ : i = Instruction.(); break;

            // case OR : i = Instruction.(); break;
            // case AND: i = Instruction.(); break;

            // case GT:  i = Instruction.
        
            default:
                break;
        }

        writer.write(i);
        freeRegister(r);
        
        return l;
    }

    @Override
    public Register visitOp(Op o) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr soe) {
        // will always
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        // writer.write(s);
        Register r = te.expr.accept(this);
        // writer.write(te.);
        return r;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        es.expr.accept(this);
        return null;
    }

    @Override
    public Register visitWhile(While w) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitIf(If i) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        // TODO: assign goes to local or global variable?
        Register r = a.right.accept(this);
        // TODO: account for arrays and pointers, not just vars
        if (a.left.isVarExpr()) {
            String name = ((VarExpr) a.left).name;
            // vars.put(name, r);
        } else if (a.left.isFieldAccessExpr()) {

        } else if (a.left.isArrayAccessExpr()) {

        }

        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        // TODO Auto-generated method stub
        writer.write(Instruction.InstrFmt("# return (%s)", r.funReturnType));
        if (r.expr != null) {
            // expression evaluated and placed in a register
            Register rReg = r.expr.accept(this);
            // this is the return of the function so set $v0 to it
            writer.write(Instruction.la(Register.v0, rReg));
        } else {
            writer.write(Instruction.la(Register.v0, Register.zero));
        }
        
        // return in main?
        // if (!r.fd.name.equals("main")) {
        //     writer.write(Instruction.jr(Register.ra));
        // }
        return null;
    }
}
