package gen;

import ast.*;

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

    private HashMap<String, Register> vars = new HashMap<>();

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
        writer.write("exec_main:\n");
        writer.write(Instruction.j("main"));
        writer.write("\n");
        
        
        writeHeading("library functions");
        // HashMap<String, Integer> sysfuncs = new HashMap<String, Integer>() {{
        //     put("print_i", 1);
        //     put("print_s", 4);
        //     put("print_c", 11);
        //     put("read_i", 5);
        //     put("read_c", 12);
        //     put("mcmalloc", 9);
        // }};

        // for (Entry<String, Integer> f : sysfuncs.entrySet()) {
        //     writeSysFunction(f.getKey(), f.getValue());
        //     writer.print("\n");
        // }

        writer.write(LibFunc.printSysFuncs());

        writeHeading("program functions");

        // "normal" functions as macros
        for (FunDecl fd : p.funDecls) {
            if (!fd.name.equals("main")) {
                fd.accept(this);
            }
        }
        // main as a label
        writer.write("main:");
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
        writer.write("    # " + b.getClass().getSimpleName() + " \n");
        List<Register> regs = new ArrayList<>();
        for (Stmt s : b.stmts) {
            regs.add(s.accept(this));
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

        writer.write(Instruction.InstrFmt(
            ".macro %s (%s)", fd.name, String.join(", ", params)));
        
        writeTextSection();
        
        fd.block.accept(this);

        // writer.write( "" +
        // "    .macro load_str_lit (" + r + "," + var + ")\n" +
        // "    .data\n" +
        // "string: .asciiz " + var + "\n" +
        // "    .text\n" +
        // "    la " + r + ", string\n");
        // Instruction.la(r, "string") +
        
        writer.write(Instruction.InstrFmt(".end_macro"));
        // writer.write("    # " + fd.getClass().getSimpleName() + " \n");
        // writer.write(fd.name + ":\n");
        
        // fd.block.accept(this);
        
        // exit after main
        // if (fd.name.equals("main")) {
        //     writer.write(Instruction.j("exit"));
        // } else {
        //     writer.write(Instruction.jr(Register.ra));
        // }
        freeAllRegisters();

        writer.write("\n");
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // writer.write(String.format("%s:", vd.name));
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        Register r = vars.get(v.name);
        // TODO: fix this
        if (r == null) {
            r = getRegister();
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

        // int aIndex = 0;
        // for (Expr arg : fce.args) {
        //     Register aReg = Register.paramRegs[aIndex++];
        //     Register thisReg = arg.accept(this);
        //     writer.write(Instruction.la(aReg, thisReg));
        // }
        // writer.write(Instruction.jal(fce.name) + "\n");
        String i = "    " + fce.name;
        String delimiter = "";

        if (fce.args.size() > 0) {
            i += "(";
            for (Expr arg : fce.args) {
                i += arg.accept(this);
            }
            i += ")";
        }

        writer.write(i + "\n");
        writer.write("\n");

        // free temporary registers?

        // result should have been returned in $v0
        return Register.v0;
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
            case SUB: i = Instruction.sub(l, l, r); break;
            case MUL: i = Instruction.mul(l, l, r); break;
            case DIV: i = Instruction.div(l, l, r); break;
            case MOD: i = Instruction.mod(l, l, r); break;

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
            vars.put(name, r);
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
        if (!r.fd.name.equals("main")) {
            writer.write(Instruction.jr(Register.ra));
        }
        return null;
    }
}
