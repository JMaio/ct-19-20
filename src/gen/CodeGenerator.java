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

    public void comment(String s) {
        write("    # " + s);
    }
    // create a formatted comment
    public void comment(String s, Object... args) {
        comment(String.format(s, args));
    }

    public void nl() {
        write("");
    }

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();
    private ArrayList<Register> regsInUse = new ArrayList<Register>();

    private Stack<Integer> stackOffsets = new Stack<Integer>();

    // 'total' stack offset
    int stackOffset = 0;
    // offset from last stack offset,
    // added to stackOffset and reset when entering a new block

    UIDLabel uidLabel = new UIDLabel();

    public CodeGenerator() {
        freeAllRegisters();
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            Register r = freeRegs.pop();
            regsInUse.add(r);
            return r;
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        if (Register.tmpRegs.contains(reg) && !freeRegs.contains(reg)) {
            freeRegs.push(reg);
            regsInUse.remove(reg);
        }
    }

    private void restoreUsedRegisters(ArrayList<Register> regs) {
        freeAllRegisters();
        freeRegs.addAll(regs);
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
        comment("-------------------------------");
        comment(h);
        comment("-------------------------------");
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
        
        comment("exec_main");
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

        // write(Instruction.j("exit"));
        // nl();

        comment("exit");
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

        // stackOffset += frameOffset;
        // frameOffset = 0;
        
        for (VarDecl vd : b.vds) {
            // no register return
            vd.accept(this);
        }

        for (Stmt s : b.stmts) {
            Register r = s.accept(this);
            freeRegister(r);
        }
        
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl fd) {
        // reset the stack
        // stackOffsets.push(stackOffset);
        int frameOffset = 0;
        
        // create the function label
        write("%s:", fd.name);

        int returnSize = fd.type.size();
        write(Instruction.incrementSp(returnSize));
        frameOffset -= returnSize;

        comment("allocate stack space for vars");
        for (VarDecl param : fd.params) {
            param.accept(this);
            frameOffset -= param.type.size();
        }
        
        for (Register r : new Register[] {Register.fp, Register.ra}) {
            comment("save %s", r);
            write(Instruction.incrementSp(4));
            frameOffset -= 4;
            write(Instruction.sw(Register.fp, r, frameOffset));
        }

        // ArrayList<Register> savedRegs = new ArrayList<>();
        comment("save temporary registers");    
        for (Register r : Register.tmpRegs) {
            write(Instruction.incrementSp(4));
            frameOffset -= 4;
            write(Instruction.sw(Register.fp, r, frameOffset));
            // if (!freeRegs.contains(r)) {
            //     savedRegs.add(r);
            // }
        }

        // store used registers
        ArrayList<Register> inUse = regsInUse;
        freeAllRegisters();

        fd.block.accept(this);        

        comment("load old $fp");
        write(Instruction.lw(Register.fp, Register.fp, 4 * 20));
        
        int callStackOffset = 0;
        for (Register r : new Register[] {Register.ra}) {
            comment("load old %s", r);
            callStackOffset -= 4;
            write(Instruction.lw(r, Register.fp, callStackOffset));
        }
        comment("load temporary registers");    
        for (Register r : Register.tmpRegs) {
            callStackOffset -= 4;
            write(Instruction.lw(r, Register.fp, callStackOffset));
        }
        
        comment("set new stack pointer location");
        write(Instruction.decrementSp(callStackOffset));

        write(Instruction.addi(Register.v0, Register.fp, -returnSize));
        // stackOffset = stackOffsets.pop();
        // write(Instruction.move(Register.fp, Register.sp));

        // restore registers which were in use
        restoreUsedRegisters(inUse);

        // return
        comment("default return even if one exists");
        write(Instruction.jr(Register.ra));
        comment("-------------------- end %s", fd.name);
        nl();

        // all registers here are only local
        // freeAllRegisters();
        // stackOffset = stackOffsets.pop();
        
        nl();
        // reset global stack offset
        stackOffset = 0;

        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if (vd.global) {
            comment(vd.toString());
            write(Instruction.InstrFmt("%-11s .space %d", vd.name + ":", vd.type.size()));
            if (vd.type.size() % 4 != 0) {
                // align non-word vars to 4 bytes
                write(Instruction.InstrFmt("%-11s .align 2", ""));
            }
        } else {
            // align non-word vars to 4 bytes
            int size = Type.alignTo4Byte(vd.type.size());
            stackOffset -= size;

            // save stack offset
            vd.offset = stackOffset;

            comment("stack space for '%s' => %d bytes", vd.name, size);
            write(Instruction.incrementSp(size));
        }
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        Register r = getRegister();
        if (v.vd.global) {
            // load global variable from label
            write(Instruction.la(r, v.name));
        } else {
            // load from stack
            comment("load '%s' at $fp offset (%d)", v.name, v.vd.offset);
            write(Instruction.la(r, Register.fp, v.vd.offset));
        }
        return r;        
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
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
        Register r = getRegister();
        
        write(Instruction.InstrFmt("load_str_lit(%s, \"%s\")", r, s.value));

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
        comment("%s () [immediate=%s]", fce.name, fce.isImmediate);

        // TODO save previous $ra on the stack, as this could be a call inside a call

        if (LibFunc.isLibFunc(fce.name)) {
            ArrayList<String> args = new ArrayList<>();

            if (fce.args.size() > 0) {
                for (Expr arg : fce.args) {
                    Register r = arg.accept(this);
                    if (!arg.isImmediate) {
                        write(Instruction.lw(r, r));
                    }
                    args.add(r.toString());
                    freeRegister(r);
                }
            }
            
            write(Instruction.InstrFmt("%s (%s)", fce.name, String.join(", ", args)));
        } else {
            // which a register to get from the list
            // int a = 0;
            // Register prevFp = getRegister();
            // write(Instruction.move(prevFp, Register.fp));
            
            // TODO store contents of temp registers

            int returnSize = fce.type.size();            
            

            // // TODO
            // if (returnSize > 4) {
            //     // return type must be passed via stack
            //     // return value stored at $sp address
            //     write(Instruction.incrementSp(returnSize));
            //     write(Instruction.incrementFp(returnSize));
            //     // only write past the return space 
            //     // stackOffset += returnSize;
            //     // framePtr = returnSize;
            // }    
            //                                                         $fp
            //    ------------  return type size + 20 ----------------- |  sum(size(args))
            // |  return type size  |   1   |   1   |        18         |   args
            // |    return value    |  $fp  |  $ra  |  $t0-$t... others |   args


            // save registers in use
            // freeAllRegisters();

            // TODO Works
            // comment("set new frame pointer location");
            // write(Instruction.move(Register.fp, Register.sp));

            int argsOffset = 0;

            comment("arguments must come after frame pointer to be accessible");
            comment("but must be fetched using old frame pointer");
            comment("function call arguments (stack):");

            for (Expr arg : fce.args) {
                // address (int*) // 4
                // char arr[14]; // 16

                // get a register with the address
                Register r = arg.accept(this);
                int size = arg.type.size();

                argsOffset -= size;
                write(Instruction.incrementSp(size));

                // offset where this expr is stored is equal to the param's vd.offset
                comment("store %s at $sp", arg);
                if (!arg.isImmediate) {
                    write(Instruction.lw(r, r));
                }
                write(Instruction.sw(Register.fp, r, argsOffset));
                // write(Instruction.move(Register.paramRegs[a], r));
                freeRegister(r);
            }
            
            // comment("set new frame pointer location");
            // write(Instruction.addi(Register.fp, Register.fp, frameOffset));

            nl();

            comment("call the function");
            write(Instruction.jal(fce.name));

            // RESTORE ALL THE THINGS! --------------------------------------------------
            // restore content of all registers

            // write(Instruction.incrementSp(returnSize));
            
            // for (Expr arg : fce.args) {
                //     callStackOffset += arg.type.size();
                // }
                
                // write(Instruction.decrementSp(callStackOffset));
                
            
            
        }
        comment("------------- %s () : end call", fce.name);
        freeAllRegisters();
        
        // result should have been returned in $v0
        return Register.v0;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        Register result = getRegister();
        comment(bo.toString());
        comment("------------");

        comment("left: %s", bo.left);
        Register l = bo.left.accept(this);
        Register r;
        
        if (!bo.left.isImmediate) {
            comment("load value of left side from memory");
            write(Instruction.lw(l, l));
        }

        String rightLabel = uidLabel.mk("BinOp_right");
        String endLabel = uidLabel.mk("BinOp_end");
        
        if (bo.op == Op.OR || bo.op == Op.AND) {
            // cannot accept right side unconditionally

            write(Instruction.sne(result, l, 0));

            switch (bo.op) {
                // store the result
                // if 1
                //   goto next
                // if 0
                //   eval right
                //     if 1:
                //       goto next
                //     if 0:
                //       goto end
                //   
                // next:
                //  "if block"
                // end:
                case OR : write(Instruction.beq(result, 1, endLabel)); break;
                case AND: write(Instruction.beq(result, 0, endLabel)); break;
                default : break;
            }

            write(rightLabel + ":");

            r = bo.right.accept(this);
            if (!bo.right.isImmediate) {
                comment("load value of right side from memory");
                write(Instruction.lw(r, r));
            }

            write(Instruction.sne(result, r, 0));

        } else {
            String i = "";

            r = bo.right.accept(this);
            if (!bo.right.isImmediate) {
                comment("load value of right side from memory");
                write(Instruction.lw(r, r));
            }

            switch (bo.op) {
                // add and store in left register, saving one register
                case ADD: i = Instruction.add(result, l, r); break;
                case SUB: i = Instruction.sub(result, l, r); break;
                case MUL: i = Instruction.mul(result, l, r); break;
                case DIV: i = Instruction.div(result, l, r); break;
                case MOD: i = Instruction.rem(result, l, r); break;
    
                case GT : i = Instruction.sgt(result, l, r); break;
                case LT : i = Instruction.slt(result, l, r); break;
    
                case GE : i = Instruction.sge(result, l, r); break;
                case LE : i = Instruction.sle(result, l, r); break;
    
                case NE : i = Instruction.sne(result, l, r); break;
                case EQ : i = Instruction.seq(result, l, r); break;
            
                default: break;
            }

            write(i);
        }

        write(endLabel + ":");

        comment("------------");

        freeRegister(l);
        freeRegister(r);
        
        return result;
    }

    @Override
    public Register visitOp(Op o) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        comment("array access:");
        Register r = aae.array.accept(this);
        Register i = aae.index.accept(this);

        if (!aae.index.isImmediate) {
            comment("load value from " + i);    
            write(Instruction.lw(i, i));
        }

        int size = aae.type.size();

        write(Instruction.mulo(i, i, size));
        write(Instruction.add(r, r, i));

        freeRegister(i);
        comment("array accessed!...");
        return r;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        Register r = fae.struct.accept(this);

        StructType st = (StructType) fae.struct.type;
        write(Instruction.addi(r, r, st.getFieldOffset(fae.field)));

        return r;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr soe) {
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        Register r = te.expr.accept(this);
        return r;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        Register r = es.expr.accept(this);
        freeRegister(r);
        return null;
    }

    @Override
    public Register visitWhile(While w) {
        comment(w.toString());
        String loopLabel = uidLabel.mk("while_loop");
        String endLabel = uidLabel.mk("while_end");
        
        // create loop label - return here if condition still hold
        write(loopLabel + ":");

        // evaluate condition
        Register r = w.cond.accept(this);
        
        if (!w.cond.isImmediate) {
            comment("load value of if condition from memory");
            write(Instruction.lw(r, r));
        }

        // if condition no longer holds, end the loop
        write(Instruction.beq(r, Register.zero, endLabel));
        
        // otherwise, execute
        w.stmt.accept(this);
        
        // loop again
        write(Instruction.j(loopLabel));

        write(endLabel + ":");
        comment("----------");

        return null;
    }

    @Override
    public Register visitIf(If i) {
        comment(i.toString());
        String endLabel = uidLabel.mk("if_end");
        String elseLabel = uidLabel.mk("if_else");
        String next;
        Register r = i.cond.accept(this);

        // if condition is false, go to 'else' (if no else exists, acts as ending condition)
        if (i.elseStmt == null) { 
            next = endLabel;
        } else { 
            next = elseLabel;
        }
        
        if (!i.cond.isImmediate) {
            comment("load value of if condition from memory");
            write(Instruction.lw(r, r));
        }

        write(Instruction.beq(r, Register.zero, next));

        // block (if true)
        i.stmt.accept(this);
        // completed block, skip else
        write(Instruction.j(endLabel));

        // else condition (if false)
        if (i.elseStmt != null) {
            write(elseLabel + ":");
            // comment("else");
            i.elseStmt.accept(this);
        }

        write(endLabel + ":");
        comment("----------");

        freeRegister(r);
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        comment(a.toString());
        
        Register l = a.left.accept(this);
        Register r = a.right.accept(this);

        int size = a.left.type.size();
        // comment("size of %s = %d", a.left.type, size);

        if (!a.right.isImmediate) {
            comment("loaded value of right in assign %s", a.right);
            switch (size) {
                case 1: write(Instruction.lb(r, r)); break;
                case 4: write(Instruction.lw(r, r)); break;
                // no structs as it's never immediate
                default: break;
            }
            // write(Instruction.lw(r, r));
        }

        switch (size) {
            case 1: write(Instruction.sb(l, r)); break;
            case 4: write(Instruction.sw(l, r)); break;
        
            default: {
                // struct / array - needs to be copied over
                comment("copying struct over");
                // int leftIncrement = a.left.isGlobal ? 4 : -4;
                // comment("left: %s isglobal %s", a.left, a.left.isGlobal);
                // int rightIncrement = a.right.isGlobal ? 4 : -4;
                // comment("right: %s isglobal %s", a.right, a.right.isGlobal);
                // if (a.left.isGlobal) {
                //     write(Instruction.addi(dest, src, i));
                // }
                Register s = getRegister();
                for (int i = 0; i < a.left.type.size(); i += 4) {
                    write(Instruction.lw(s, r));
                    write(Instruction.sw(l, s));
                    
                    write(Instruction.addi(l, 4));
                    write(Instruction.addi(r, 4));
                }
                freeRegister(s);
            }; break;
        }

        nl();
        
        freeRegister(l);
        freeRegister(r);

        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        // TODO Auto-generated method stub
        comment("return (%s)", r.funReturnType);
        if (r.expr != null) {
            // expression evaluated and placed in a register
            Register rReg = r.expr.accept(this);
            // this is the address of the return of the function, so set $v0 to it
            write(Instruction.move(Register.v0, rReg));
            // if (r.expr.type.size() <= 4) {
            // } else {
            //     // pass on the stack

            // }
        } else {
            write(Instruction.la(Register.v0, Register.zero));
        }
        
        // return in main?
        if (!r.fd.name.equals("main")) {
            write(Instruction.jr(Register.ra));
        }
        return null;
    }
}
