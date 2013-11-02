package ast.stm;

public class Print extends T
{
  public ast.exp.T exp;

  public Print(ast.exp.T exp, int lineNum)
  {
    this.exp = exp;
    this.lineNum = lineNum;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
