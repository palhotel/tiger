package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.Length;
import cfg.stm.MoveArray;
import cfg.stm.NewArray;
import cfg.stm.Not;

public class DeadCode implements cfg.Visitor {
	public cfg.program.T program;
	// liveIn, liveOut for statements
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

	// liveIn, liveOut for transfer
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

	public DeadCode() {
		this.program = null;

		this.stmLiveIn = new java.util.HashMap<>();
		this.stmLiveOut = new java.util.HashMap<>();

		this.transferLiveIn = new java.util.HashMap<>();
		this.transferLiveOut = new java.util.HashMap<>();

	}

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
	}

	@Override
	public void visit(cfg.operand.Var operand) {
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
	}

	@Override
	public void visit(cfg.stm.Lt s) {
	}

	@Override
	public void visit(cfg.stm.Move s) {
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
	}

	@Override
	public void visit(cfg.stm.Print s) {
	}

	@Override
	public void visit(cfg.stm.Sub s) {
	}

	@Override
	public void visit(cfg.stm.Times s) {
	}

	@Override
	public void visit(And m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Not m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MoveArray m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewArray m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ArraySelect m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Length m) {
		// TODO Auto-generated method stub

	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
	}

	@Override
	public void visit(cfg.transfer.Return s) {
	}

	// type
	@Override
	public void visit(cfg.type.Class t) {
	}

	@Override
	public void visit(cfg.type.Int t) {
	}

	@Override
	public void visit(cfg.type.IntArray t) {
	}

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		for (int i = b.stms.size() - 1; i >= 0; i--) {
			java.util.HashSet<String> stmOut = this.stmLiveOut.get(b.stms
					.get(i));

			if (!stmOut.contains(b.stms.get(i).dst)) {
				// delete this stm
				try {
					b.stms.remove(b.stms.get(i));
				} catch (Exception e) {
					System.out.print("error");
				}
			}
		}
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		for (int i = m.blocks.size() - 1; i >= 0; i--) {
			m.blocks.get(i).accept(this);
		}
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		for (int i = m.blocks.size() - 1; i >= 0; i--) {
			m.blocks.get(i).accept(this);
		}
	}

	// vtables
	@Override
	public void visit(cfg.vtable.Vtable v) {
	}

	// class
	@Override
	public void visit(cfg.classs.Class c) {
	}

	// program
	@Override
	public void visit(cfg.program.Program p) {
		p.mainMethod.accept(this);
		for (cfg.method.T mth : p.methods) {
			mth.accept(this);
		}
		this.program = p;

		return;
	}

}
