package gen;

import ast.*;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

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

    // private HashMap<Op, String> ops = new HashMap<Op, String>() {{
    //     put(Op.ADD, "add");
    //     put(Op.ADD, "add");
    // }};

    // private String opToInstruction(Op op) {

    // }

    private void writeSysFunction(String name, int opcode) {
        writer.write(name + ":\n");

        writer.write("    li $v0, " + opcode + "\n");
        // writer.write("add " + "\n");
        writer.write("    syscall\n");
    }

    @Override
    public Register visitProgram(Program p) {
        writer.write("# jwow compiler v0.0.1\n");

        writer.write("    .data\n");
        for (VarDecl vd : p.varDecls) {
            vd.accept(this);
        }

        writer.write("\n");
        writer.write("    .text\n");
        writer.write("    .globl main            # set main\n");
        writer.write("\n");
        writer.write("exec_main:\n");
        writer.write("    j main                 # unconditional jump to main");
        writer.write("\n");

        ArrayList<Pair<String, Integer>> sysfuncs = new ArrayList<Pair<String, Integer>>() {{
            add(new Pair<String,Integer>("print_i", 1));
            add(new Pair<String,Integer>("print_s", 4));
            add(new Pair<String,Integer>("print_c", 11));
            
            add(new Pair<String,Integer>("read_i", 5));
            add(new Pair<String,Integer>("read_c", 12));
            
            add(new Pair<String,Integer>("mcmalloc", 9));
        }};

        for (Pair<String, Integer> f : sysfuncs) {
            writeSysFunction(f.getKey(), f.getValue());
            writer.print("\n");
        }

        for (FunDecl fd : p.funDecls) {
            fd.accept(this);
        }

        writer.write("exit:                       #\n");
        writer.write("    li   $v0, 10            # exit()\n");
        writer.write("    syscall                 #\n");
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

        return null;
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
        return null;
    }
}
