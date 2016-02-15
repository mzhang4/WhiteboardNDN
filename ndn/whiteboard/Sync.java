package ndn.whiteboard;

import java.awt.event.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Properties;
import java.io.IOException;
import java.rmi.RemoteException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.sync.ChronoSync2013;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;
import javax.swing.JFrame;

public class Sync implements ChronoSync2013.OnInitialized,
                                    ChronoSync2013.OnReceivedSyncState,
                                    OnData,
                                    OnInterest,
                                    OnRegisterFailed,
                                    OnTimeout
{
  private ChronoSync2013 m_chronoSync;
  private Face m_face;
  private KeyChain m_keyChain;
  private Name m_certificateName;
  private Map<String,Long> m_lastSequenceNo = new HashMap<String,Long>();
  private UUID m_uuid;
  private WhiteBoard m_whiteBoard = new WhiteBoard();
  private String m_content = "JOIN";

  public Sync(Face face) throws SecurityException
  {
    m_face = face;
    m_certificateName = new Name();

    // Set up KeyChain and Identity
    m_keyChain = Security.initialize(m_face, m_certificateName);

    // Used to make the data prefix unique to this process
    m_uuid = UUID.randomUUID();

    System.out.println("The uuid is: " + m_uuid);

    final Name DATA_PREFIX = new Name("/ndn/demo/" + m_uuid);
    final Name BROADCAST_PREFIX = new Name("/ndn/broadcast/demo");
    final Name DATA_ALL_PREFIX = new Name("/ndn/demo/data/" + m_uuid);

    int session = (int)Math.round(System.currentTimeMillis() / 1000.0);

    // Register Application prefix
    try {
      m_face.registerPrefix(DATA_PREFIX, this, this);
      m_face.registerPrefix(DATA_ALL_PREFIX, this, this);
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }

    try {
      m_chronoSync = new ChronoSync2013(this,
                                        this,
                                        DATA_PREFIX,
                                        BROADCAST_PREFIX,
                                        session,
                                        m_face,
                                        m_keyChain,
                                        m_certificateName,
                                        5000,
                                        this);
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }

  public final int
  hasUpdate()
  {
    return m_whiteBoard.getChanged();
  }

  public final void
  sendMessage() throws IOException, SecurityException
  {
    m_content = m_whiteBoard.getText();
    m_whiteBoard.setChanged(0);
    m_chronoSync.publishNextSequenceNo();
  }

  public final void
  sendChangeMessage(int opt) throws IOException, SecurityException
  {
    if (opt == 2)
    {
      m_content = "CALL";
    }
    else
    {
      m_content = "REALSE";
    }

    m_whiteBoard.setChanged(0);
    m_chronoSync.publishNextSequenceNo();
  }
 
  public void
  onData(Interest interest, Data data)
  {
    String content = data.getContent().toString();
    String [] s = data.getName().toUri().split("/");

    if (data.getName().toUri().contains("/data/")) {
      m_whiteBoard.setText(content);
    } else {
      if (s[s.length-1].equals("0"))
      {
        return;
      }
      else if (content.equals("CALL")) 
      {
        m_whiteBoard.setRight(true);
      }
      else if (content.equals("REALSE")) 
      {
        m_whiteBoard.setRight(false);
      }
      else
        m_whiteBoard.setText(content);
    }
  }

  public void
  onTimeout(Interest interest)
  {
    // pass
  }

  public void
  onInterest(Name prefix, Interest interest, Transport transport, long registeredPrefixId)
  {
    if (prefix.toUri().contains("/data/")) {
      Data data = new Data(interest.getName().appendSequenceNumber(m_chronoSync.getSequenceNo()));
      data.setContent(new Blob(m_whiteBoard.getText()));
    } else {
      // Create response Data
      Data data = new Data(interest.getName());

      data.setContent(new Blob(m_content));

      Blob encodedData = data.wireEncode();

      // Send Data
      try {
        //System.out.println("Sending Data for " + interest.getName().toUri());
        transport.send(encodedData.buf());
      } catch (IOException e) {
        //System.out.println(e.getMessage());
      }
    }
  }

  public final void
  onInitialized()
  {
    // pass
  }

  public final void
  onReceivedSyncState(List syncStates, boolean isRecovery)
  {
    // Iterate through SyncState updates
    for (int i = 0; i < syncStates.size(); ++i) {
      ChronoSync2013.SyncState state = (ChronoSync2013.SyncState)syncStates.get(i);

      // Get UUID associated with SyncState
      String id = new Name(state.getDataPrefix()).get(-1).toEscapedString();

      // Don't fetch own updates
      if (id.equals(m_uuid.toString())) {
        continue;
      }

      // Don't fetch outdated data
      if (m_lastSequenceNo.get(id) != null && state.getSequenceNo() <= m_lastSequenceNo.get(id)) {
        continue;
      }

      if (m_lastSequenceNo.get(id) == null && state.getSequenceNo() > 0) {
        m_whiteBoard.setRight(true);

        if (m_chronoSync.getSequenceNo() == 0) {
          Name name =new Name("/ndn/demo/data/" + m_uuid);
          Interest interest = new Interest(name);
          break;
        }
      }

      Name name = new Name(state.getDataPrefix() + "/" + state.getSequenceNo());

      Interest interest = new Interest(name);

      m_lastSequenceNo.put(id, state.getSequenceNo());

      try {
        m_face.expressInterest(interest, this);
      }
      catch (Exception e) {
      }
    }
  }

  public void
  onRegisterFailed(Name prefix)
  {
    throw new Error("Prefix registration failed");
  }

  public static void
  main(String[] argv)
  {
    try {
      Face face = new Face();

      Sync sync = new Sync(face);

      while (true) {
        if (sync.hasUpdate() == 1)
        {
          sync.sendMessage();
        } 
        else if (sync.hasUpdate() == 2) 
        {
          sync.sendChangeMessage(2);
        } 
        else if (sync.hasUpdate() == 3)
        {
          sync.sendChangeMessage(3);
        }

        face.processEvents();

        Thread.sleep(10);
      }
    }
    catch (Exception e) {
    }
  }
}