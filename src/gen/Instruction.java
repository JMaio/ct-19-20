package gen;

/**
 * Instruction
 */
public class Instruction {

    public static String InstrFmt(String fmt, Object ...args) {
        return String.format("    " + fmt, args);
    }
    

    public static String add(Register dest, Register s, Register t) {
        return InstrFmt("add  %s, %s, %s", dest, s, t);
    }
    
    public static String addi(Register r, int i) {
        return addi(r, r, i);
    }
    
    public static String addi(Register dest, Register src, int i) {
        return InstrFmt("addi %s, %s, %d", dest, src, i);
    }
    
    public static String beq(Register s, Register t, String label) {
        return InstrFmt("beq  %s, %s, %s", s, t, label);
    }

    public static String beq(Register r, int i, String label) {
        return InstrFmt("beq  %s, %d, %s", r, i, label);
    }

    // division (integer)
    public static String div(Register dest, Register s, Register t) {
        return InstrFmt("div  %s, %s, %s", dest, s, t);
    }
    
    // modulo
    public static String rem(Register dest, Register s, Register t) {
        return InstrFmt("rem  %s, %s, %s", dest, s, t);
    }


    public static String j(String label) {
        return InstrFmt("j    %s", label);
    }
    
    public static String jal(String label) {
        return InstrFmt("jal  %s", label);
    }
    
    public static String jr(Register r) {
        return InstrFmt("jr   %s", r);
    }
    
    public static String la(Register r, Register address, int offset) {
        return InstrFmt("la   %s, %d(%s)", r, offset, address);
    }

    public static String la(Register r, Register address) {
        return la(r, address, 0);
    }

    public static String la(Register r, String label) {
        return la(r, label, false);
    }

    public static String la(Register r, String label, boolean macro) {
        if (macro) {
            return InstrFmt("la   %s, (%s)", r, label);
        } else {
            return InstrFmt("la   %s, %s", r, label);
        }
    }

    public static String li(Register r, int val) {
        return InstrFmt("li   %s, %s", r, val);
    }
    
    public static String lw(Register r, String label) {
        return InstrFmt("lw   %s, %s", r, label);
    }

    public static String lw(Register r, Register address, int offset) {
        return InstrFmt("lw   %s, %d(%s)", r, offset, address);
    }
    
    public static String lw(Register r, Register address) {
        return lw(r, address, 0);
    }

    
    public static String lb(Register r, String label) {
        return InstrFmt("lb   %s, %s", r, label);
    }

    public static String lb(Register r, Register address, int offset) {
        return InstrFmt("lb   %s, %d(%s)", r, offset, address);
    }
    
    public static String lb(Register r, Register address) {
        return lb(r, address, 0);
    }


    public static String move(Register dest, Register src) {
        return InstrFmt("move %s, %s", dest, src);
    }
        
    
    public static String mul(Register dest, Register s, Register t) {
        return InstrFmt("mul  %s, %s, %s", dest, s, t);
    }

    public static String mulo(Register dest, Register src, int i) {
        return InstrFmt("mulo %s, %s, %s", dest, src, i);
    }



    // set equal
    public static String seq(Register t1, Register t2, Register t3) {
        return InstrFmt("seq  %s, %s, %s", t1, t2, t3);
    }
    // set equal
    public static String seq(Register t1, Register t2, int i) {
        return InstrFmt("seq  %s, %s, %s", t1, t2, i);
    }

    // set not equal
    public static String sne(Register t1, Register t2, Register t3) {
        return InstrFmt("sne  %s, %s, %s", t1, t2, t3);
    }
    
    // set not equal
    public static String sne(Register s, Register t, int i) {
        return InstrFmt("sne  %s, %s, %d", s, t, i);
    }

    // set less than
    public static String slt(Register t1, Register t2, Register t3) {
        return InstrFmt("slt  %s, %s, %s", t1, t2, t3);
    }
    
    // set less than or eq
    public static String sle(Register t1, Register t2, Register t3) {
        return InstrFmt("sle  %s, %s, %s", t1, t2, t3);
    }
    
    // set greater than
    public static String sgt(Register t1, Register t2, Register t3) {
        return InstrFmt("sgt  %s, %s, %s", t1, t2, t3);
    }
    
    // set greater than or eq
    public static String sge(Register t1, Register t2, Register t3) {
        return InstrFmt("sge  %s, %s, %s", t1, t2, t3);
    }



    // subtract
    public static String sub(Register dest, Register s, Register t) {
        return InstrFmt("sub  %s, %s, %s", dest, s, t);
    }

    
    public static String sb(Register dest, Register src, int offset) {
        return InstrFmt("sb   %s, %d(%s)", src, offset, dest);
    }

    public static String sb(Register dest, Register src) {
        return sb(dest, src, 0);
    }

    public static String sb(Register src, String label, Register offset) {
        return InstrFmt("sb   %s, %s(%s)", src, label, offset);
    }
    
    public static String sb(Register src, String label, int offset) {
        return InstrFmt("sb   %s, %s+%d", src, label, offset);
    }

    public static String sb(Register src, String label) {
        return sb(src, label, 0);
    }


    public static String sw(Register dest, Register src, int offset) {
        return InstrFmt("sw   %s, %d(%s)", src, offset, dest);
    }

    public static String sw(Register dest, Register src) {
        return sw(dest, src, 0);
    }
    
    public static String sw(Register src, String label, Register offset) {
        return InstrFmt("sw   %s, %s(%s)", src, label, offset);
    }
    
    public static String sw(Register src, String label, int offset) {
        return InstrFmt("sw   %s, %s+%d", src, label, offset);
    }

    public static String sw(Register src, String label) {
        return sw(src, label, 0);
    }
    
    public static String syscall() {
        return InstrFmt("syscall");
    }


    public static String incrementSp(int size) {
        return addi(Register.sp, Register.sp, -size);
    }

    public static String decrementSp(int size) {
        return incrementSp(-size);
    }
    
    
    public static String incrementFp(int size) {
        return addi(Register.fp, Register.fp, -size);
    }

    public static String decrementFp(int size) {
        return incrementSp(-size);
    }

}