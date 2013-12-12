package codegen.C.exp;

import codegen.C.Visitor;

public class Id extends T
{
  public String id;
  public boolean isField=false;
  public boolean isInt;

  public Id(String id)
  {
    this.id = id;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}
