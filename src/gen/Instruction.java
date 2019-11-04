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
        // return String.format("la %s, %s", r, address);
    }

    public static String la(Register r, Register address) {
        return InstrFmt("la   %s, (%s)", r, address);
        // return String.format("la %s, %s", r, address);
    }

    public static String li(Register r, int val) {
        return InstrFmt("li   %s, %s", r, val);
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
    
    public static String syscall() {
        return InstrFmt("syscall");
    }

}