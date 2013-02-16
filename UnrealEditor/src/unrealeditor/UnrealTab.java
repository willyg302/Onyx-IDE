package unrealeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.fife.io.UnicodeReader;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RecordableTextAction;


public class UnrealTab {
    public RSyntaxTextArea textArea;
    public RTextScrollPane textScroller;
    private File workingFile;
    private ButtonTabComponent thisTabComp;
    
    // True if PC Encoded, false if UNIX
    private boolean isPC = true;
    int textHash = 0;
    
    RecordableTextAction undoAction = RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION);
    
    private MainTextPad padref;
    
    public UnrealTab(final MainTextPad pad, File myFile) {
        padref = pad;
        if(myFile != null)
            setWF(myFile);
        textArea = new RSyntaxTextArea(36, 120);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, textArea.getFont().getSize()+4));
        textArea.setSyntaxEditingStyle("SYNTAX_STYLE_USCRIPT");
        // Generate the custom UScript scheme
        textArea.setSyntaxScheme(generateUScriptScheme()); 
        
        try {
            textArea.setDropTarget(new DropTarget(textArea, new DropTargetListener() {
                public void dragEnter(DropTargetDragEvent e) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                }
                public void dragOver(DropTargetDragEvent e) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                }
                public void dragExit(DropTargetEvent e) {}
                public void drop(DropTargetDropEvent e) {
                    if((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                    } else {
                        e.rejectDrop();
                        return;
                    }
                    try {
                        List<File> files = (List<File>) e.getTransferable().getTransferData(
                                DataFlavor.javaFileListFlavor);
                        e.dropComplete(true);
                        for(File f : files)
                            pad.addTab(f);
                        return;
                    } catch(java.io.IOException ex) {
                        System.err.println(ex);
                    } catch(UnsupportedFlavorException ex) {
                        System.err.println(ex);
                    }
                    e.dropComplete(false);
                }
                public void dropActionChanged(DropTargetDragEvent e) {}
            }));
        } catch(RuntimeException ex) {
            ex.printStackTrace(System.err);
        }
        
        pad.autoCompletion.install(textArea);
        
        
        
        textScroller = new RTextScrollPane(textArea, true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(textScroller, BorderLayout.CENTER);
        
        thisTabComp = new ButtonTabComponent(getDocumentTitle(), pad);
	pad.tabbedPane.addTab(getDocumentTitle(), mainPanel );
        pad.tabbedPane.setTabComponentAt(pad.tabbedPane.getTabCount()-1, thisTabComp);
        textArea.requestFocusInWindow();
        
        textArea.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                pad.updateStatusBar();
                updateTabLabel("");
            }
        });
        
        
        // Set the saved file options
        textArea.setLineWrap(pad.tabOptions[0]);
        textScroller.setLineNumbersEnabled(pad.tabOptions[1]);
        textArea.setWhitespaceVisible(pad.tabOptions[2]);
        textArea.setHighlightCurrentLine(pad.tabOptions[3]);
        textArea.setTabsEmulated(pad.tabOptions[4]);
        textArea.setCodeFoldingEnabled(pad.tabOptions[5]);
    }
    
    public void updateTabLabel(String newLabel) {
        String lbl = (newLabel.equals("") ? getDocumentTitle() : newLabel);
        if(textHash != textArea.getText().hashCode()) {
            thisTabComp.setNewLabel(lbl + "*", Color.RED);
        } else if(textHash == textArea.getText().hashCode()) {
            thisTabComp.setNewLabel(lbl, Color.BLACK);
        }
    }
    
    public SyntaxScheme generateUScriptScheme() {
        SyntaxScheme scheme = textArea.getSyntaxScheme();
        
        Style style = scheme.getStyle(Token.DATA_TYPE);
        style.font = new Font("Monospaced", Font.BOLD, textArea.getFont().getSize());
        scheme.setStyle(Token.DATA_TYPE, style);
        
        style = scheme.getStyle(Token.FUNCTION);
        style.font = new Font("Monospaced", Font.BOLD, textArea.getFont().getSize());
        scheme.setStyle(Token.FUNCTION, style);
        
        return scheme;
    }
    
    public String getDocumentTitle() {
        if(workingFile == null)
            return "Untitled";
        String title = "";
        File parent = workingFile.getParentFile();
        if(parent != null)
            title = parent.getName() + File.separatorChar;
        title += workingFile.getName();
        return title.substring(title.lastIndexOf(File.separatorChar)+1, title.indexOf('.'));
    }
    
    public File getWF() {
        return workingFile;
    }
    
    public void setWF(File theFile) {
        workingFile = theFile;
    }
    
    public boolean getIsPC() {return isPC;}
    public void setIsPC(boolean set) {isPC = set;}
    
    boolean openFile(File file) {
        try {
            byte[] raw = Utils.readBytes(file);
            StringCodec sc = new StringCodec();
            String tmp = sc.decode(raw);
            
            isPC = (tmp.indexOf('\r') >= 0);
            String encoding = sc.getDetectedEncoding();
            StringBuilder buf = new StringBuilder();
            BufferedReader reader = new BufferedReader(new UnicodeReader(new ByteArrayInputStream(raw), encoding));
            String line;
            while((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append('\n');
            }
            String text = buf.toString();
            padref.fileChooser.setSelectedFile(file);
            padref.getRootPane().putClientProperty("Window.documentFile", file); // Mac OS X
            textArea.setText(text);
            textArea.setCaretPosition(0);
            textArea.discardAllEdits();
            padref.getRootPane().putClientProperty("Window.documentModified", Boolean.FALSE);
            textHash = text.hashCode();
            updateTabLabel("");
        } catch(IOException ex) {
            return false;
        }
        return true;
    }
    
    private void writeTextFile(String content, File file, String encoding) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
        //if(padref.pcTerminationStyle.isSelected())
        if(isPC)
            writer = new PCWriter(writer);
        writer.write(content);
        writer.close();
    }
    
    boolean saveFile(File file) {
        try {
            String text = textArea.getText();
            writeTextFile(text, file, "UTF-8");
            textHash = text.hashCode();
            padref.getRootPane().putClientProperty("Window.documentModified", Boolean.FALSE);
            padref.fileChooser.setSelectedFile(file);
            padref.statusBar.setText("Document written to " + file.getAbsolutePath());
            padref.getRootPane().putClientProperty("Window.documentFile", file); // Mac OS X
        } catch(IOException ex) {
            return false;
        }
        setWF(file);
        updateTabLabel("");
        return true;
    }
}