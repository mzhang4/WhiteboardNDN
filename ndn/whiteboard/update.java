package ndn.whiteboard;

public class update 
{ 
  private int action;
  private int offset;
  private int length;
  private String updates;


  public int getAction() { return action; }
  public void setAction(int a) { action = a; }
  public int getOffset() { return offset; }
  public void setOffset(int os) { offset = os; }
  public int getLength() { return length; }
  public void setLength(int l) { length = l; }
  public String getUpdate() { return updates; }
  public void setUpdate(String u) { updates = u; }
}