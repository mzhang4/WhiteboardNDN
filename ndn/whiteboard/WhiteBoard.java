package ndn.whiteboard;

import java.io.*;
import java.awt.*; 
import javax.swing.*; 
import java.awt.event.*; 
import javax.swing.event.*; 
import java.util.*; 
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.FileWriter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;

public class WhiteBoard extends JFrame 
{ 
  private JTextArea m_jta; 
  private String m_copyText;
  private int m_action;
  private int m_offset;
  private int m_length;
  private int m_changed = 0;
  private boolean m_editable = false;
  private JMenuItem mcfw = new JMenuItem("CALL");
  private JMenuItem mrea = new JMenuItem("Release");
  private JMenuItem mClear = new JMenuItem("Clear");


  class cfwListener implements ActionListener
  {
    public void
    actionPerformed(ActionEvent e)
    {
      m_editable = true;
      m_jta.setEditable(true);
      mrea.setEnabled(true);
      mClear.setEnabled(true);
      mcfw.setEnabled(false);
      m_changed = 2;
    }
  }

  class reaListener implements ActionListener
  {
    public void
    actionPerformed(ActionEvent e)
    {
      m_editable = false;
      m_jta.setEditable(false);
      mrea.setEnabled(false);
      mClear.setEnabled(false);
      mcfw.setEnabled(true);
      m_changed = 3;
    }
  }

  class clearListener implements ActionListener
  {
    public void
    actionPerformed(ActionEvent e)
    {
      m_jta.setText("");
      m_changed = 1;
    }
  }

  class saveListener implements ActionListener 
  {
    public void 
    actionPerformed(ActionEvent e) 
    {
      JFileChooser chooser = new JFileChooser(); 
      int retrival = chooser.showSaveDialog(WhiteBoard.this);
      if (retrival == JFileChooser.APPROVE_OPTION) {
        try (Writer writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
          writer.write(m_jta.getText().toString());
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
      else {
        System.out.println("Save Canceled by User");
      }
    }
  }

  class exitListener implements ActionListener 
  {
    public void
    actionPerformed(ActionEvent e) 
    {
      System.exit(0);
    } 
  }

  class copyListener implements ActionListener 
  { 
    public void
    actionPerformed(ActionEvent e)
    { 
      m_copyText = m_jta.getSelectedText().toString();
    }
  }

  class pasteListener implements ActionListener 
  {
    public void
    actionPerformed(ActionEvent e) 
    {
      if (m_copyText != null) {
        m_jta.append(m_copyText);
      }
    }
  }

  class MyDocumentListener implements DocumentListener
  {
    final String newline = "\n";
 
    public void
    insertUpdate(DocumentEvent e)
    {
      m_changed = 1;
    }

    public void
    removeUpdate(DocumentEvent e)
    {
      m_changed = 1;
    }

    public void
    changedUpdate(DocumentEvent e)
    {
      //Plain text components don't fire these events.
    }
  }
 
  public WhiteBoard() 
  {
    m_jta = new JTextArea("",40,60); 
    //m_jta=new RSyntaxTextArea("",40,60); 
    m_jta.getDocument().addDocumentListener(new MyDocumentListener());    
    m_jta.setEditable(false);
    //RTextScrollPane jsp = new RTextScrollPane(m_jta);
    JScrollPane jsp = new JScrollPane(m_jta); 
    JMenuBar jmb = new JMenuBar();
    JMenu mRight = new JMenu("CFW"); 
    JMenu mFile = new JMenu("File");
    JMenu mEdit = new JMenu("Edit");

    mcfw.addActionListener(new cfwListener());
    mRight.add(mcfw);

    mrea.setEnabled(false);
    mrea.addActionListener(new reaListener());
    mRight.add(mrea);

    mClear.setEnabled(false);
    mClear.addActionListener(new clearListener());
    mFile.add(mClear);

    JMenuItem mSave = new JMenuItem("Save"); 
    mSave.addActionListener(new saveListener()); 
    mFile.add(mSave); 

    mFile.addSeparator();

    JMenuItem mExit = new JMenuItem("Exit"); 
    mExit.addActionListener(new exitListener()); 
    mFile.add(mExit); 
    mFile.setMnemonic(KeyEvent.VK_F); 

    JMenuItem jmi; 
    jmi = new JMenuItem("Copy");
    jmi.addActionListener(new copyListener()); 
    mEdit.add(jmi); 

    jmi = new JMenuItem("Paste");
    jmi.addActionListener(new pasteListener()); 
    mEdit.add(jmi); 

    jmb.add(mRight);
    jmb.add(mFile); 
    jmb.add(mEdit);

    this.setJMenuBar(jmb);

    this.getContentPane().add(jsp);
    this.setSize(600,600);
    this.setVisible(true);
  }

  public String
  getText() 
  {
    return m_jta.getText();
  }

  public void
  setText(String content)
  {
    System.out.println("I dont want except");
    m_jta.setEditable(true);
    m_jta.setText(content);

    if (m_editable == false)
      m_jta.setEditable(false);

    m_changed = 0;
  }

  public void
  setRight(boolean right)
  {
    if (right == true) {
      mcfw.setEnabled(false);
    }
    else {
      mcfw.setEnabled(true);
    }

    m_changed = 0;
  }

  public int
  getChanged()
  {
    return m_changed;
  }

  public void
  setChanged(int changed)
  {
    m_changed = changed;
  }
}