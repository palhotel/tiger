package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.Length;
import cfg.stm.MoveArray;
import cfg.stm.NewArray;
import cfg.stm.Not;

public class DeadCode implements cfg.Visitor
{
  public cfg.program.T program;
  
  public DeadCode()
  {
    this.program = null;
  } 

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(cfg.operand.Int operand)
  {
  }

  @Override
  public void visit(cfg.operand.Var operand)
  {
  }

  // statements
  @Override
  public void visit(cfg.stm.Add s)
  {
  }

  @Override
  public void visit(cfg.stm.InvokeVirtual s)
  {
  }

  @Override
  public void visit(cfg.stm.Lt s)
  {
  }

  @Override
  public void visit(cfg.stm.Move s)
  {
  }

  @Override
  public void visit(cfg.stm.NewObject s)
  {
  }

  @Override
  public void visit(cfg.stm.Print s)
  {
  }

  @Override
  public void visit(cfg.stm.Sub s)
  {
  }

  @Override
  public void visit(cfg.stm.Times s)
  {
  }

  // transfer
  @Override
  public void visit(cfg.transfer.If s)
  {
  }

  @Override
  public void visit(cfg.transfer.Goto s)
  {
  }

  @Override
  public void visit(cfg.transfer.Return s)
  {
  }

  // type
  @Override
  public void visit(cfg.type.Class t)
  {
  }

  @Override
  public void visit(cfg.type.Int t)
  {
  }

  @Override
  public void visit(cfg.type.IntArray t)
  {
  }

  // dec
  @Override
  public void visit(cfg.dec.Dec d)
  {
  }

  // block
  @Override
  public void visit(cfg.block.Block b)
  {
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
  }

  // vtables
  @Override
  public void visit(cfg.vtable.Vtable v)
  {
  }

  // class
  @Override
  public void visit(cfg.classs.Class c)
  {
  }

  // program
  @Override
  public void visit(cfg.program.Program p)
  {
    this.program = p;
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

}
