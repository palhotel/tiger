package util;

public class Error
{
  public static void bug()
  {
    throw new java.lang.Error("Compiler bug");
  }
  
  public static void bug(String msg)
  {
    throw new java.lang.Error("Compiler bug: " + msg);
  }
}
