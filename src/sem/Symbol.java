package sem;

public abstract class Symbol {
	public String name;
	
	public Symbol(String name) {
		this.name = name;
	}

	public boolean isVar() { return false; };
	public boolean isFun() { return false; };
	public boolean isStruct() { return false; };

}
