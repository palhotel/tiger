package elaborator;

public class MethodTable {
	private java.util.Hashtable<String, ast.type.T> table, unused;

	public MethodTable() {
		this.table = new java.util.Hashtable<String, ast.type.T>();
	}

	// Duplication is not allowed
	@SuppressWarnings("unchecked")
	public void put(java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals) {
		for (ast.dec.T dec : formals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated parameter: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
		}

		for (ast.dec.T dec : locals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated variable: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
		}
		unused = (java.util.Hashtable<String, ast.type.T>)table.clone();
	}

	// return null for non-existing keys
	public ast.type.T get(String id) {
		if(unused == null)
			return null;
		if ( unused.containsKey(id)) {
			unused.remove(id);
		}
		return this.table.get(id);
	}

	public void printUnused()
	{
		if(unused != null && unused.keySet().size() > 0)
		{
			for (String str : unused.keySet()) {
				System.out.println("Warning: " + str + " has not been used.");
			}
		}
	}
	
	public void dump() {
		for (String str : table.keySet()) {
			System.out.println(str + "    " + table.get(str));
		}
	}

	public void Clear() {
		table.clear();
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
