package gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ast.FunDecl;

/**
 * LibFuncs
 */
public abstract class LibFunc {

    // private static HashMap<String, LibFunc> libFuncs = new HashMap<String, LibFunc>() {{
    //     put("print_i", print_i.funcall());
    //     put("print_c", print_c.funcall());
    //     put("print_s", print_s.funcall());
    //     put("read_i", read_i.funcall());
    //     put("read_c", read_c.funcall());
    //     put("mcmalloc", mcmalloc.funcall());
    // }};
    private static ArrayList<String> libFuncs = new ArrayList<String>() {{
        add("print_i");
        add("print_c");
        add("print_s");
        add("read_i");
        add("read_c");
        add("mcmalloc");
    }};

    public static boolean isLibFunc(String f) {
        return libFuncs.contains(f);
    }

    // public class print_i extends LibFunc {

    //     public String funcall() {

    //     }
    // }

    public static String print_i() {
        String var = "%i";
        return 
        "    .macro print_i (" + var + ")\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 1) +
        Instruction.la(Register.paramRegs[0], var, true) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String print_c() {
        String var = "%c";
        return 
        "    .macro print_c (" + var + ")\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 11) +
        Instruction.la(Register.paramRegs[0], var, true) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String print_s() {
        String var = "%s"; //register
        return 
        "    .macro print_s (" + var + ")\n" +
        // "    .data\n" +
        // "string_out: .asciiz " + var + "\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 4) +
        Instruction.la(Register.paramRegs[0], var, true) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String read_i() {
        return 
        "    .macro read_i\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 5) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String read_c() {
        return 
        "    .macro read_c\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 12) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String mcmalloc() {
        String var = "%m";
        return 
        "    .macro mcmalloc (" + var + ")\n" +
        "    .text\n" +
        Instruction.li(Register.v0, 9) +
        Instruction.la(Register.paramRegs[0], var, true) +
        Instruction.syscall() +
        "    .end_macro\n";
    }

    public static String load_str_lit() {
        String r = "%r";
        String var = "%s";
        return
        "    .macro load_str_lit (" + r + "," + var + ")\n" +
        "    .data\n" +
        "string: .asciiz " + var + "\n" +
        "    .text\n" +
        "    la " + r + ", string\n" +
        // Instruction.la(r, "string") +
        "    .end_macro\n";
    }

    public static String printSysFuncs() {
        return 
        print_i() + "\n" +
        print_c() + "\n" +
        print_s() + "\n" +
        read_i() + "\n" +
        read_c() + "\n" +
        mcmalloc() + "\n" +
        load_str_lit() + "\n" +
        "";
    }

    // public final String name;
    // public final List<String> body;

    // public LibFunc(String name, List<String> body) {
    //     this.name = name;
    //     this.body = body;
    // }
    
    // public static String writeMacro(String name, String logic) {
    //     String macro = String.format(".macro %s", name);
    // }
}