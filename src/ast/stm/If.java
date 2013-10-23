package ast.stm;

public class If extends T
{
  public ast.exp.T condition;
  public Block thenn;
  public Block elsee;

  public If(ast.exp.T condition, Block thenn, Block elsee)
  {
    this.condition = condition;
    this.thenn = thenn;
    this.elsee = elsee;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
