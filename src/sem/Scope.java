package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable;
	
	public Scope(Scope outer) { 
		this.outer = outer;
		this.symbolTable = new HashMap<String,Symbol>();
	}
	
	public Scope() { this(null); }
	
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
}
