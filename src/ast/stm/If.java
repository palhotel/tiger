package ast.stm;

public class If extends T
{
  public ast.exp.T condition;
  public Block thenn;
  public Block elsee;

  public If(ast.exp.T condition, Block thenn, Block elsee, int lineNum)
  {
    this.condition = condition;
    this.thenn = thenn;
    this.elsee = elsee;
    this.lineNum = lineNum;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
