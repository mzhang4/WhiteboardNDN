package tests;

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
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class TextEditorDemo extends JFrame {
   
   class MyDocumentListener implements DocumentListener
   {
      final String newline = "\n";
 
      public void 
      insertUpdate(DocumentEvent e)
      {
      }
        
      public void
      removeUpdate(DocumentEvent e)
      {
      }

      public void
      changedUpdate(DocumentEvent e)
      {
         //Plain text components don't fire these events.
      }
   }

   public TextEditorDemo() {

      JPanel cp = new JPanel();

      RSyntaxTextArea textArea = new RSyntaxTextArea(60, 80);
      textArea.getDocument().addDocumentListener(new MyDocumentListener());    

      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

      JMenuBar jmb = new JMenuBar();
      JMenu mRight = new JMenu("CFW"); 
      JMenu mFile = new JMenu("File"); 
      JMenu mEdit = new JMenu("Edit");

      //textArea.setCodeFoldingEnabled(true);
      RTextScrollPane sp = new RTextScrollPane(textArea);
      cp.add(sp);

      setContentPane(cp);
      setTitle("Text Editor Demo");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);

      jmb.add(mRight);
      jmb.add(mFile); 
      jmb.add(mEdit);
      this.setJMenuBar(jmb); 

   }

   public static void main(String[] args) {
      // Start all Swing applications on the EDT.
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new TextEditorDemo().setVisible(true);
         }
      });
   }

}