package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map.Entry;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
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


    private PrintWriter writer; // use this writer to output the assembly instructions
    
    
    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);
        
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
        writer.write("exec_main:\n");
        writer.write(Instruction.j("main"));
        writer.write("\n");
        
        
        writeHeading("library functions");
        HashMap<String, Integer> sysfuncs = new HashMap<String, Integer>() {{
            put("print_i", 1);
            put("print_s", 4);
            put("print_c", 11);
            put("read_i", 5);
            put("read_c", 12);
            put("mcmalloc", 9);
        }};

        for (Entry<String, Integer> f : sysfuncs.entrySet()) {
            writeSysFunction(f.getKey(), f.getValue());
            writer.print("\n");
        }

        writeHeading("program functions");

        for (FunDecl fd : p.funDecls) {
            fd.accept(this);
        }

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
        writer.write("    # " + b.getClass().getSimpleName() + " \n");
        for (Stmt s : b.stmts) {
            s.accept(this);
        }

        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl fd) {
        // create the function label
        writer.write("    # " + fd.getClass().getSimpleName() + " \n");
        writer.write(fd.name + ":\n");
        
        fd.block.accept(this);
        
        // exit after main
        if (fd.name.equals("main")) {
            writer.write(Instruction.j("exit"));
        } else {
            writer.write(Instruction.jr(Register.ra));
        }

        writer.write("\n");
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
        return null;
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
        // TODO Auto-generated method stub
        Register r = getRegister();

        writer.write("    # " + i.getClass().getSimpleName() + " \n");
        writer.write("    li   " + r + ", " + i.value + "\n");
        return r;
    }

    @Override
    public Register visitStrLiteral(StrLiteral s) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral c) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fce) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        // TODO Auto-generated method stub
        Register l = bo.left.accept(this);
        Register r = bo.right.accept(this);

        String i = "";

        switch (bo.op) {
            // add and store in left register, saving one register
            case ADD: i = Instruction.add(l, l, r); break;
        
            default:
                break;
        }

        writer.write(i);
        
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
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
        if (!r.fd.name.equals("main")) {
            writer.write(Instruction.jr(Register.ra));
        }
        return null;
    }
}
