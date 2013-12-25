package cfg.optimizations;

import java.util.HashSet;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.Length;
import cfg.stm.MoveArray;
import cfg.stm.NewArray;
import cfg.stm.Not;

public class LivenessVisitor implements cfg.Visitor {
	// gen, kill for one statement
	private java.util.HashSet<String> oneStmGen;
	private java.util.HashSet<String> oneStmKill;

	// gen, kill for one transfer
	private java.util.HashSet<String> oneTransferGen;
	private java.util.HashSet<String> oneTransferKill;

	// gen, kill for statements
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmGen;
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmKill;

	// gen, kill for transfers
	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferGen;
	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferKill;

	// gen, kill for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockGen;
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockKill;

	// liveIn, liveOut for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveIn;
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveOut;

	// liveIn, liveOut for statements
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

	// liveIn, liveOut for transfer
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

	// As you will walk the tree for many times, so
	// it will be useful to recored which is which:
	enum Liveness_Kind_t {
		None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
	}

	private Liveness_Kind_t kind = Liveness_Kind_t.None;

	public LivenessVisitor() {
		this.oneStmGen = new java.util.HashSet<>();
		this.oneStmKill = new java.util.HashSet<>();

		this.oneTransferGen = new java.util.HashSet<>();
		this.oneTransferKill = new java.util.HashSet<>();

		this.stmGen = new java.util.HashMap<>();
		this.stmKill = new java.util.HashMap<>();

		this.transferGen = new java.util.HashMap<>();
		this.transferKill = new java.util.HashMap<>();

		this.blockGen = new java.util.HashMap<>();
		this.blockKill = new java.util.HashMap<>();

		this.blockLiveIn = new java.util.HashMap<>();
		this.blockLiveOut = new java.util.HashMap<>();

		this.stmLiveIn = new java.util.HashMap<>();
		this.stmLiveOut = new java.util.HashMap<>();

		this.transferLiveIn = new java.util.HashMap<>();
		this.transferLiveOut = new java.util.HashMap<>();

		this.kind = Liveness_Kind_t.None;
	}

	// /////////////////////////////////////////////////////
	// utilities

	private java.util.HashSet<String> getOneStmGenAndClear() {
		java.util.HashSet<String> temp = this.oneStmGen;
		this.oneStmGen = new java.util.HashSet<>();
		return temp;
	}

	private java.util.HashSet<String> getOneStmKillAndClear() {
		java.util.HashSet<String> temp = this.oneStmKill;
		this.oneStmKill = new java.util.HashSet<>();
		return temp;
	}

	private java.util.HashSet<String> getOneTransferGenAndClear() {
		java.util.HashSet<String> temp = this.oneTransferGen;
		this.oneTransferGen = new java.util.HashSet<>();
		return temp;
	}

	private java.util.HashSet<String> getOneTransferKillAndClear() {
		java.util.HashSet<String> temp = this.oneTransferKill;
		this.oneTransferKill = new java.util.HashSet<>();
		return temp;
	}

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
		// this.oneStmGen.add(new Integer(operand.i).toString());
		return;
	}

	@Override
	public void visit(cfg.operand.Var operand) {
		this.oneStmGen.add(operand.id);
		return;
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
		return;
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		this.oneStmKill.add(s.dst);
		this.oneStmGen.add(s.obj);
		for (cfg.operand.T arg : s.args) {
			arg.accept(this);
		}
		return;
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
		return;
	}

	@Override
	public void visit(cfg.stm.Move s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.src.accept(this);
		return;
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		this.oneStmKill.add(s.dst);
		return;
	}

	@Override
	public void visit(cfg.stm.Print s) {
		s.arg.accept(this);
		return;
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
		return;
	}

	@Override
	public void visit(cfg.stm.Times s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
		return;
	}

	@Override
	public void visit(And m) {
		// kill
		this.oneStmKill.add(m.dst);
		// gen
		m.left.accept(this);
		m.right.accept(this);
		return;
	}

	@Override
	public void visit(Not m) {
		// kill
		this.oneStmKill.add(m.dst);
		// gen
		m.src.accept(this);
		return;
	}

	@Override
	public void visit(MoveArray m) {
		// gen
		m.index.accept(this);
		m.src.accept(this);
		return;
	}

	@Override
	public void visit(NewArray m) {
		this.oneStmKill.add(m.dst);
	}

	@Override
	public void visit(ArraySelect m) {
		// kill
		this.oneStmKill.add(m.dst);
		// gen
		m.index.accept(this);
		return;
	}

	@Override
	public void visit(Length m) {
		// kill
		this.oneStmKill.add(m.dst);
		// gen
		m.src.accept(this);
		return;
	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
		// Invariant: accept() of operand modifies "gen"
		s.operand.accept(this);
		return;
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
		return;
	}

	@Override
	public void visit(cfg.transfer.Return s) {
		// Invariant: accept() of operand modifies "gen"
		s.operand.accept(this);
		return;
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

	// utility functions:
	private void calculateStmTransferGenKill(cfg.block.Block b) {
		if (control.Control.isTracing("liveness.step1")) {
			System.out.print("\nblock " + b.label.toString());
		}
		for (cfg.stm.T s : b.stms) {
			this.oneStmGen = new java.util.HashSet<>();
			this.oneStmKill = new java.util.HashSet<>();
			s.accept(this);
			this.stmGen.put(s, this.oneStmGen);
			this.stmKill.put(s, this.oneStmKill);
			if (control.Control.isTracing("liveness.step1")) {
				System.out.print("\ngen, kill for statement:");
				s.toString();
				System.out.print("\ngen is:");
				for (String str : this.oneStmGen) {
					System.out.print(str + ", ");
				}
				System.out.print("\nkill is:");
				for (String str : this.oneStmKill) {
					System.out.print(str + ", ");
				}
			}
		}
		this.oneStmGen = new java.util.HashSet<>();
		this.oneStmKill = new java.util.HashSet<>();
		// this.oneTransferGen = new java.util.HashSet<>();
		// this.oneTransferKill = new java.util.HashSet<>();
		b.transfer.accept(this);
		this.transferGen.put(b.transfer, this.oneStmGen);
		this.transferKill.put(b.transfer, this.oneStmKill);
		if (control.Control.isTracing("liveness.step1")) {
			System.out.print("\ngen, kill for transfer:");
			b.toString();
			System.out.print("\ngen is:");
			for (String str : this.oneStmGen) {
				System.out.print(str + ", ");
			}
			System.out.println("\nkill is:");
			for (String str : this.oneStmKill) {
				System.out.print(str + ", ");
			}
		}
		return;
	}

	// block gen and kill
	void calculateBlockGenKill(cfg.block.Block b) {
		// gen[pn] = gen[n] U (gen[p] - kill[n])
		// kill[pn] = kill[p] U kill[n]

		java.util.HashSet<String> blockStmGen = new java.util.HashSet<>();
		java.util.HashSet<String> blockStmKill = new java.util.HashSet<>();

		// reverse, transfer first
		for (String str : this.transferKill.get(b.transfer)) {
			if (blockStmGen.contains(str))
				blockStmGen.remove(str);

			if (!blockStmKill.contains(str))
				blockStmKill.add(str);
		}

		for (String str : this.transferGen.get(b.transfer)) {
			if (!blockStmGen.contains(str))
				blockStmGen.add(str);
		}

		// stm reverse
		for (int i = b.stms.size() - 1; i >= 0; i--) {
			for (String str : this.stmKill.get(b.stms.get(i))) {
				if (blockStmGen.contains(str))
					blockStmGen.remove(str);

				if (!blockStmKill.contains(str))
					blockStmKill.add(str);
			}

			for (String str : this.stmGen.get(b.stms.get(i))) {
				if (!blockStmGen.contains(str))
					blockStmGen.add(str);
			}
		}

		// print
		if (control.Control.isTracing("liveness.step2")) {
			System.out.print("\nblock " + b.label.toString() + " gen is:");
			for (String str : blockStmGen) {
				System.out.print(str + ", ");
			}
			System.out.print("\nblock " + b.label.toString() + " kill is:");
			for (String str : blockStmKill) {
				System.out.print(str + ", ");
			}
		}

		// add to block gen and kill
		this.blockGen.put(b, blockStmGen);
		this.blockKill.put(b, blockStmKill);
	}

	java.util.HashSet<String> oneBlockLiveIn = new java.util.HashSet<String>();

	@SuppressWarnings("unchecked")
	void calculateBlockInOut(cfg.block.Block b) {
		// Step 3: calculate the "liveIn" and "liveOut" sets for each block
		// Note that to speed up the calculation, you should first
		// calculate a reverse topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		java.util.HashSet<String> newBlockLiveIn = (java.util.HashSet<String>) oneBlockLiveIn
				.clone();
		java.util.HashSet<String> newBlockLiveOut = (java.util.HashSet<String>) oneBlockLiveIn
				.clone();

		for (String str : this.blockKill.get(b))
			if (newBlockLiveIn.contains(str))
				newBlockLiveIn.add(str);
		for (String str : this.blockGen.get(b))
			if (!newBlockLiveIn.contains(str))
				newBlockLiveIn.add(str);

		if (control.Control.isTracing("liveness.step3")) {
			System.out.print("\nblock " + b.label.toString() + " live in is:");
			for (String str : newBlockLiveIn) {
				System.out.print(str + ", ");
			}
			System.out.print("\nblock " + b.label.toString() + " live out is:");
			for (String str : newBlockLiveOut) {
				System.out.print(str + ", ");
			}
		}

		oneBlockLiveIn = newBlockLiveIn;
		this.blockLiveIn.put(b, newBlockLiveIn);
		this.blockLiveOut.put(b, newBlockLiveOut);
	}

	@SuppressWarnings("unchecked")
	void calculateStmInOut(cfg.block.Block b) {
		// Step 4: calculate the "liveIn" and "liveOut" sets for each
		// statement and transfer
		// in[n] = gen[n] U (out[n] - kill[n])
		// out[n] = U in[s] s<succ[n]
		java.util.HashSet<String> newLiveOut = this.blockLiveOut.get(b);
		java.util.HashSet<String> newLiveIn = (HashSet<String>) this.blockLiveOut
				.get(b).clone();
		
		for (String str : this.transferGen.get(b.transfer))
			if (!newLiveIn.contains(str))
				newLiveIn.add(str);

		this.transferLiveIn.put(b.transfer, newLiveIn);
		this.transferLiveOut.put(b.transfer, newLiveOut);

		if (control.Control.isTracing("liveness.step4")) {
			System.out.print("\nblock " + b.label.toString()
					+ " transfer live in is:");
			for (String str : newLiveIn) {
				System.out.print(str + ", ");
			}
			System.out.print("\nblock " + b.label.toString()
					+ " transfer live out is:");
			for (String str : newLiveOut) {
				System.out.print(str + ", ");
			}
		}

		for (int i = b.stms.size() - 1; i >= 0; i--) {
			newLiveOut = newLiveIn;
			this.transferLiveIn = new java.util.HashMap<>();
			this.transferLiveOut = new java.util.HashMap<>();

			for (String str : this.stmKill.get(b.stms.get(i)))
				if (newLiveIn.contains(str))
					newLiveIn.add(str);
			for (String str : this.stmGen.get(b.stms.get(i)))
				if (!newLiveIn.contains(str))
					newLiveIn.add(str);

			this.stmLiveIn.put(b.stms.get(i), newLiveIn);
			this.stmLiveOut.put(b.stms.get(i), newLiveOut);

			if (control.Control.isTracing("liveness.step4")) {
				System.out.print("\nblock " + b.label.toString()
						+ " stm"+ i +" live in is:");
				for (String str : newLiveIn) {
					System.out.print(str + ", ");
				}
				System.out.print("\nblock " + b.label.toString()
						+ " stm"+ i +" live out is:");
				for (String str : newLiveOut) {
					System.out.print(str + ", ");
				}
			}
		}
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		switch (this.kind) {
		case StmGenKill:
			calculateStmTransferGenKill(b);
			break;
		case BlockGenKill:
			calculateBlockGenKill(b);
			break;
		case BlockInOut:
			calculateBlockInOut(b);
			break;
		case StmInOut:
			calculateStmInOut(b);
			break;
		default:
			// Your code here:
			return;
		}
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		// Four steps:
		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmGenKill;
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block in a reverse order.
		// Your code here:

		this.kind = Liveness_Kind_t.BlockGenKill;
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}

		// Step 3: calculate the "liveIn" and "liveOut" sets for each block
		// Note that to speed up the calculation, you should first
		// calculate a reverse topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		// Your code here:

		this.kind = Liveness_Kind_t.BlockInOut;
		for (int i = m.blocks.size() - 1; i >= 0; i--) {
			m.blocks.get(i).accept(this);
		}

		// Step 4: calculate the "liveIn" and "liveOut" sets for each
		// statement and transfer
		// Your code here:
		this.kind = Liveness_Kind_t.StmInOut;
		for (int i = m.blocks.size() - 1; i >= 0; i--) {
			m.blocks.get(i).accept(this);
		}
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		// Four steps:
		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmGenKill;
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block in a reverse order.
		// Your code here:
		this.kind = Liveness_Kind_t.BlockGenKill;
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}

		// Step 3: calculate the "liveIn" and "liveOut" sets for each block
		// Note that to speed up the calculation, you should first
		// calculate a reverse topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		// Your code here:
		this.kind = Liveness_Kind_t.BlockInOut;
		for (int i = m.blocks.size() - 1; i >= 0; i--) {
			m.blocks.get(i).accept(this);
		}

		// Step 4: calculate the "liveIn" and "liveOut" sets for each
		// statement and transfer
		// Your code here:
		this.kind = Liveness_Kind_t.StmInOut;
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
		return;
	}

}
