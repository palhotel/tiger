package elaborator;

public class ClassTable {
	// map each class name (a string), to the class bindings.
	private java.util.Hashtable<String, ClassBinding> table, unused;

	public ClassTable() {
		this.table = new java.util.Hashtable<String, ClassBinding>();
	}

	// Duplication is not allowed
	@SuppressWarnings("unchecked")
	public void put(String c, ClassBinding cb) {
		if (this.table.get(c) != null) {
			System.out.println("duplicated class: " + c);
			System.exit(1);
		}
		this.table.put(c, cb); 
		unused = (java.util.Hashtable<String, ClassBinding>)table.clone();
	}

	// put a field into this table
	// Duplication is not allowed
	@SuppressWarnings("unchecked")
	public void put(String c, String id, ast.type.T type) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type);
		unused = (java.util.Hashtable<String, ClassBinding>)table.clone();
		return;
	}

	// put a method into this table
	// Duplication is not allowed.
	// Also note that MiniJava does NOT allow overloading.
	@SuppressWarnings("unchecked")
	public void put(String c, String id, MethodType type) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type);
		unused = (java.util.Hashtable<String, ClassBinding>)table.clone();
		return;
	}

	// return null for non-existing class
	public ClassBinding get(String className) {
		return this.table.get(className);
	}

	// get type of some field
	// return null for non-existing field.
	public ast.type.T get(String className, String xid) {
		ClassBinding cb = this.table.get(className);
		ClassBinding un = this.unused.get(className);
		ast.type.T type = cb.fields.get(xid);
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;
			
			cb = this.table.get(cb.extendss);
			un = this.unused.get(un.extendss);
			type = cb.fields.get(xid);
		}
		if(un.fields.containsKey(xid)){
			un.fields.remove(xid);
		}
		return type;
	}
	
	public void printUnused()
	{
		for(String cls : unused.keySet())
		{
			for(String f : unused.get(cls).fields.keySet())
				System.out.println("warning: in class " + cls +" identify " + f + "has not been used.");
		}
	}

	// get type of some method
	// return null for non-existing method
	public MethodType getm(String className, String mid) {
		ClassBinding cb = this.table.get(className);
		MethodType type = cb.methods.get(mid);
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;

			cb = this.table.get(cb.extendss);
			type = cb.methods.get(mid);
		}
		return type;
	}

	public void dump() {
		for (String str : table.keySet()) {
			System.out.println(str);
			for(String f : table.get(str).fields.keySet())
				System.out.println(f + "\t" +table.get(str).fields.get(f));
			for(String m : table.get(str).methods.keySet())
				System.out.println(m + "\t" +  table.get(str).methods.get(m));
		}
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
