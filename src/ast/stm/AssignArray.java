package ast.stm;

public class AssignArray extends T
{
  public String id;
  public ast.exp.T index;
  public ast.exp.T exp;

  public AssignArray(String id, ast.exp.T index, ast.exp.T exp, int lineNum)
  {
    this.id = id;
    this.index = index;
    this.exp = exp;
    this.lineNum = lineNum;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
  }
}
