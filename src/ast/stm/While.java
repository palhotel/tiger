package ast.stm;

public class While extends T
{
  public ast.exp.T condition;
  public Block body;

  public While(ast.exp.T condition, Block body)
  {
    this.condition = condition;
    this.body = body;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
