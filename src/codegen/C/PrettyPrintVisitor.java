package codegen.C;

import control.Control;

public class PrettyPrintVisitor implements Visitor {
	private int indentLevel;
	private java.io.BufferedWriter writer;
	java.util.Hashtable<String, String> fieldsBitVector = new java.util.Hashtable<String, String>();

	public PrettyPrintVisitor() {
		this.indentLevel = 2;
	}

	private void indent() {
		this.indentLevel += 2;
	}

	private void unIndent() {
		this.indentLevel -= 2;
	}

	private void printSpaces() {
		int i = this.indentLevel;
		while (i-- != 0)
			this.say(" ");
	}

	private void sayln(String s) {
		say(s);
		try {
			this.writer.write("\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void say(String s) {
		try {
			this.writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(codegen.C.exp.Add e) {
		e.left.accept(this);
		this.say(" + ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.And e) {
		e.left.accept(this);
		this.say(" && ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.ArraySelect e) {
		e.array.accept(this);
		this.say(" [");
		e.index.accept(this);
		this.say("+4]");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Call e) {
		this.say("(frame." + e.assign + "=");
		e.exp.accept(this);
		this.say(", ");
		this.say("frame." + e.assign + "->vptr->" + e.id + "(frame." + e.assign);
		int size = e.args.size();
		if (size == 0) {
			this.say("))");
			return;
		}
		for (codegen.C.exp.T x : e.args) {
			this.say(", ");
			x.accept(this);
		}
		this.say("))");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Id e) {
		if (e.isInt) {
			if (e.isField == true)
				this.say("this->" + e.id);
			else
				this.say(e.id);
		} else {
			if (e.isField == true)
				this.say("this->" + e.id);
			else
				this.say("frame." + e.id);
			/*
			 * this.say("this->frame." + e.id); else this.say("frame." + e.id);
			 */
		}
	}

	// weiwancheng
	@Override
	public void visit(codegen.C.exp.Length e) {
		e.array.accept(this);
		say("[2]");
	}

	@Override
	public void visit(codegen.C.exp.Lt e) {
		e.left.accept(this);
		this.say(" < ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.NewIntArray e) {
		this.say("(int*)Tiger_new_array(");
		e.exp.accept(this);
		this.say(")");
	}

	// /////////////////////////////////////////////////////////////////////////excise
	// 7
	@Override
	public void visit(codegen.C.exp.NewObject e) {
		this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id
				+ "_vtable_, sizeof(struct " + e.id + "))))");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Not e) {
		this.say("!(");
		e.exp.accept(this);
		this.say(")");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Num e) {
		this.say(Integer.toString(e.num));
		return;
	}

	@Override
	public void visit(codegen.C.exp.Sub e) {
		e.left.accept(this);
		this.say(" - ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.This e) {
		this.say("this");
	}

	@Override
	public void visit(codegen.C.exp.Times e) {
		e.left.accept(this);
		this.say(" * ");
		e.right.accept(this);
		return;
	}

	// statements
	@Override
	public void visit(codegen.C.stm.Assign s) {
		this.printSpaces();
		if (s.isField == true)
			this.say("this->" + s.id + " = ");
		else if (s.isInt) {
			this.say(s.id + " = ");
		} else {
			this.say("frame." + s.id + " = ");
		}
		s.exp.accept(this);
		this.sayln(";");
		return;
	}

	@Override
	public void visit(codegen.C.stm.AssignArray s) {
		this.printSpaces();
		if (s.isField == true)
			this.say("this->" + s.id + "[");
		else
			this.say("frame." + s.id + "[");
		s.index.accept(this);
		this.say("+4] = ");
		s.exp.accept(this);
		this.sayln(";");
		return;
	}

	@Override
	public void visit(codegen.C.stm.Block s) {
		this.printSpaces();
		this.sayln("{");
		this.indent();
		for (codegen.C.stm.T m : s.stms)
			m.accept(this);
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	@Override
	public void visit(codegen.C.stm.If s) {
		this.printSpaces();
		this.say("if (");
		s.condition.accept(this);
		this.sayln(")");
		s.thenn.accept(this);
		this.printSpaces();
		this.sayln("else");
		s.elsee.accept(this);
		this.sayln("");
		return;
	}

	@Override
	public void visit(codegen.C.stm.Print s) {
		this.printSpaces();
		this.say("System_out_println (");
		s.exp.accept(this);
		this.sayln(");");
		return;
	}

	@Override
	public void visit(codegen.C.stm.While s) {
		this.printSpaces();
		this.say("while (");
		s.condition.accept(this);
		this.sayln(")");
		s.body.accept(this);
		this.sayln("");
		return;
	}

	// type
	@Override
	public void visit(codegen.C.type.Class t) {
		this.say("struct " + t.id + " *");
	}

	@Override
	public void visit(codegen.C.type.Int t) {
		this.say("int");
	}

	@Override
	public void visit(codegen.C.type.IntArray t) {
		this.say("int *");
	}

	// dec
	@Override
	public void visit(codegen.C.dec.Dec d) {
		this.printSpaces();
		d.type.accept(this);
		say(" " + d.id + ";");
		return;
	}

	// method
	@Override
	public void visit(codegen.C.method.Method m) {

		String arguments_gc_map = "";
		int locals_gc_count = 0;

		for (codegen.C.dec.T d : m.formals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			if (dec.type.toString() == "@int") {
				arguments_gc_map += "0";
			} else {
				arguments_gc_map += "1";
			}
		}

		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			if (dec.type.toString() != "@int") {
				locals_gc_count++;
			}
		}

		this.sayln("char* " + m.classId + "_" + m.id + "_arguments_gc_map = \""
				+ arguments_gc_map + "\";");
		this.sayln("int " + m.classId + "_" + m.id + "_locals_gc_count = "
				+ locals_gc_count + ";");

		this.sayln("struct " + m.classId + "_" + m.id + "_gc_frame{");
		indent();
		this.sayln("void *prev;");
		this.sayln("char *arguments_gc_map;");
		this.sayln("int *arguments_base_address;");
		this.sayln("int locals_gc_count;");
		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			if (dec.type.toString() == "@int") {
				// this.sayln("int "+dec.id);
			} else if (dec.type.toString() == "@int[]") {
				this.sayln("int *" + dec.id + ";");
			} else {
				this.sayln("struct " + dec.type + " *" + dec.id + ";");
			}
		}
		unIndent();
		this.sayln("};");

		m.retType.accept(this);
		this.say(" " + m.classId + "_" + m.id + "(");
		int size = m.formals.size();
		for (codegen.C.dec.T d : m.formals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			size--;
			dec.type.accept(this);
			this.say(" " + dec.id);
			if (size > 0)
				this.say(", ");
		}
		this.sayln(")");
		this.sayln("{");
		indent();

		this.sayln("struct " + m.classId + "_" + m.id + "_gc_frame frame;");
		this.sayln("frame.prev = prev;");
		this.sayln("prev = &frame;");
		this.sayln("frame.arguments_gc_map = " + m.classId + "_" + m.id
				+ "_arguments_gc_map;");
		this.sayln("frame.arguments_base_address = (int*)&this;");
		this.sayln("frame.locals_gc_count = " + m.classId + "_" + m.id
				+ "_locals_gc_count;");

		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			// dec.type.accept(this);
			// this.sayln(" " + dec.id + ";");

			if (dec.type.toString() == "@int") {
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
				// this.sayln("frame."+dec.id + " = "+dec.id+";");
			} // else {
				// this.sayln("frame."+dec.id + " = &"+dec.id+";");
				// }

		}
		this.sayln("");
		for (codegen.C.stm.T s : m.stms)
			s.accept(this);

		m.retType.accept(this);
		this.say(" returnResult = ");
		m.retExp.accept(this);
		this.sayln(";");

		this.sayln("prev = frame.prev;");
		// this.sayln("frame=0;");

		this.sayln("return returnResult;");
		// m.retExp.accept(this);
		// this.sayln(";");
		unIndent();
		this.sayln("}");
		return;
	}

	@Override
	public void visit(codegen.C.mainMethod.MainMethod m) {
		this.sayln("//Main's Frame");
		this.sayln("struct " + "main_gc_frame{");
		indent();
		this.sayln("void *prev;");
		this.sayln("char *arguments_gc_map;");
		this.sayln("int *arguments_base_address;");
		this.sayln("int locals_gc_count;");
		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			if (dec.type.toString() == "@int") {
				// this.sayln("int "+dec.id);
			} else if (dec.type.toString() == "@int[]") {
				this.sayln("int *" + dec.id + ";");
			} else {
				this.sayln("struct " + dec.type + " *" + dec.id + ";");
			}
		}
		unIndent();
		this.sayln("};");
		// Main method
		this.sayln("int Tiger_main ()");
		this.sayln("{");
		//
		indent();
		this.sayln("struct " + "main_gc_frame frame;");
		this.sayln("frame.prev = prev;");
		this.sayln("prev = &frame;");
		this.sayln("frame.arguments_gc_map = " + "0;");
		this.sayln("frame.arguments_base_address = 0;");

		int locals_gc_count = 0;
		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			if (dec.type.toString() != "@int") {
				locals_gc_count++;
			}
		}
		this.sayln("frame.locals_gc_count = " + locals_gc_count + ";");

		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			// dec.type.accept(this);
			// this.sayln(" " + dec.id + ";");

			if (dec.type.toString() == "@int") {
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}

		}
		m.stm.accept(this);

		this.sayln("prev = frame.prev;");
		unIndent();
		//
		/*
		 * for (codegen.C.dec.T dec : m.locals) { this.say("  ");
		 * codegen.C.dec.Dec d = (codegen.C.dec.Dec) dec; d.type.accept(this);
		 * this.say(" "); this.sayln(d.id + ";"); } m.stm.accept(this);
		 */
		this.sayln("}\n");
		return;
	}

	// vtables
	@Override
	public void visit(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable");
		this.sayln("{");
		this.sayln("char* " + v.id + "_gc_map;");
		for (codegen.C.Ftuple t : v.ms) {
			this.say("  ");
			t.ret.accept(this);
			this.sayln(" (*" + t.id + ")();");
		}
		this.sayln("};\n");
		return;
	}

	private void outputVtable(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
		this.sayln("{");
		this.sayln("\"" + this.fieldsBitVector.get(v.id) + "\",");
		for (codegen.C.Ftuple t : v.ms) {
			this.say("  ");
			this.sayln(t.classs + "_" + t.id + ",");
		}
		this.sayln("};\n");
		return;
	}

	// class
	@Override
	public void visit(codegen.C.classs.Class c) {
		String str = "";
		this.sayln("struct " + c.id);
		this.sayln("{");
		this.sayln("  struct " + c.id + "_vtable *vptr;");
		//new obj-model,add 3 fields
		this.sayln("  int mem_isobj;");
		this.sayln("  int mem_size;");
		this.sayln("  void* mem_forwarding;");
		for (codegen.C.Tuple t : c.decs) {
			this.say("  ");
			t.type.accept(this);
			if (t.type.toString().equals("@int"))
				str += "0";
			else {
				str += "1";
			}
			this.say(" ");
			this.sayln(t.id + ";");
		}
		this.fieldsBitVector.put(c.id, str);
		this.sayln("};");
		return;
	}

	// program
	@Override
	public void visit(codegen.C.program.Program p) {
		// we'd like to output to a file, rather than the "stdout".
		try {
			String outputName = null;
			if (Control.outputName != null)
				outputName = Control.outputName;
			else if (Control.fileName != null)
				outputName = Control.fileName + ".c";
			else
				outputName = "a.c";

			this.writer = new java.io.BufferedWriter(
					new java.io.OutputStreamWriter(
							new java.io.FileOutputStream(outputName)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		this.sayln("// This is automatically generated by the Tiger compiler.");
		this.sayln("// Do NOT modify!\n");
		// global
		this.sayln("//Global vars");
		this.sayln("extern void* prev;");

		this.sayln("// structures");
		for (codegen.C.classs.T c : p.classes) {
			c.accept(this);
		}

		this.sayln("// vtables structures");
		for (codegen.C.vtable.T v : p.vtables) {
			v.accept(this);
		}
		this.sayln("");

		this.sayln("//only methods declaration");
		for (codegen.C.method.T mm : p.methods) {
			codegen.C.method.Method m = (codegen.C.method.Method) mm;
			m.retType.accept(this);
			this.say(" " + m.classId + "_" + m.id + "(");
			int size = m.formals.size();
			for (codegen.C.dec.T d : m.formals) {
				codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
				size--;
				dec.type.accept(this);
				this.say(" " + dec.id);
				if (size > 0)
					this.say(", ");
			}
			this.sayln(");");
		}
		this.sayln("");

		this.sayln("// vtables");
		for (codegen.C.vtable.T v : p.vtables) {
			outputVtable((codegen.C.vtable.Vtable) v);
		}
		this.sayln("");

		this.sayln("// methods");
		for (codegen.C.method.T m : p.methods) {
			m.accept(this);
		}
		this.sayln("");

		this.sayln("// main method");
		p.mainMethod.accept(this);
		this.sayln("");

		this.say("\n\n");

		try {
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
