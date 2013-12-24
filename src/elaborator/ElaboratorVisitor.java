package elaborator;

public class ElaboratorVisitor implements ast.Visitor {
	public ClassTable classTable; // symbol table for class
	public MethodTable methodTable; // symbol table for each method
	public String currentClass; // the class name being elaborated
	public ast.type.T type; // type of the expression being elaborated

	public ElaboratorVisitor() {
		this.classTable = new ClassTable();
		this.methodTable = new MethodTable();
		this.currentClass = null;
		this.type = null;
	}

	private void error(String msg) {
		System.out.println("type mismatch: " + msg);
		return;
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error("line:" + e.lineNum + " 'add' left type not equal to right");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals("@boolean")
				|| !leftty.toString().equals("@boolean"))
			error("line:"
					+ e.lineNum
					+ " '&&' left type and right type should be both is boolean");
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]"))
			error("line:" + e.lineNum + " arraySelcet type should be int[]");
		e.index.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line:" + e.lineNum + " array index type should be int");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Call e) {
		ast.type.T leftty;
		ast.type.Class ty = null;

		e.exp.accept(this);
		leftty = this.type;
		if (leftty instanceof ast.type.Class) {
			ty = (ast.type.Class) leftty;
			e.type = ty.id;
		} else
			error("line:" + e.lineNum
					+ " method call object type should be a class");
		MethodType mty = this.classTable.getm(ty.id, e.id);
		java.util.LinkedList<ast.type.T> declaredArgTypes = new java.util.LinkedList<ast.type.T>();
		for (ast.dec.T dec : mty.argsType) {
			declaredArgTypes.add(((ast.dec.Dec) dec).type);
		}
		java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();
		if (e.args != null)
			for (ast.exp.T a : e.args) {
				a.accept(this);
				argsty.addLast(this.type);
			}
		if (declaredArgTypes.size() != argsty.size())
			error("");
		// For now, the following code only checks that
		// the types for actual and formal arguments should
		// be the same. However, in MiniJava, the actual type
		// of the parameter can also be a subtype (sub-class) of the
		// formal type. That is, one can pass an object of type "A"
		// to a method expecting a type "B", whenever type "A" is
		// a sub-class of type "B".
		// Modify the following code accordingly:
		for (int i = 0; i < argsty.size(); i++) {
			if (declaredArgTypes.toString().equals(argsty.get(i).toString()))
				;
			else
				error("");
		}
		this.type = mty.retType;
		// the following two types should be the declared types.
		e.at = declaredArgTypes;
		e.rt = this.type;
		return;
		/*
		 * MethodType mty = this.classTable.getm(ty.id, e.id);
		 * java.util.LinkedList<ast.type.T> argsty = new
		 * java.util.LinkedList<ast.type.T>(); if (e.args != null &&
		 * e.args.size() != 0) { for (ast.exp.T a : e.args) { a.accept(this);
		 * argsty.addLast(this.type); } } if (mty.argsType.size() !=
		 * argsty.size()) error("line:" + e.lineNum +
		 * " method call parameters' count have not matched"); for (int i = 0; i
		 * < argsty.size(); i++) { ast.dec.Dec dec = (ast.dec.Dec)
		 * mty.argsType.get(i); if
		 * (!dec.type.toString().equals(argsty.get(i).toString())) { String
		 * classname = argsty.get(i).toString(); while (true) { if
		 * (dec.type.toString().equals(classname)) break; classname =
		 * this.classTable.get(argsty.get(i).toString()).extendss; if (classname
		 * == null) error("line:" + e.lineNum +
		 * " method call parameters' type have not matched"); } } } this.type =
		 * mty.retType; e.at = argsty; e.rt = this.type; return;
		 */}

	@Override
	public void visit(ast.exp.False e) {
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.Id e) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(e.id);
		// if search failed, then s.id must be a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, e.id);
			// mark this id as a field id, this fact will be
			// useful in later phase.
			e.isField = true;
		}
		if (type == null)
			error("line:" + e.lineNum + " id " + e.id + " has not defined");
		this.type = type;
		// record this type on this node for future use.
		e.type = type;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]"))
			error("line:" + e.lineNum + " length type should be int[]");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		ast.type.T ty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(ty.toString()))
			error("line:" + e.lineNum
					+ " '<' both left and right type should be boolean");
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line:" + e.lineNum + " IntArray index type should be int");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.type = new ast.type.Class(e.id);
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("line:" + e.lineNum + " 'not' type should be boolean");
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error("line:" + e.lineNum
					+ " '-' left and right type should be int");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		this.type = new ast.type.Class(this.currentClass);
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error("line:" + e.lineNum
					+ " '*' left and right type should be int.");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		this.type = new ast.type.Boolean();
		return;
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(s.id);
		// if search failed, then s.id must
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		if (type == null)
			error("line:" + s.lineNum + " '=' left type not defined.");
		s.exp.accept(this);
		s.type = type;
		this.type.toString().equals(type.toString());
		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		ast.type.T type = this.methodTable.get(s.id);
		// if search failed, then s.id must
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		if (type == null)
			error("line:" + s.lineNum + " '=' left type not defined.");

		if (!type.toString().equals("@int[]"))
			error("line:" + s.lineNum + " '=' left type should be int[].");
		s.index.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line:" + s.lineNum + " '=' right array index should be int");
		s.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line:" + s.lineNum + " '=' right array should be int[]");
		return;
	}

	@Override
	public void visit(ast.stm.Block s) {
		for (ast.stm.T t : s.stms)
			t.accept(this);
		return;
	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("line:" + s.lineNum + " if condition type should be boolean.");
		s.thenn.accept(this);
		s.elsee.accept(this);
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line:" + s.lineNum + " print method parameter should be int");
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("line:" + s.lineNum
					+ " while condition type should be boolean");
		s.body.accept(this);
		return;
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.type.Class t) {
		this.type = new ast.type.Class(t.id);
		return;
	}

	@Override
	public void visit(ast.type.Int t) {
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.type.IntArray t) {
		this.type = new ast.type.IntArray();
		return;
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		if (this.classTable.get(this.currentClass, d.id) != null)
			error("line:" + d.lineNum + " " + d.id + "has been defined.");
		if (this.methodTable.get(d.id) != null)
			error("line:" + d.lineNum + " " + d.id + "has been defined.");
		return;
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		// construct the method table
		this.methodTable.Clear();
		this.methodTable.put(m.formals, m.locals);

		if (control.Control.elabMethodTable)
			this.methodTable.dump();

		for (ast.stm.T s : m.stms)
			s.accept(this);
		m.retExp.accept(this);

		methodTable.printUnused();
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		this.currentClass = c.id;

		for (ast.method.T m : c.methods) {
			m.accept(this);
		}
		return;
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		this.currentClass = c.id;
		// "main" has an argument "arg" of type "String[]", but
		// one has no chance to use it. So it's safe to skip it...

		c.stm.accept(this);
		return;
	}

	// ////////////////////////////////////////////////////////
	// step 1: build class table
	// class table for Main class
	private void buildMainClass(ast.mainClass.MainClass main) {
		this.classTable.put(main.id, new ClassBinding(null));
	}

	// class table for normal classes
	private void buildClass(ast.classs.Class c) {
		this.classTable.put(c.id, new ClassBinding(c.extendss));
		for (ast.dec.T dec : c.decs) {
			ast.dec.Dec d = (ast.dec.Dec) dec;
			this.classTable.put(c.id, d.id, d.type);
		}
		for (ast.method.T method : c.methods) {
			ast.method.Method m = (ast.method.Method) method;
			this.classTable.put(c.id, m.id,
					new MethodType(m.retType, m.formals));
		}
	}

	// step 1: end
	// ///////////////////////////////////////////////////

	// program
	@Override
	public void visit(ast.program.Program p) {
		// ////////////////////////////////////////////////
		// step 1: build a symbol table for class (the class table)
		// a class table is a mapping from class names to class bindings
		// classTable: className -> ClassBinding{extends, fields, methods}
		buildMainClass((ast.mainClass.MainClass) p.mainClass);
		for (ast.classs.T c : p.classes) {
			buildClass((ast.classs.Class) c);
		}

		// we can double check that the class table is OK!
		if (control.Control.elabClassTable) {
			this.classTable.dump();
		}

		classTable.printUnused();
		// ////////////////////////////////////////////////
		// step 2: elaborate each class in turn, under the class table
		// built above.
		p.mainClass.accept(this);
		for (ast.classs.T c : p.classes) {
			c.accept(this);
		}

	}
}
