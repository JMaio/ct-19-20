package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	public final String namespace;
	private Map<String, Symbol> symbolTable;
	
	public Scope(Scope outer, String namespace) { 
		this.outer = outer;
		this.namespace = namespace;

		this.symbolTable = new HashMap<String,Symbol>();
	}
	
	public Scope() { this(null, "global scope"); }
	
	public Symbol lookup(String name) {
		Symbol s = lookupCurrent(name);
		if (s == null && this.outer != null) {
			s = this.outer.lookup(name);
		}
		return s;
	}
	
	public Symbol lookupCurrent(String name) {
		return this.symbolTable.get(name);
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}

	public Scope getOuter() {
		return this.outer;
	}
}
