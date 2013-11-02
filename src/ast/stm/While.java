package ast.stm;

public class While extends T
{
  public ast.exp.T condition;
  public Block body;

  public While(ast.exp.T condition, Block body, int lineNum)
  {
    this.condition = condition;
    this.body = body;
    this.lineNum = lineNum;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
