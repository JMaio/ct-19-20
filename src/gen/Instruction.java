package gen;

/**
 * Instruction
 */
public class Instruction {

    public static String InstrFmt(String fmt, Object ...args) {
        return String.format("    " + fmt + "\n", args);
    }
    

    public static String add(Register dest, Register s, Register t) {
        return InstrFmt("add  %s, %s, %s", dest, s, t);
    }
    
    public static String _div(Register s, Register t) {
        return InstrFmt("div  %s, %s", s, t);
    }

    public static String div(Register dest, Register s, Register t) {
        return _div(s, t) +
        InstrFmt("mflo %s", dest);
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
    
    public static String la(Register r, Register address) {
        return InstrFmt("la   %s, (%s)", r, address);
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

    public static String mod(Register dest, Register s, Register t) {
        return _div(s, t) +
        InstrFmt("mfhi %s", dest);
    }
        
    public static String move(Register dest, Register src) {
        return InstrFmt("move %s, %s", dest, src);
    }
        
    public static String mul(Register dest, Register s, Register t) {
        return InstrFmt("mul  %s, %s, %s", dest, s, t);
    }

    public static String sub(Register dest, Register s, Register t) {
        return InstrFmt("sub  %s, %s, %s", dest, s, t);
    }
    
    public static String syscall() {
        return InstrFmt("syscall");
    }

}