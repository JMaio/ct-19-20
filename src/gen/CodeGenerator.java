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

    //  write a line to the file (with trailing newline)
    public void write(String s) {
        writer.write(s + '\n');
    }
    public void write(String s, Object... args) {
        write(String.format(s, args));
    }

    // create a formatted comment
    public void comment(String s, Object... args) {
        write("    # " + s, args);
    }

    public void nl() {
        write("");
    }

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    private Stack<Integer> stackOffsets = new Stack<Integer>();

    // 'total' stack offset
    int stackOffset = 0;
    // offset from last stack offset,
    // added to stackOffset and reset when entering a new block
    int frameOffset = 0;

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
        write("    .data");
    }
    private void writeTextSection() {
        write("    .text");
    }

    private void writeHeading(String h) {
        write("# -------------------------------");
        write("# " + h);
        write("# -------------------------------");
        nl();
    }

    @Override
    public Register visitProgram(Program p) {
        
        writeHeading("jwow compiler v0.0.1");
        writeDataSection();
        for (VarDecl vd : p.varDecls) {
            vd.accept(this);
        }
        nl();

        writeTextSection();
        write("    .globl main            # set main");
        nl();
        
        write("exec_main:");
        write(Instruction.j("main"));
        nl();

        writeHeading("library functions");

        write(LibFunc.printSysFuncs());

        writeHeading("program functions");

        for (FunDecl fd : p.funDecls) {
            if (!fd.name.equals("main")) {
                fd.accept(this);
            }    
        }    

        // main
        write("main:");
        p.main.block.accept(this);

        write(Instruction.j("exit"));
        nl();

        write("exit:");
        write(Instruction.li(Register.v0, 10));
        write(Instruction.syscall());

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

        stackOffsets.push(stackOffset);
        stackOffset -= frameOffset;
        frameOffset = 0;
        
        for (VarDecl vd : b.vds) {
            vd.accept(this);
        }

        for (Stmt s : b.stmts) {
            s.accept(this);
        }
        
        stackOffset = stackOffsets.pop();
        frameOffset = 0;

        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl fd) {

        // create the function label
        List<String> params = new ArrayList<>();
        for (VarDecl v : fd.params) {
            params.add("%" + v.name);
        }

        write("%s:", fd.name);
        
        fd.block.accept(this);
                
        freeAllRegisters();

        nl();
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if (vd.global) {
            comment("%s", vd.name);
            write(Instruction.InstrFmt("%-11s .space %d", vd.name + ":", vd.type.size()));
            if (vd.type.size() % 4 != 0) {
                // align non-word vars to 4 bytes
                write(Instruction.InstrFmt("%-11s .align 2", ""));
            }
        } else {
            // align non-word vars to 4 bytes
            int size = Type.alignTo4Byte(vd.type.size());
            frameOffset -= size;

            // save stack offset
            vd.offset = stackOffset + frameOffset;

            comment("# '%s' offset = %d", vd.name, vd.offset);
            write(Instruction.incrementSp(-size));
        }
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        Register r = getRegister();
        if (v.vd.global) {
            // load global variable from label
            write(Instruction.lw(r, v.name));
        } else {
            // load from stack
            comment("# offset '%s'\n", v.name);
            write(Instruction.lw(r, Register.fp, v.vd.offset));
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
        write(Instruction.li(r, i.value));
        return r;
    }

    @Override
    public Register visitStrLiteral(StrLiteral s) {
        // TODO Auto-generated method stub
        Register r = getRegister();
        
        write(Instruction.InstrFmt("load_str_lit(%s, \"%s\")\n", r, s.value));
        // writer.write("# " + s.value + "\n");

        return r;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral c) {
        // inefficiency of assigning register here no matter what
        Register r = getRegister();
        write(Instruction.li(r, c.value));
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
            write(Instruction.InstrFmt("%s (%s)\n", fce.name, String.join(", ", args)));
            write("");
        } else {
            // TODO
        }
        
        // TODO free temporary registers?

        // result should have been returned in $v0
        return Register.v0;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
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

        write(i);
        freeRegister(l);
        freeRegister(r);
        
        return l;
    }

    @Override
    public Register visitOp(Op o) {
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
        // TODO will always be exaluated beforehand?
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

        comment("# %s = %s", a.left, a.right);

        if (Expr.isVarExpr(a.left)) {

            VarDecl vd = ((VarExpr) a.left).vd;
            if (vd.global) {
                write(Instruction.sw(r, vd.name));
            } else {
                int offset = vd.offset;
                comment("# store %s at (%d)", r, offset);
                write(Instruction.sw(Register.fp, r, offset));
            }

        } else if (Expr.isFieldAccessExpr(a.left)) {

        } else if (Expr.isArrayAccessExpr(a.left)) {

        } else {
            
        }

        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        // TODO Auto-generated method stub
        comment("return (%s)", r.funReturnType);
        if (r.expr != null) {
            // expression evaluated and placed in a register
            Register rReg = r.expr.accept(this);
            // this is the return of the function so set $v0 to it
            write(Instruction.la(Register.v0, rReg));
        } else {
            write(Instruction.la(Register.v0, Register.zero));
        }
        
        // return in main?
        // if (!r.fd.name.equals("main")) {
        //     writer.write(Instruction.jr(Register.ra));
        // }
        return null;
    }
}
