package codegen.C;

// Given a Java ast, translate it into a C ast and outputs it.

public class TranslateVisitor implements ast.Visitor {
	private ClassTable table;
	private String classId;
	private codegen.C.type.T type; // type after translation
	private codegen.C.dec.T dec;
	private codegen.C.stm.T stm;
	private codegen.C.exp.T exp;
	private codegen.C.method.T method;
	private java.util.LinkedList<codegen.C.dec.T> tmpVars;
	private java.util.LinkedList<codegen.C.classs.T> classes;
	private java.util.LinkedList<codegen.C.vtable.T> vtables;
	private java.util.LinkedList<codegen.C.method.T> methods;
	private codegen.C.mainMethod.T mainMethod;
	private java.util.LinkedList<codegen.C.dec.T> fieldVars;
	java.util.LinkedList<codegen.C.dec.T> locals;
	java.util.LinkedList<codegen.C.dec.T> newFormals;
	public codegen.C.program.T program;

	public TranslateVisitor() {
		this.table = new ClassTable();
		this.classId = null;
		this.type = null;
		this.dec = null;
		this.stm = null;
		this.exp = null;
		this.method = null;
		this.classes = new java.util.LinkedList<codegen.C.classs.T>();
		this.vtables = new java.util.LinkedList<codegen.C.vtable.T>();
		this.methods = new java.util.LinkedList<codegen.C.method.T>();
		this.mainMethod = null;
		this.program = null;
		this.fieldVars = null;
		this.locals = null;
		this.newFormals = null;
	}

	// //////////////////////////////////////////////////////
	//
	public String genId() {
		return util.Temp.next();
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		codegen.C.exp.T left = this.exp;
		e.right.accept(this);
		this.exp = new codegen.C.exp.Add(left, this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		codegen.C.exp.T left = this.exp;
		e.right.accept(this);
		this.exp = new codegen.C.exp.And(left, this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		codegen.C.exp.T array = this.exp;
		e.index.accept(this);
		this.exp = new codegen.C.exp.ArraySelect(array, this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.Call e) {
		e.exp.accept(this);
		String newid = this.genId();
		this.tmpVars.add(new codegen.C.dec.Dec(
				new codegen.C.type.Class(e.type), newid));
		codegen.C.exp.T exp = this.exp;
		java.util.LinkedList<codegen.C.exp.T> args = new java.util.LinkedList<codegen.C.exp.T>();
		if (e.args != null && (!e.args.isEmpty())) {
			for (ast.exp.T x : e.args) {
				x.accept(this);
				args.add(this.exp);
			}
		}

		this.exp = new codegen.C.exp.Call(newid, exp, e.id, args);
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
		this.exp = new codegen.C.exp.Num(0);
		return;
	}

	@Override
	public void visit(ast.exp.Id e) {
		codegen.C.exp.Id eee = new codegen.C.exp.Id(e.id);

		for (codegen.C.dec.T dec : this.locals) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(e.id)) {
				if (((codegen.C.dec.Dec) dec).type.toString().equals("@int"))
					eee.isInt = true;
				else
					eee.isInt = false;
			}
		}
		for (codegen.C.dec.T dec : this.newFormals) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(e.id)) {
				eee.isInt = true;
			}
		}

		for (codegen.C.dec.T dec : this.fieldVars) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(e.id)) {
				if (((codegen.C.dec.Dec) dec).type.toString().equals("@int"))
					eee.isInt = true;
				else
					eee.isInt = false;
			}
		}

		eee.isField = e.isField;
		this.exp = eee;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		this.exp = new codegen.C.exp.Length(this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		codegen.C.exp.T left = this.exp;
		e.right.accept(this);
		codegen.C.exp.T right = this.exp;
		this.exp = new codegen.C.exp.Lt(left, right);
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		this.exp = new codegen.C.exp.NewIntArray(this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.exp = new codegen.C.exp.NewObject(e.id);
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		this.exp = new codegen.C.exp.Not(this.exp);
		return;
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.exp = new codegen.C.exp.Num(e.num);
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		codegen.C.exp.T left = this.exp;
		e.right.accept(this);
		codegen.C.exp.T right = this.exp;
		this.exp = new codegen.C.exp.Sub(left, right);
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		this.exp = new codegen.C.exp.This();
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		codegen.C.exp.T left = this.exp;
		e.right.accept(this);
		codegen.C.exp.T right = this.exp;
		this.exp = new codegen.C.exp.Times(left, right);
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		this.exp = new codegen.C.exp.Num(1);
		return;
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		s.exp.accept(this);

		codegen.C.stm.Assign stmm = new codegen.C.stm.Assign(s.id, this.exp);
		for (codegen.C.dec.T dec : this.locals) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(s.id)) {
				if (((codegen.C.dec.Dec) dec).type.toString().equals("@int"))
					stmm.isInt = true;
				else
					stmm.isInt = false;
			}
		}
		for (codegen.C.dec.T dec : this.newFormals) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(s.id)) {
				stmm.isInt = true;
			}
		}

		for (codegen.C.dec.T dec : this.fieldVars) {
			if (dec != null && ((codegen.C.dec.Dec) dec).id.equals(s.id)) {
				stmm.isField = true;
				if (((codegen.C.dec.Dec) dec).type.toString().equals("@int"))
					stmm.isInt = true;
				else
					stmm.isInt = false;
			}
		}

		this.stm = stmm;
		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		s.index.accept(this);
		codegen.C.exp.T index = this.exp;
		s.exp.accept(this);
		codegen.C.stm.AssignArray stmm = new codegen.C.stm.AssignArray(s.id,
				index, this.exp);
		for (codegen.C.dec.T decc : this.fieldVars) {
			if (decc == null)
				continue;
			if (((codegen.C.dec.Dec) decc).id.equals(s.id)) {
				stmm.isField = true;
				break;
			}
		}
		this.stm = stmm;
		return;
	}

	@Override
	public void visit(ast.stm.Block s) {
		java.util.LinkedList<codegen.C.stm.T> newStm = new java.util.LinkedList<codegen.C.stm.T>();
		for (ast.stm.T t : s.stms) {
			t.accept(this);
			newStm.add(this.stm);
		}
		this.stm = new codegen.C.stm.Block(newStm);
		return;
	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		codegen.C.exp.T condition = this.exp;
		s.thenn.accept(this);
		codegen.C.stm.T thenn = this.stm;
		s.elsee.accept(this);
		codegen.C.stm.T elsee = this.stm;
		this.stm = new codegen.C.stm.If(condition, thenn, elsee);
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		this.stm = new codegen.C.stm.Print(this.exp);
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		codegen.C.exp.T condition = this.exp;
		s.body.accept(this);
		codegen.C.stm.T body = this.stm;
		this.stm = new codegen.C.stm.While(condition, body);
		return;
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
		this.type = new codegen.C.type.Int();
	}

	@Override
	public void visit(ast.type.Class t) {
		this.type = new codegen.C.type.Class(t.id);
	}

	@Override
	public void visit(ast.type.Int t) {
		this.type = new codegen.C.type.Int();
	}

	@Override
	public void visit(ast.type.IntArray t) {
		this.type = new codegen.C.type.IntArray();
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		d.type.accept(this);
		this.dec = new codegen.C.dec.Dec(this.type, d.id);
		return;
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		this.tmpVars = new java.util.LinkedList<codegen.C.dec.T>();
		m.retType.accept(this);
		codegen.C.type.T newRetType = this.type;
		newFormals = new java.util.LinkedList<codegen.C.dec.T>();
		newFormals.add(new codegen.C.dec.Dec(new codegen.C.type.Class(
				this.classId), "this"));
		for (ast.dec.T d : m.formals) {
			d.accept(this);
			newFormals.add(this.dec);
		}
		locals = new java.util.LinkedList<codegen.C.dec.T>();
		for (ast.dec.T d : m.locals) {
			d.accept(this);
			locals.add(this.dec);
		}
		java.util.LinkedList<codegen.C.stm.T> newStm = new java.util.LinkedList<codegen.C.stm.T>();
		for (ast.stm.T s : m.stms) {
			s.accept(this);
			newStm.add(this.stm);
		}
		m.retExp.accept(this);
		codegen.C.exp.T retExp = this.exp;
		for (codegen.C.dec.T dec : this.tmpVars) {
			locals.add(dec);
		}
		this.method = new codegen.C.method.Method(newRetType, this.classId,
				m.id, newFormals, locals, newStm, retExp);
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		ClassBinding cb = this.table.get(c.id);
		this.classes.add(new codegen.C.classs.Class(c.id, cb.fields));
		this.vtables.add(new codegen.C.vtable.Vtable(c.id, cb.methods));
		this.classId = c.id;
		for (ast.method.T m : c.methods) {
			m.accept(this);
			this.methods.add(this.method);
		}
		return;
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		ClassBinding cb = this.table.get(c.id);
		codegen.C.classs.T newc = new codegen.C.classs.Class(c.id, cb.fields);
		this.classes.add(newc);
		this.vtables.add(new codegen.C.vtable.Vtable(c.id, cb.methods));

		this.tmpVars = new java.util.LinkedList<codegen.C.dec.T>();

		c.stm.accept(this);
		codegen.C.mainMethod.T mthd = new codegen.C.mainMethod.MainMethod(
				this.tmpVars, this.stm);
		this.mainMethod = mthd;
		return;
	}

	// /////////////////////////////////////////////////////
	// the first pass
	public void scanMain(ast.mainClass.T m) {
		this.table.init(((ast.mainClass.MainClass) m).id, null);
		// this is a special hacking in that we don't want to
		// enter "main" into the table.
		return;
	}

	public void scanClasses(java.util.LinkedList<ast.classs.T> cs) {
		// field linkedlist
		this.fieldVars = new java.util.LinkedList<codegen.C.dec.T>();
		// put empty chuncks into the table
		for (ast.classs.T c : cs) {
			ast.classs.Class cc = (ast.classs.Class) c;
			this.table.init(cc.id, cc.extendss);
		}

		// put class fields and methods into the table
		for (ast.classs.T c : cs) {
			ast.classs.Class cc = (ast.classs.Class) c;
			java.util.LinkedList<codegen.C.dec.T> newDecs = new java.util.LinkedList<codegen.C.dec.T>();
			for (ast.dec.T dec : cc.decs) {
				dec.accept(this);
				this.fieldVars.add(this.dec);
				newDecs.add(this.dec);
			}
			this.table.initDecs(cc.id, newDecs);

			// all methods
			java.util.LinkedList<ast.method.T> methods = cc.methods;
			for (ast.method.T mthd : methods) {
				ast.method.Method m = (ast.method.Method) mthd;
				java.util.LinkedList<codegen.C.dec.T> newArgs = new java.util.LinkedList<codegen.C.dec.T>();
				for (ast.dec.T arg : m.formals) {
					arg.accept(this);
					newArgs.add(this.dec);
				}
				m.retType.accept(this);
				codegen.C.type.T newRet = this.type;
				this.table.initMethod(cc.id, newRet, newArgs, m.id);
			}
		}

		// calculate all inheritance information
		for (ast.classs.T c : cs) {
			ast.classs.Class cc = (ast.classs.Class) c;
			this.table.inherit(cc.id);
		}
	}

	public void scanProgram(ast.program.T p) {
		ast.program.Program pp = (ast.program.Program) p;
		scanMain(pp.mainClass);
		scanClasses(pp.classes);
		return;
	}

	// end of the first pass
	// ////////////////////////////////////////////////////

	// program
	@Override
	public void visit(ast.program.Program p) {
		// The first pass is to scan the whole program "p", and
		// to collect all information of inheritance.
		scanProgram(p);

		// do translations
		p.mainClass.accept(this);
		for (ast.classs.T classs : p.classes) {
			classs.accept(this);
		}
		this.program = new codegen.C.program.Program(this.classes,
				this.vtables, this.methods, this.mainMethod);
		return;
	}
}
