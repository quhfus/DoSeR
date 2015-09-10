/*
 * Distributed as part of the Stanford Topic Modeling Toolbox.
 * Copyright (c) 2009- The Board of Trustees of the Leland
 * Stanford Junior University.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA.
 */
package edu.stanford.nlp.tmt;

import java.net.{URL,URI};
import java.io.File;
import scalanlp.pipes.Pipes;

/**
 * Main console entry point for TMT.  If no arguments are provided, pops
 * up the GUI.  If arguments are provided, uses scala's main generic runner
 * to run the script from the command line.
 * 
 * @author dramage
 */
object TMTMain {
  def main(args : Array[String]) {
    if (args.length == 0) {
      TMTSwingMain.main(args);
    } else {
      import Pipes.global._;
      
      // end this process when asked
      TMTHub.beginClient();

      // redirect to invoking the standard scala main method
      val method = Class.forName("scala.tools.nsc.MainGenericRunner").
        getMethod("main", classOf[Array[String]]);

      // augmented arguments
      val aurg : Object = (
        List[String](
          "-nocompdaemon",
          "-classpath", System.getProperty("java.class.path"),
          "-usejavacp"
        ) ++ args
      ).toArray[String];

      method.invoke(null, aurg);
    }
  }
}

/**
 * Provides IPC to kill launched processes from the GUI.
 *
 * @author dramage
 */
object TMTHub {
  import scalanlp.distributed.{Hub,SocketService};
  
  val hubPropertyName = "scalanlp.distributed.hub";
  val idPropertyName  = "scalanlp.distributed.id";
  
  private var id = 0;
  def getNextID = synchronized { id += 1; "/tmt/"+(id - 1); }
  
  /**
   * Sets up the listener service to exit this client program on request from
   * the hub if scalanlp.distributed.Id is non-null.
   */
  def beginClient() {
    val idName  = System.getProperty(idPropertyName);
    if (idName != null) {
      try {
        val service = SocketService(idName) {
          case EndProcess =>
            System.exit(-1);
        }
        hub.register(service.uri);
        service.runAsDaemon;
      } catch {
        case _ =>
          System.err.println("Remote control has been disabled for this client "+
                             " -- you may have to kill the process manually");
      }
    }
  }

  /** Starts a hub service. */
  def beginServer() = {
    val hubService = Hub.service();
    hubService.runAsDaemon;
    System.setProperty(hubPropertyName, hubService.uri.toString);
    System.err.println("Starting hub at "+hubService);
  }

  /** Returns a connection to the hub service. */
  lazy val hub = {
    if (System.getProperty(hubPropertyName) == null) {
      throw new RuntimeException("No hub provided or started");
    }
    Hub.connect(new URI(System.getProperty(hubPropertyName)));
  }

  //
  // Messages
  //

  /** Sent to end the process. */
  case object EndProcess;
}

/**
 * Version information about the current TMT release with code to check for updates.
 * 
 * @author dramage
 */
object TMTVersion {
  val VERSION = "tmt-0.4.0";
  
  val VERSION_URL = "http://nlp.stanford.edu/software/tmt/release.txt";
  
  sealed trait Response;
  case object UpToDateResponse extends Response;
  case class  NewVersionResponse(version : String, uri : URI) extends Response;
  case class  ErrorResponse(message : String) extends Response;
  
  def checkForUpdates() : Response = {
    import Pipes.global._;
    
    try {
      val current = new URL(VERSION_URL).openConnection.getInputStream.
        getLines.next.split("\t");
      
      if (current(0) == VERSION) {
        UpToDateResponse;
      } else {
        NewVersionResponse(current(0), new URI(current(1)));
      }
    } catch {
      case ex : Throwable =>
        ErrorResponse(ex.getClass.getSimpleName + ": " + ex.getMessage);
    }
  }
}

import scala.swing._;
import scala.swing.event._;
import java.awt.{Desktop, Font, Toolkit};
import java.awt.event.KeyEvent;
import javax.swing.{UIManager,KeyStroke};


/**
 * Helper traits for the TMTSwingMain.
 * 
 * @author dramage
 */
private[tmt] object TMTSwingHelpers {

  case class TMTPanelClosedEvent(panel : TMTJobPanel) extends Event;
  
  trait ClickHandler extends Reactor {
    var clickHandler = Map[AbstractButton,(()=>Unit)]();
    def onClick(b : AbstractButton)(f : =>Unit) = {
      clickHandler += (b -> f _);
      listenTo(b);
    }
    reactions += {
      case ButtonClicked(b) =>
        clickHandler(b)();
    }
  }

  trait BoxLayoutLeft extends Component {
    xLayoutAlignment = 0;
    yLayoutAlignment = 0;
  }

  trait SimpleBorder extends Component {
    border = javax.swing.border.LineBorder.createBlackLineBorder;
  }
}

import TMTSwingHelpers._;

/**
 * Main frame GUI for no-arg invocation.
 * 
 * @author dramage
 */
private[tmt] object TMTSwingMain extends SimpleSwingApplication {
  
  implicit val pipes = Pipes.global;

  var lastOpenedFileFolder = pipes.cwd;
  
  // use platform toolkit
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

  // start the server
  TMTHub.beginServer();
  
  val _title = "Stanford Topic Modeling Toolbox ("+TMTVersion.VERSION+")";
  
  private def showURI(uri : URI) = {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(uri);
    } else {
      Dialog.showMessage(null, "Open your web browser to: "+uri);
    }
  }
  
  def top = new MainFrame with ClickHandler { main =>
    title = _title;
    
    reactions += {
      case TMTPanelClosedEvent(panel) => {
        for ((page,i) <- tabs.pages.zipWithIndex; if page.content == panel) {
          tabs.pages.remove(i)
        }
      }
    }
    
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem("Open script ...") {
          onClick(this) {
            val chooser = new FileChooser(lastOpenedFileFolder);
            if (chooser.showDialog(this, "Open")  == FileChooser.Result.Approve && chooser.selectedFile != null) {
              lastOpenedFileFolder = chooser.selectedFile.getParentFile;
              val sub = Pipes(pipes);
              sub.cd(chooser.selectedFile.getParentFile);
              
              val panel = new TMTJobPanel(chooser.selectedFile)(sub);
              val tab = new TabbedPane.Page(chooser.selectedFile.getName, panel);
              main.listenTo(panel);
              tabs.pages += tab;
              tabs.peer.setSelectedComponent(panel.peer);
            }
          }
          
          peer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }
        
        contents += new MenuItem("Exit") {
          onClick(this) {
            System.exit(0);
          }
          
          peer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }
      }
      
      contents += new Menu("Edit") {
        contents += new MenuItem("Copy") {
          onClick(this) {
            val clipboard = java.awt.Toolkit.getDefaultToolkit.getSystemClipboard;
            clipboard.setContents(new java.awt.datatransfer.StringSelection(
              tabs.pages(0).content.asInstanceOf[TMTJobPanel].scroll.text.selected), null);
          }
         
          peer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
        }
      }
      
      contents += new Menu("Help") {
        contents += new MenuItem("Documentation") {
          onClick(this) {
            showURI(new URI("http://nlp.stanford.edu/software/tmt/"));
          }
          
          peer.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        }
      }
    }
    
    val tabs = new TabbedPane with BoxLayoutLeft {
      preferredSize = new scala.swing.Dimension(720,400);
      
      val welcome = new BorderPanel {
        val placeholder = new Label("<html>&nbsp;</html>");
        
        val label = new Label("""<html><center>
            <h1>Stanford Topic Modeling Toolbox</h1>
            <p>Load a TMT script into a new tab using the File -> Open script.</p>
            <br /><br />
            <font size="-1">
            <p>Copyright (c) 2009- The Board of Trustees of <br />
               The Leland Stanford Junior University. All Rights Reserved.</p>
            </font>
          </center></html>""");
        add(label, BorderPanel.Position.Center);
        add(placeholder, BorderPanel.Position.South);
        
        scala.concurrent.ops.spawn {
          val updates = TMTVersion.checkForUpdates match {
            case TMTVersion.UpToDateResponse =>
              new Label("<html><p>This is the current release.</p></html>");
            case TMTVersion.NewVersionResponse(version, uri) =>
              new FlowPanel {
                contents += new Label("<html><p>A newer version of this software is available: </p></html>");
                contents += new Button("Download page") { onClick(this) { showURI(uri); } } 
              }
            case TMTVersion.ErrorResponse(msg) =>
              new Label("<html>Could not check for updates at this time</html>");
          };
          
          peer.remove(placeholder.peer);
          add(updates, BorderPanel.Position.South)
          peer.doLayout;
        }
      };
      pages += new TabbedPane.Page("Welcome", welcome);
    }
    
    contents = tabs;
  }
}

class TMTJobPanel(script : File)(implicit pipes : Pipes) extends BorderPanel with ClickHandler with Publisher {
  import pipes._;
  
  import javax.swing.JOptionPane;
  import java.awt.GridBagConstraints._;
  val inset = new java.awt.Insets(0,10,0,10);
  
  var processID : String = null;
  
  def memorySpec : Option[String] = {
    try {
      val rv = info.memoryArea.number.text.toInt;
      require(rv > 0);
      Some(rv + "m");
    } catch {
      case _ =>
        JOptionPane.showMessageDialog(peer, 
          "Inavlid memory specification given: "+info.memoryArea.number.text,
          "Invalid memory specification", JOptionPane.ERROR_MESSAGE);
        None;
    }
  }
  
  def doRun() {
    if (!memorySpec.isDefined) {
      return;
    }
    
    run.enabled = false;
    stop.enabled = true;
    
    if (processID != null) {
      throw new IllegalArgumentException("Can't call run while running.");
    }
    
    def clean(classpath : String) : Iterable[String] =
      if (classpath == null) Iterable.empty
      else for (segment <- classpath.split(System.getProperty("path.separator"));
             val file = new java.io.File(segment); if (file.exists))
          yield file.getAbsolutePath;
    
    env("CLASSPATH", 
        (clean(System.getProperty("java.class.path")) ++ clean(System.getProperty("env.classpath"))).
          mkString(System.getProperty("path.separator"))); 
    
    processID = TMTHub.getNextID;
    val cmd = "java -D%s=%s -D%s=%s -Xmx%s edu.stanford.nlp.tmt.TMTMain \"%s\"".format(
      TMTHub.hubPropertyName, System.getProperty(TMTHub.hubPropertyName),
      TMTHub.idPropertyName,  processID, memorySpec.get, script);
    
    val process = pipes.sh(cmd);
      
    scroll.text.text = "";
    scroll.text.append("-------------------------------------------------------\n");
    scroll.text.append("Command started: "+new java.util.Date+"\n");
    scroll.text.append(cmd+"\n\n");
              
    import scala.concurrent.ops._;
              
    spawn {
      val errStream = future {
        for (line <- process.getErrorStream.getLines) {
          scroll.synchronized { scroll.text.append(line + "\n"); }
          scroll.peer.getVerticalScrollBar.setValue(scroll.peer.getVerticalScrollBar.getMaximum)
        }
        true;
      }
                
      val outStream = future {
        for (line <- process.getInputStream.getLines) {
          scroll.synchronized { scroll.text.append(line + "\n"); }
          scroll.peer.getVerticalScrollBar.setValue(scroll.peer.getVerticalScrollBar.getMaximum)
        }
        true;
      }
              
      errStream();
      outStream();
      process.waitFor;
              
      scroll.text.append("-------------------------------------------------------\n");
      scroll.text.append("Command finished: "+new java.util.Date+"\n");
                  
      stop.enabled = false;
      run.enabled = true;
      processID = null;
    }
  }
          
  def doStop() {
    if (processID != null) {
      TMTHub.hub.select(_.getPath == processID).foreach(_ ! TMTHub.EndProcess);
      
      scroll.text.append("-------------------------------------------------------\n");
      scroll.text.append("Command terminated: "+new java.util.Date+"\n");
              
      stop.enabled = false;
      run.enabled = true;
      processID = null;
    }
  }
          
  def doClose() {
    doStop();
    publish(TMTPanelClosedEvent(this));
  }
          
  val run = new Button("Run") {
    onClick(this)(doRun);
  }
          
  val stop = new Button("Stop") {
   enabled = false;
   onClick(this)(doStop);
  }
    
  val info = new GridBagPanel with SimpleBorder {
    val scriptArea = new BoxPanel(Orientation.Vertical) {
      contents += new Label(script.toString) with BoxLayoutLeft;
        
      contents += new FlowPanel(FlowPanel.Alignment.Left)() with BoxLayoutLeft {
          
        val edit = new Button("Edit script") {
          onClick(this) {
            try {
              Desktop.getDesktop.edit(script);
            } catch {
              case _ =>
                try {
                  Desktop.getDesktop.open(script);
                } catch {
                  case _ =>
                    Dialog.showMessage(this, "Unable to automatically " +
                      "launch your system's text editor.\nYou can edit the " +
                      "script in any text editor.");
                }
            }
              
          }
        };

        contents += edit;
        contents += run;
        contents += stop;
      }
    }
      
    val dataArea = new BoxPanel(Orientation.Vertical) {
      val label = new Label(pipes.cwd.toString) with BoxLayoutLeft;
        
      contents += label;
        
      contents += new FlowPanel(FlowPanel.Alignment.Left)() with BoxLayoutLeft {
        contents += new Button("Explore folder") {
          onClick(this) {
            Desktop.getDesktop.open(pipes.cwd);
          }
        }
          
        contents += new Button("Change data folder ...") {
          onClick(this) {
            val chooser = new FileChooser(pipes.cwd);
            chooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly;
            if (chooser.showDialog(this, "Change data folder") == FileChooser.Result.Approve && chooser.selectedFile != null) {
              pipes.cd(chooser.selectedFile);
              label.text = pipes.cwd.toString;
            }
          }
        }
      }
    }
    
    val memoryArea = new FlowPanel(FlowPanel.Alignment.Left)() with BoxLayoutLeft {
      val number = new TextField(4);
      number.text = ("256");
      number.inputVerifier = (c : Component) => {
        try { number.text.toInt > 0 } catch { case _ => false; };
      }
      
      contents += number;
      
      contents += new Label("MB");
    }
    
    val closeButton = new Button {
      icon = javax.swing.plaf.metal.MetalIconFactory.getInternalFrameCloseIcon(16);
      onClick(this)(doClose);
    }
      
    add(new Label("Script:"),      new Constraints(0,0,1,1,0,0, FIRST_LINE_END,  NONE,inset,0,0));
    add(scriptArea,                new Constraints(1,0,1,1,1,0, LINE_START,      HORIZONTAL,inset,20,0));
    add(closeButton,               new Constraints(2,0,1,1,0,0, FIRST_LINE_END,  NONE,inset,0,0));
    add(new Label("Data folder:"), new Constraints(0,1,1,1,0,0, FIRST_LINE_END,  NONE,inset,0,0));
    add(dataArea,                  new Constraints(1,1,1,1,1,0, LINE_START,      BOTH,inset,20,0));
    add(new Label("Memory:"),      new Constraints(0,2,1,1,0,0, EAST,            NONE,inset,0,0));
    add(memoryArea,                new Constraints(1,2,1,1,1,0, EAST,            BOTH,inset,20,0));
  }
    
  val scroll = new ScrollPane {
    val text = new TextArea {
      font = new Font(Font.MONOSPACED, 0, 12);
      editable = false;
      wordWrap = false;
        
      text = """Click "Run" to start the script.  Output will appear in this window."""
    }
      
    viewportView = text;
  }
    
  add(info,   BorderPanel.Position.North);
  add(scroll, BorderPanel.Position.Center);
}
