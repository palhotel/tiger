package ast.optimizations;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor {
	private ast.classs.Class newClass;
	private ast.mainClass.MainClass mainClass;
	private ast.method.Method newMethod;
	public ast.program.T program;

	ast.type.T type; // type of the expression being elaborated
	boolean isDeadCode = true; // 判断for或while的判断条件是否是恒真或恒假
	boolean isTrue = true; // 记录恒定的判断条件
	int compareValue; // 保存判断条件中比较的数字

	public DeadCode() {
		this.newClass = null;
		this.mainClass = null;
		this.program = null;
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
		int temp = 0;
		if (isDeadCode)
			temp = compareValue;
		e.right.accept(this);
		if (isDeadCode)
			compareValue += temp;
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		int temp = 0;
		if (isDeadCode)
			temp = compareValue;
		e.right.accept(this);
		if (isDeadCode)
			compareValue -= temp;
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		int temp = 0;
		if (isDeadCode)
			temp = compareValue;
		e.right.accept(this);
		if (isDeadCode)
			compareValue *= temp;
		return;
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		boolean temp = true;
		if (isDeadCode)
			temp = isTrue;
		e.right.accept(this);
		if (isDeadCode)
			isTrue &= temp;
		return;
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		e.index.accept(this);
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.Call e) {
		e.exp.accept(this);
		if (e.args != null)
			for (ast.exp.T arg : e.args) {
				arg.accept(this);
			}
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		this.isTrue = true;
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
		this.isTrue = false;
		return;
	}

	@Override
	public void visit(ast.exp.Id e) {
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		int temp = 0;
		if (isDeadCode)
			temp = this.compareValue;
		e.right.accept(this);
		if (isDeadCode)
			this.isTrue = temp < this.compareValue;
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		this.isTrue = !isTrue;
		return;
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.compareValue = e.num;
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		return;
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		return;
	}

	@Override
	public void visit(ast.stm.Block s) {
		return;
	}

	@Override
	public void visit(ast.stm.If s) {
		this.isDeadCode = true;
		s.condition.accept(this);

		if (this.isDeadCode) {
			if (this.isTrue) {
				newMethod.stms.set(newMethod.stms.indexOf(s), s.thenn);
			} else
				newMethod.stms.set(newMethod.stms.indexOf(s), s.elsee);
		} else {
			s.thenn.accept(this);
			s.elsee.accept(this);
		}
		this.isDeadCode = false;
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		this.isDeadCode = true;
		s.condition.accept(this);
		if (this.isDeadCode) {
			if (this.isTrue) {
				System.out.println("at line " + s.lineNum
						+ " : while true, dead Loop.");
				System.exit(0);
			}
			else
				newMethod.stms.remove(s);
		} 
		return;
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
		return;
	}

	@Override
	public void visit(ast.type.Class t) {
		return;
	}

	@Override
	public void visit(ast.type.Int t) {
		return;
	}

	@Override
	public void visit(ast.type.IntArray t) {
		return;
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		return;
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		this.newMethod = m;
		for (ast.stm.T s : m.stms)
			s.accept(this);
		m.retExp.accept(this);
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		for (ast.method.T method : c.methods) {
			method.accept(this);
		}
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		c.stm.accept(this);
		return;
	}

	// program
	@Override
	public void visit(ast.program.Program p) {
		// You should comment out this line of code:
		
		ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
		if (control.Control.trace.contains("ast.DeadCode")) {
			System.out.println("before optimization:");
			p.accept(pp);
		}
		mainClass = (ast.mainClass.MainClass) p.mainClass;
		mainClass.accept(this);
		java.util.LinkedList<ast.classs.T> classes = new java.util.LinkedList<ast.classs.T>();
		for (ast.classs.T c : p.classes) {
			newClass = (ast.classs.Class) c;
			newClass.accept(this);
			classes.add(newClass);
		}
		// this.program = p;
		this.program = new ast.program.Program(mainClass, classes);
		if (control.Control.trace.contains("ast.DeadCode")) {
			System.out.println("after optimization:");
			this.program.accept(pp);
		}
		return;
	}
}
