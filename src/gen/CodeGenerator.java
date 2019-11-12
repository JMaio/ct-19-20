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

    private Stack<Integer> stackOffsets = new Stack<Integer>();

    // 'total' stack offset
    int stackOffset = 0;
    // offset from last stack offset,
    // added to stackOffset and reset when entering a new block
    int frameOffset = 0;

    UIDLabel uidLabel = new UIDLabel();

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

        // stackOffsets.push(stackOffset);
        stackOffset += frameOffset;
        frameOffset = 0;
        
        for (VarDecl vd : b.vds) {
            vd.accept(this);
        }

        for (Stmt s : b.stmts) {
            s.accept(this);
        }
        
        // stackOffset = stackOffsets.pop();

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
            comment(vd.toString());
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

            comment("stack space for '%s' => %d bytes", vd.name, size);
            write(Instruction.incrementSp(-size));
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
        // use argument registers. TODO: use the stack?

        comment("%s ()", fce.name);

        int returnSize = fce.type.size();

        if (returnSize > 0) {
            write(Instruction.incrementSp(fce.type.size()));
        }

        if (LibFunc.isLibFunc(fce.name)) {
            // String i = "    " + fce.name;
            // String delimiter = "";
            // System.out.println("function: " +fce.name);
            ArrayList<String> args = new ArrayList<>();

            if (fce.args.size() > 0) {
                // i += "(";
                for (Expr arg : fce.args) {
                    Register r = arg.accept(this);
                    if (!arg.isImmediate) {
                        write(Instruction.lw(r, r));
                    }
                    args.add(r.toString());
                }
                // i += ")";
            }
            
            write(Instruction.InstrFmt("%s (%s)", fce.name, String.join(", ", args)));
        } else {
            // TODO
        }
        
        // TODO free temporary registers?
        freeAllRegisters();
        
        nl();
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
        Register r = aae.array.accept(this);


        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        // TODO Auto-generated method stub
        Register r = fae.struct.accept(this);

        // a.b.c
        StructType st = (StructType) fae.struct.type;
        write(Instruction.addi(r, r, st.getFieldOffset(fae.field)));
        // VarExpr inner = ((VarExpr) fae.getInnermost()).vd;

        // r = fae.getInnermost().accept(this);
        // // if (inner.global) {

        // // }
        // write(Instruction.la(r, Register.fp, inner.offset - fae.totalOffset));
        return r;
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
        // TODO: assign goes to local or global variable?
        comment(a.toString());
        
        // TODO: account for arrays and pointers, not just vars
        Register r = a.right.accept(this);
        Register l = a.left.accept(this);

        if (!a.right.isImmediate) {
            write(Instruction.lw(r, r));
        }

        write(Instruction.sw(l, r));

        nl();

        // if (Expr.isVarExpr(a.left)) {
        //     // get variable address to write to
        //     VarDecl vd = ((VarExpr) a.left).vd;
        //     if (vd.global) {
        //         write(Instruction.sw(r, vd.name));
        //     } else {
        //         int offset = vd.offset;
        //         comment("store %s at (%d)", vd.name, offset);
        //         write(Instruction.sw(Register.fp, r, offset));
        //     }

        // } else if (Expr.isFieldAccessExpr(a.left)) {
            
        //     FieldAccessExpr fae = (FieldAccessExpr) a.left;
        //     comment("%s offset = %d", fae.struct.type, fae.totalOffset);

        //     VarDecl vd = ((VarExpr) a.left.getInnermost()).vd;
        //     if (vd.global) {
        //         write(Instruction.sw(r, vd.name, fae.totalOffset));
        //     } else {
        //         int offset = vd.offset;
        //         comment("store %s at (%d)", vd.name, offset);
        //         write(Instruction.sw(Register.fp, r, offset + fae.totalOffset));
        //     }


        //     // StructType st = (StructType) fae.struct.type;
        //     // int offset = 0;

        //     // Expr inner = fae.getInnermost();
        //     // // should be a var expression with vardecl
        //     // try {
        //     //     VarExpr innerVarExpr = ((VarExpr) inner);
        //     //     // offset each struct
        //     //     // a.b.c
        //     //     //                    #      c    d    z
        //     //     // a: struct          # 4  ---- ---- ----               
        //     //     // b: field (struct)  #    ---- ----
        //     //     //  c: field (int)    #    ----
                
        //     //     // a: {b: struct, z: int}
        //     //     // b: {c: int, d: int}

        //     //     Expr outer = fae.struct;
        //     //     offset += st.getFieldOffset(fae.field);

        //     //     // add offsets from various structs
        //     //     while (!Expr.isVarExpr(outer)) {
                    
        //     //         // off(a)[point] = off(p)[point] - size[pair] + off(a)[pair]
        //     //         // offset += st.getFieldOffset(outer.field) - ().size() + ;
                    
        //     //         // a.x.y.z
                    
        //     //         // (StructType) fae.struct.type
        //     //         outer = outer.getInner();
        //     //     }

        //     //     if (innerVarExpr.vd.global) {
        //     //         comment("struct '%s' (global)", innerVarExpr.name);
        //     //         comment("field '%s' offset=%d", fae.field, offset);
        //     //         comment("fae total offset=%d", fae.totalOffset);
        //     //         write(Instruction.sw(r, innerVarExpr.vd.name));
        //     //     } else {
        //     //         comment("struct '%s' (local)", innerVarExpr.name);

        //     //     }

        //     // } catch (Exception e) {
        //     //     //TODO: handle exception
        //     // }
        //     // comment("field access offset: %d (field %s)", st.getFieldOffset(fae.field), fae.field);

        // } else if (Expr.isArrayAccessExpr(a.left)) {

        // } else {
            
        // }

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
