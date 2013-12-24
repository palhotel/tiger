package ast.optimizations;

// Algebraic simplification optimizations on an AST.

public class AlgSimp implements ast.Visitor {
	private ast.classs.T newClass;
	private ast.mainClass.T mainClass;
	public ast.program.T program;

	boolean isNum, isId, isBool, booleanValue; // 判断exp类型
	int num;
	java.util.Hashtable<String, Integer> iLocals; // 存储整数赋值的局部变量
	java.util.Hashtable<String, Boolean> bLocals;

	public AlgSimp() {
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
		boolean left = false;
		isId = false;
		int temp = 0;
		e.left.accept(this);
		if (isId && iLocals.containsKey(((ast.exp.Id) e.left).id)) {
			e.left = new ast.exp.Num(num, e.lineNum);
			left = true;
			temp = num;
		} else if (isNum) {
			temp = num;
			left = true;
		}
		isId = false;
		e.right.accept(this);
		if (isId || isNum) {
			if (isId && iLocals.containsKey(((ast.exp.Id) e.right).id)) {
				e.right = new ast.exp.Num(num, e.lineNum);
			}
			if (left)
				num += temp;
			else
				isNum = false;
		} else
			isNum = false;
		isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		boolean left = false;
		isId = false;
		int temp = 0;
		e.left.accept(this);
		if (isId && iLocals.containsKey(((ast.exp.Id) e.left).id)) {
			e.left = new ast.exp.Num(num, e.lineNum);
			left = true;
			temp = num;
		} else if (isNum) {
			temp = num;
			left = true;
		}
		isId = false;
		e.right.accept(this);
		if (isId || isNum) {
			if (isId && iLocals.containsKey(((ast.exp.Id) e.right).id)) {
				e.right = new ast.exp.Num(num, e.lineNum);
			}
			if (left)
				num -= temp;
			else
				isNum = false;
		} else
			isNum = false;
		isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		boolean left = false;
		isId = false;
		int temp = 0;
		e.left.accept(this);
		if (isId && iLocals.containsKey(((ast.exp.Id) e.left).id)) {
			e.left = new ast.exp.Num(num, e.lineNum);
			left = true;
			temp = num;
		} else if (isNum) {
			temp = num;
			left = true;
		}
		isId = false;
		e.right.accept(this);
		if (isId || isNum) {
			if (isId && iLocals.containsKey(((ast.exp.Id) e.right).id)) {
				e.right = new ast.exp.Num(num, e.lineNum);
			}
			if (left)
				num *= temp;
			else
				isNum = false;
		} else
			isNum = false;
		isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.And e) {
		isBool = false;
		e.left.accept(this);
		boolean temp = booleanValue;
		e.right.accept(this);
		if(isBool)
			booleanValue &= temp;
		isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		e.index.accept(this);
		return;
	}

	@Override
	public void visit(ast.exp.Call e) {
		e.exp.accept(this);
		if (e.args != null)
			for (ast.exp.T arg : e.args) {
				arg.accept(this);
			}
		this.isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		isBool = true;
		booleanValue = true;
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
		isBool = true;
		booleanValue = false;
		return;
	}

	@Override
	public void visit(ast.exp.Id e) {
		this.isId = true;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		return;
	}

	@Override
	public void visit(ast.exp.Lt e) {
		isNum = false;
		e.left.accept(this);
		if(isNum)
			e.left = new ast.exp.Num(num,e.lineNum);
		isNum = false;
		e.right.accept(this);
		if(isNum)
			e.right = new ast.exp.Num(num,e.lineNum);
		isId = false;
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		return;
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		isBool = false;
		e.exp.accept(this);
		if(isBool)
			booleanValue = !booleanValue;
		return;
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.num = e.num;
		this.isNum = true;
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		return;
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		this.isNum = this.isBool = false;
		
		s.exp.accept(this);
		if (isNum)
		{
			iLocals.put(s.id, num);
			s.exp = new ast.exp.Num(num,s.lineNum);
		}
		else {
			if (iLocals.containsKey(s.id))
				iLocals.remove(s.id);
			if(isBool)
			{
				bLocals.put(s.id, booleanValue);
				if(booleanValue)
					s.exp = new ast.exp.True(s.lineNum);
				else
					s.exp = new ast.exp.False(s.lineNum);
			}
			else if (bLocals.containsKey(s.id))
					bLocals.remove(s.id);
		}
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
		isBool = isId = false;
		s.condition.accept(this);
		if(isBool)
		{
			if(booleanValue)
				s.condition = new ast.exp.True(s.lineNum);
			else
				s.condition = new ast.exp.False(s.lineNum);
		}
		if(isId && bLocals.contains(((ast.exp.Id)s.condition).id))
		{
			if(bLocals.get(((ast.exp.Id)s.condition).id))
				s.condition = new ast.exp.True(s.lineNum);
			else
				s.condition = new ast.exp.False(s.lineNum);
		}
		s.thenn.accept(this);
		s.elsee.accept(this);
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		isBool = isId = false;
		s.condition.accept(this);
		if(isBool)
		{
			if(booleanValue)
				s.condition = new ast.exp.True(s.lineNum);
			else
				s.condition = new ast.exp.False(s.lineNum);
		}
		if(isId && bLocals.size() > 0 && bLocals.contains(((ast.exp.Id)s.condition).id))
		{
			if(bLocals.get(((ast.exp.Id)s.condition).id))
				s.condition = new ast.exp.True(s.lineNum);
			else
				s.condition = new ast.exp.False(s.lineNum);
		}
		s.body.accept(this);
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
		this.iLocals = new java.util.Hashtable<String, Integer>();
		this.bLocals = new java.util.Hashtable<String, Boolean>();
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
		this.program = p;
		ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
		if (control.Control.trace.contains("ast.AlgSimp")) {
			System.out.println("before optimization:");
			p.accept(pp);
		}

		mainClass = (ast.mainClass.MainClass) p.mainClass;
		mainClass.accept(this);
		for (ast.classs.T c : p.classes) {
			newClass = (ast.classs.Class) c;
			newClass.accept(this);
		}

		if (control.Control.trace.contains("ast.AlgSimp")) {
			System.out.println("after optimization:");
			this.program.accept(pp);
		}
		return;
	}
}
