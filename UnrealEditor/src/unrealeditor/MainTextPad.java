package unrealeditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.SearchEngine;

public class MainTextPad extends JFrame {

    
    
    
    private static MainTextPad instance;
    static final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    public final static String APP_NAME = "Onyx UnrealScript Editor";
    public final static String VERSION = "1.0";
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu, editMenu, tabsMenu, lineTerminationMenu, viewMenu, 
            themeMenu, helpMenu, windowsMenu;
    JRadioButtonMenuItem pcTerminationStyle = new JRadioButtonMenuItem("PC style");
    JRadioButtonMenuItem unixTerminationStyle = new JRadioButtonMenuItem("UNIX style");
    private ButtonGroup lafGroup = new ButtonGroup();
    ButtonGroup terminationStyle = new ButtonGroup();
    {
        terminationStyle.add(pcTerminationStyle);
        terminationStyle.add(unixTerminationStyle);
        pcTerminationStyle.setSelected(true);
    }

    AutoCompletion autoCompletion;
    DefaultCompletionProvider provider = new DefaultCompletionProvider();
    
    
    JStackLabel statusBar = new JStackLabel();
    JFileChooser fileChooser = new JFileChooser();
    
    
    
    JTabbedPane tabbedPane;
    //DnDTabbedPane tabbedPane;
    
    
    ArrayList openTabs = new ArrayList();
    
    MainToolBar toolBar = null;
    
    //reference to our compiler area so CmdClient can update..
    public JTextArea compilerArea;
    public CmdClient commandClient;
    
    /**
     * TAB OPTIONS
     * ----------------------
     * 1. Line wrap?
     * 2. Show line numbers?
     * 3. Show whitespace?
     * 4. Highlight the current line?
     * 5. Have spaces emulate tabs?
     * 6. Enable code folding?
     * 
     * Shown here are the defaults.
     */
    public boolean[] tabOptions = {true, true, false, true, false, false};
    public int currentOnyxTheme = 0;
    public RecentMenu recentMenu;
    // C:\UDK\ or something like that
    public File UDKdir = null;
    
    
    
    /**
     * File Actions
     */
    Action newAction = new AbstractAction("New") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/new.png"));}
        public void actionPerformed(ActionEvent e) {
            addTab(null);
        }
    };
    
    Action openAction = new AbstractAction("Open...") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_O, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/open.png"));}
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
            chooser.setSelectedFile(fileChooser.getSelectedFile());
            chooser.setMultiSelectionEnabled(true);
            int choice = chooser.showOpenDialog(MainTextPad.this);
            File[] files = chooser.getSelectedFiles();
            if(choice == JFileChooser.APPROVE_OPTION) {
                //lastLocation = getLocation();
                for(File f : files)
                    addTab(f);
            }
        }
    };
    
    Action closeAction = new AbstractAction("Close") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_W, menuShortcutKeyMask));}
        public void actionPerformed(ActionEvent e) {
            closeTab(tabbedPane.getSelectedIndex());
        }
    };
    
    //false if do not close;
    public boolean closeTab(int index) {
        if(!ensureNoLoss(index))
            return false;
        tabbedPane.remove(index);
        openTabs.remove(index);
        if(openTabs.isEmpty()) {
            exitAction.actionPerformed(null);
        }
        return true;
    }
    
    Action saveAction = new AbstractAction("Save") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/save.png"));}
        public void actionPerformed(ActionEvent e) {
            File workingFile = getWorkingFile(-1);
            if(workingFile == null) {
                saveAsAction.actionPerformed(e);
            } else {
                saveFile(workingFile);
            }
        }
    };
    
    Action saveAsAction = new AbstractAction("Save As...") {
        public void actionPerformed(ActionEvent e) {
            //editors.remove(fileChooser.getSelectedFile());
            fileChooser.setSelectedFile(new File(getDocumentTitle(-1) + ".uc"));
            int choice = fileChooser.showSaveDialog(MainTextPad.this);
            File selectedFile = fileChooser.getSelectedFile();
            if(choice == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                if(selectedFile.exists()) {
                    int answer = JOptionPane.showConfirmDialog(MainTextPad.this,
                            "" + selectedFile.getAbsolutePath() + " already exists.\nDo you wish to replace it?",
                            "Save As",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if(answer == JOptionPane.YES_OPTION) {
                        saveFile(selectedFile);
                        return;
                    }
                    if(answer == JOptionPane.NO_OPTION)
                        return;
                }
                saveFile(selectedFile);
                return;
            }
        }
    };
    
    Action exitAction = new AbstractAction("Exit") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/exit.gif"));}
        public void actionPerformed(ActionEvent e) {
            for(int i = tabbedPane.getTabCount()-1; i>=0; i--)
                if(!closeTab(i))
                    return;
            if(UnrealEditor.runsStandalone) {
                saveSettings();
                System.exit(0);
            }
        }
    };
    
    /**
     * Edit actions
     */
    Action findAction = new AbstractAction("Find...") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuShortcutKeyMask));}
        public void actionPerformed(ActionEvent e) {
            findDialog.setMode(JFindDialog.Mode.find);
            findDialog.toFind.setText("");
            findDialog.setVisible(true);
        }
    };
    Action findNextAction = new AbstractAction("Find Next") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));}
        public void actionPerformed(ActionEvent e) {
            if(findDialog.toFind.getText().length() == 0) {
                findAction.actionPerformed(e);
            } else {
                boolean found = SearchEngine.find(getTab(-1).textArea,
                        findDialog.toFind.getText(),
                        findDialog.down.isSelected(),
                        findDialog.matchCase.isSelected(),
                        findDialog.wholeWords.isSelected(),
                        findDialog.regExp.isSelected());
                if(!found)
                    statusBar.setText("Search passed the end of file");
            }
        }
    };
    Action replaceAction = new AbstractAction("Replace...") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcutKeyMask));}
        public void actionPerformed(ActionEvent e) {
            findDialog.setMode(JFindDialog.Mode.replace);
            findDialog.toFind.setText("");
            findDialog.replaceWith.setText("");
            findDialog.setVisible(true);
        }
    };
    Action replaceNextAction = new AbstractAction("Replace") {
        public void actionPerformed(ActionEvent e) {
            if(findDialog.toFind.getText().length() == 0) {
                return;
            } else {
                boolean found = SearchEngine.replace(getTab(-1).textArea,
                        findDialog.toFind.getText(),
                        findDialog.replaceWith.getText(),
                        findDialog.down.isSelected(),
                        findDialog.matchCase.isSelected(),
                        findDialog.wholeWords.isSelected(),
                        findDialog.regExp.isSelected());
                if(!found)
                    statusBar.setText("Search passed the end of file");
            }
        }
    };
    Action replaceAllAction = new AbstractAction("Replace All") {
        public void actionPerformed(ActionEvent e) {
            if(findDialog.toFind.getText().length() == 0) {
                return;
            } else {
                int count = SearchEngine.replaceAll(getTab(-1).textArea,
                        findDialog.toFind.getText(),
                        findDialog.replaceWith.getText(),
                        findDialog.matchCase.isSelected(),
                        findDialog.wholeWords.isSelected(),
                        findDialog.regExp.isSelected());
                statusBar.setText("" + count + " occurences of " + findDialog.toFind.getText() + " replaced");
            }
        }
    };
    Action goToAction = new AbstractAction("Go To...") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_G, menuShortcutKeyMask));}
        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog(MainTextPad.this, 
                    "Line Number:", "Goto line", JOptionPane.PLAIN_MESSAGE);
            int line = Integer.parseInt(s) - 1;
            int i;
            try {
                i = getTab(-1).textArea.getLineStartOffset(line);
                getTab(-1).textArea.setCaretPosition(i);
            } catch(BadLocationException ex) {}
        }
    };
    Action completeCodeAction = new AbstractAction("Complete Code") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK));}
        public void actionPerformed(ActionEvent e) {
            autoCompletion.doCompletion(); // triggers nullpointer exception
        }
    };
    
    /**
     * Tabs sub menu
     */
    Action tabSizeAction = new AbstractAction("Set tab size") {
        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog(MainTextPad.this, "Tab size:", "" + getTab(-1).textArea.getTabSize());
            int tabSize = Integer.parseInt(s);
            getTab(-1).textArea.setTabSize(tabSize);
        }
    };
    ToggleAction emulatedTabs = new ToggleAction("Emulate tabs with spaces") {
        {setSelected(tabOptions[4]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(4, isSelected());
        }
    };
    Action convertTabsToSpaces = new ToggleAction("Convert tabs to spaces") {
        public void actionPerformed(ActionEvent e) {
            getTab(-1).textArea.convertTabsToSpaces();
        }
    };
    Action convertSpacesToTabs = new ToggleAction("Convert spaces to tabs") {
        public void actionPerformed(ActionEvent e) {
            getTab(-1).textArea.convertSpacesToTabs();
        }
    };
    
    
    
    /**
     * WillyG Actions
     * Because the call to RSyntaxTextArea seems bad...
     */
    Action undoAction = new AbstractAction("Undo") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/undo.png"));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION).actionPerformed(e);
        }
    };
    Action redoAction = new AbstractAction("Redo") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/redo.png"));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.REDO_ACTION).actionPerformed(e);
        }
    };
    Action cutAction = new AbstractAction("Cut") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_X, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/cut.png"));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.CUT_ACTION).actionPerformed(e);
        }
    };
    Action copyAction = new AbstractAction("Copy") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/copy.png"));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.COPY_ACTION).actionPerformed(e);
        }
    };
    Action pasteAction = new AbstractAction("Paste") {
        {putValue(Action.ACCELERATOR_KEY, 
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcutKeyMask));
        putValue(Action.SMALL_ICON, Utils.loadImage("/images/paste.png"));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION).actionPerformed(e);
        }
    };
    Action selectAllAction = new AbstractAction("Select All") {
        {putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A,menuShortcutKeyMask));}
        public void actionPerformed(ActionEvent e) {
            RSyntaxTextArea.getAction(RSyntaxTextArea.SELECT_ALL_ACTION).actionPerformed(e);
        }
    };
    
    /**
     * View actions
     */
    ToggleAction lineWrapAction = new ToggleAction("Line Wrap") {
        {setSelected(tabOptions[0]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(0, isSelected());
        }
    };
    ToggleAction lineNumbersAction = new ToggleAction("Line Numbers") {
        {setSelected(tabOptions[1]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(1, isSelected());
        }
    };
    ToggleAction whiteSpaceAction = new ToggleAction("White Space") {
        {setSelected(tabOptions[2]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(2, isSelected());
        }
    };
    ToggleAction highlightCurrentLineAction = new ToggleAction("Highlight Current Line") {
        {setSelected(tabOptions[3]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(3, isSelected());
        }
    };
    ToggleAction codeFoldingAction = new ToggleAction("Code Folding") {
        {setSelected(tabOptions[5]);}
        public void actionPerformed(ActionEvent e) {
            setAllTabsOption(5, isSelected());
        }
    };
    
    
    public void setAllTabsOption(int option, boolean isSelected) {
        for(int i = 0; i < tabbedPane.getTabCount(); i++) {
            switch(option) {
                case 0: getTab(i).textArea.setLineWrap(isSelected); break;
                case 1: getTab(i).textScroller.setLineNumbersEnabled(isSelected); break;
                case 2: getTab(i).textArea.setWhitespaceVisible(isSelected); break;
                case 3: getTab(i).textArea.setHighlightCurrentLine(isSelected); break;
                case 4: getTab(i).textArea.setTabsEmulated(isSelected); break;
                case 5: getTab(i).textArea.setCodeFoldingEnabled(isSelected); break;
            }
        }
        tabOptions[option] = isSelected;
    }
    
    /**
     * Help Actions
     */
    Action contentsAction = new AbstractAction("Contents...") {
        public void actionPerformed(ActionEvent e) {
            //HelpDialog dlg = new HelpDialog(parent);
            
            HelpDialog dlg = new HelpDialog(MainTextPad.getInstance());
            //showWIP("Help Contents");
            
        }
    };
    
    Action aboutAction = new AbstractAction("About " + APP_NAME) {
        public void actionPerformed(ActionEvent e) {
            AboutDialog dlg = new AboutDialog(MainTextPad.getInstance());
            /*
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 1));
            panel.add(new JLabel(MainTextPad.APP_NAME + " " + VERSION));
            panel.add(new JLabel("By WillyG Productions"));
            panel.add(new JLabel("Powered by Robert Futrell's RSyntaxTextArea"));
            JOptionPane.showMessageDialog(MainTextPad.this, panel, "About " + APP_NAME, 
                    JOptionPane.INFORMATION_MESSAGE);*/
        }
    };
    
    /**
     * UDK Actions (Windows Only)
     */
    
    Action setDirectoryAction = new AbstractAction("Set UDK Directory") {
        {putValue(Action.SMALL_ICON, Utils.loadImage("/images/udk.png"));}
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setSelectedFile(UDKdir);
            int choice = chooser.showOpenDialog(MainTextPad.this);
            File file = chooser.getSelectedFile();
            if(choice == JFileChooser.APPROVE_OPTION) {
                validateUDKDir(file);
            }
        }
    };
    
    Action compileAction = new AbstractAction("Compile...") {
        {putValue(Action.SMALL_ICON, Utils.loadImage("/images/compile.png"));}
        public void actionPerformed(ActionEvent e) {
            if(!validateFile())
                return;
            String filePath = getWorkingFile(-1).getAbsolutePath();
            filePath = filePath.substring(0, filePath.indexOf("Development"));
            String[] args = {"cd " + filePath + "Binaries\\Win32", "UDK make"};
            //FULL RECOMPILE : "UDK make -full"
            commandClient.runClient(args);
        }
    };
    
    Action playAction = new AbstractAction("Play...") {
        {putValue(Action.SMALL_ICON, Utils.loadImage("/images/play.png"));}
        public void actionPerformed(ActionEvent e) {
            if(!validateFile())
                return;
            String filePath = getWorkingFile(-1).getAbsolutePath();
            filePath = filePath.substring(0, filePath.indexOf("Development"));
            String[] args = {"cd " + filePath + "Binaries\\Win32", "UDK"};
            commandClient.runClient(args);
        }
    };
    
    // TODO: LAUNCH EDITOR CMD: "UDK editor"
    
    
    /**
     * MUST be here! Has references to above functions that breaks it
     * if it is moved to the top.
     */
    JFindDialog findDialog = new JFindDialog(this);
    
    
    
    public void showWIP(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(new JLabel("This is a work in progress."));
        panel.add(new JLabel("Sorry for the inconvenience."));
        JOptionPane.showMessageDialog(MainTextPad.this, panel, title,
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    
    public void validateUDKDir(File file) {
        boolean error = false;
        if(!file.getName().equals("UDK"))
            error = true;
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                if(pathname.getName().indexOf("UDK-") >= 0 && pathname.isDirectory())
                    return true;
                return false;
            }
        };
        if(file.listFiles(filter).length == 0)
            error = true;
        if(!error) {
            UDKdir = file;
            JOptionPane.showMessageDialog(MainTextPad.this, "UDK directory successfully set!",
                    "Set UDK Directory", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Utils.showError("Not a valid UDK directory");
        }
    }
    
    /**
     * Validates not only if UDK directory is set, but whether open file is
     * not null and if it is in the directory
     * @return 
     */
    public boolean validateFile() {
        if(UDKdir == null) {
            Utils.showError("UDK directory not set");
            return false;
        }
        if(getWorkingFile(-1) == null) {
            Utils.showError("You must have a file open");
            return false;
        }
        String filePath = getWorkingFile(-1).getAbsolutePath();
        if(filePath.indexOf(UDKdir.getAbsolutePath()) == -1) {
            Utils.showError("File not in recognizable UDK directory");
            return false;
        }
        return true;
    }
    

    public AbstractCompletionProvider getCompletionProvider() {
        return provider;
    }

    void saveFile(File file) {
        if(!getTab(-1).saveFile(file))
            JOptionPane.showMessageDialog(MainTextPad.this,
                    "Can't write to file " + file.getName(),
                    APP_NAME,JOptionPane.ERROR_MESSAGE);
    }

    // false if cancel, true if no/save
    boolean ensureNoLoss(int index) throws RuntimeException {
        int currentTextHash = ((UnrealTab) openTabs.get(index)).textArea.getText().hashCode();
        if(currentTextHash != ((UnrealTab) openTabs.get(index)).textHash) {
            int answer = JOptionPane.showConfirmDialog(MainTextPad.this,
                    "Save changes to " + getDocumentTitle(index) + "?",APP_NAME,
                    JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
            if(answer == JOptionPane.NO_OPTION) {
                return true;
            } else if(answer == JOptionPane.YES_OPTION) {
                saveAction.actionPerformed(null);
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public File getCurrentDirectory() {
        return fileChooser.getCurrentDirectory();
    }

    public void setCurrentDirectory(File dir) {
        fileChooser.setCurrentDirectory(dir);
    }

    public MainTextPad() {
        initComponents();
    }

    /**
     * Edit passed file, or if a directory, set current directory to it
     * @param workingFile
     */
    public MainTextPad(File workingFile) {
        initComponents();
        if(workingFile.isDirectory()) {
            setCurrentDirectory(workingFile);
        } else {
            setCurrentDirectory(workingFile.getParentFile());
            addTab(workingFile);
        }
    }
    
    // -1 means return selected tab
    public UnrealTab getTab(int index) {
        index = (index == -1) ? tabbedPane.getSelectedIndex() : index;
        if(openTabs.size() != 0)
            return (UnrealTab) openTabs.get(index);
        return null;
    }
    
    // -1 means return selected file
    public File getWorkingFile(int index) {
        if(getTab(index) != null)
            return getTab(index).getWF();
        return null;
    }

    private String getDocumentTitle(int index) {
        return getTab(index).getDocumentTitle();
    }

    public void updateTerminationStyle() {
        if(getTab(-1).getIsPC()) {
            pcTerminationStyle.setSelected(true);
        } else {
            unixTerminationStyle.setSelected(true);
        }
    }
    public void updateStatusBar() {
        int line = getTab(-1).textArea.getCaretLineNumber() + 1;
        int col = getTab(-1).textArea.getCaretOffsetFromLineStart() + 1;
        //pcTerminationStyle.setSelected(getTab(-1).getIsPC());
        String style = getTab(-1).getIsPC() ? "PC" : "UNIX";
        statusBar.setText("UTF-8 | " + style + " | Ln " + line + ", Col " + col);
    }

    private static boolean setApplicationModalExclusion(JFrame frmMain) {
        try {
            Class exclusionType = Class.forName("java.awt.Dialog$ModalExclusionType");
            Field field = exclusionType.getField("APPLICATION_EXCLUDE");
            Object value = field.get(exclusionType);
            Method meth = JFrame.class.getMethod("setModalExclusionType",new Class[]{exclusionType});
            meth.invoke(frmMain, value);
            return true;
        } catch (Exception e) {}
        return false;
    }
    
    
    
    public void openSettings() {
        File toOpen = new File(System.getProperty("user.home") + 
                System.getProperty("file.separator") + "userSettings.onyx");
        if(!toOpen.exists())
            return;
        ArrayList<String> mySettings = new ArrayList<String>();
        try {
            FileInputStream fstream = new FileInputStream(toOpen);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            int i = 0;
            while((strLine = br.readLine()) != null) {
                //mySettings.add(AES.performAES(strLine, false));
                mySettings.add(strLine);
                i++;
            }
            in.close();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this,"ERROR: Could not load user settings.");
        }
        
        currentOnyxTheme = Integer.parseInt(mySettings.get(0));
        tabOptions = Utils.intToBooleanArray(Integer.parseInt(mySettings.get(1)), tabOptions.length);
        recentMenu.loadEntries(mySettings.get(2).split("%"));
        
        
        // UDK dir saved
        if(mySettings.size() == 4)
            UDKdir = new File(mySettings.get(3));
        
        updateLookAndFeel(currentOnyxTheme);
        
        lineWrapAction.setSelected(tabOptions[0]);
        lineWrapAction.actionPerformed(null);
        lineNumbersAction.setSelected(tabOptions[1]);
        lineNumbersAction.actionPerformed(null);
        whiteSpaceAction.setSelected(tabOptions[2]);
        whiteSpaceAction.actionPerformed(null);
        highlightCurrentLineAction.setSelected(tabOptions[3]);
        highlightCurrentLineAction.actionPerformed(null);
        emulatedTabs.setSelected(tabOptions[4]);
        emulatedTabs.actionPerformed(null);
        codeFoldingAction.setSelected(tabOptions[5]);
        codeFoldingAction.actionPerformed(null);
        
        for(Enumeration e = lafGroup.getElements(); e.hasMoreElements();) {
            JRadioButtonMenuItem b = (JRadioButtonMenuItem)e.nextElement();
            if(b.getText().equals(UnrealEditor.guiProps.getFriendlyName())) {
                lafGroup.setSelected(b.getModel(), true);
                break;
            }
        }
    }
    
    
    
    public void saveSettings() {
        File toSave = new File(System.getProperty("user.home") + 
                System.getProperty("file.separator") + "userSettings.onyx");
        
        ArrayList<String> mySettings = new ArrayList<String>();
        mySettings.add(Integer.toString(currentOnyxTheme));
        mySettings.add(Integer.toString(Utils.booleanArrayToInt(tabOptions)));
        mySettings.add(recentMenu.saveEntries());
        
        if(UDKdir != null)
            mySettings.add(UDKdir.getAbsolutePath());
        try {
            FileWriter fstream = new FileWriter(toSave);
            BufferedWriter out = new BufferedWriter(fstream);
            for(int i = 0; i < mySettings.size(); i++) {
                out.write(mySettings.get(i));
                //out.write(AES.performAES(mySettings.get(i), true));
                out.newLine();
            }
            out.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"ERROR: Could not save user settings.");
        }
    }
    
    
    

    private void initComponents() {
        // Initialize token factory so we can add a hook to UScript
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("SYNTAX_STYLE_USCRIPT", UScriptTokenMaker.class.getName());
        TokenMakerFactory.setDefaultInstance(atmf);
        
        commandClient = new CmdClient();
        
        setApplicationModalExclusion(this);
        setTitle(APP_NAME);
        this.instance = this;
        
        initMenuBar();
        initAutoCompletion();
        initMainPane();
        initStatusBar();
        
        pack();
        setExtendedState(MAXIMIZED_BOTH);
        setIconImage(Utils.loadImage("/images/logo.gif").getImage());
        
        openSettings();
    }
    
    private void initMainPane() {
        //tabbedPane = new DnDTabbedPane();
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addTab(null);
        ChangeListener switchTabListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                if(tabbedPane.getTabCount() != 0) {
                    updateTerminationStyle();
                    updateStatusBar();
                }
            }
        };
        tabbedPane.addChangeListener(switchTabListener);
        
        SideTree sideTree = new SideTree();
        
        // COMPILER WINDOW
        compilerArea = new JTextArea();
        compilerArea.setEditable(false);
        
        JScrollPane compilerPane = new JScrollPane(compilerArea);
        compilerPane.setBorder(JTBorderFactory.createTitleBorder(Utils.loadImage("/images/tree.gif"), "Compiler", 0, 0));
        
        // CODING/COMPILER SPLIT
        JSplitPane codeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tabbedPane, compilerPane);
        
        codeSplitPane.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().height/2);
        codeSplitPane.setOneTouchExpandable(true);
        
        // TREE/WORK AREA SPLIT
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sideTree, codeSplitPane);
        splitPane.setDividerLocation(270);
        splitPane.setOneTouchExpandable(true);
        
        // TOOLBAR
        toolBar = new MainToolBar();
        
        // MAIN PANEL
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(toolBar, BorderLayout.NORTH);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        setContentPane(contentPanel);
    }
    
    private void initAutoCompletion() {
        
        provider.setListCellRenderer(new DefaultListCellRenderer() {
            private final Color brown = new Color(140, 110, 50);
            private final Color darkGreen = new Color(50, 140, 50);
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(!isSelected) {
                    if(value instanceof MarkupTagCompletion) {
                        comp.setForeground(Color.blue);
                    } else if ((value instanceof VariableCompletion)) {
                        comp.setForeground(darkGreen);
                    }
                }
                return comp;
            }
        });
        
        
        
        InputStream in = Utils.class.getResourceAsStream("/unrealeditor/c.xml");
        try {
            if(in!=null) {
                provider.loadFromXML(in);
                in.close();
            } else {
                provider.loadFromXML(new File("c.xml"));
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
      
      
        
        
        autoCompletion = new AutoCompletion(provider) {
            @Override
            protected String getReplacementText(Completion c, Document doc, int start, int len) {
                try {
                    if(start >= 2 && "${".equals(doc.getText(start - 2, 2)) && !endsWithCurlyBrace(doc, start)) {
                        return c.getReplacementText() + "}";
                    } else {
                        return super.getReplacementText(c, doc, start, len);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        autoCompletion.setShowDescWindow(true);
        autoCompletion.setDescriptionWindowSize(400, 400);
        autoCompletion.setParameterAssistanceEnabled(true);
        autoCompletion.setExternalURLHandler(new ExternalURLHandler() {
            public void urlClicked(URL url) {
                System.out.println("URL clicked: " + url.toString());
                BrowserLauncher.openURL(url.toString());
            }
        });
    }
    
    private void initStatusBar() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 2, 6));
        southPanel.add(statusBar, BorderLayout.BEFORE_LINE_BEGINS);
        southPanel.add(new JLabel(" "), BorderLayout.AFTER_LINE_ENDS);
        statusBar.setText("Welcome to Onyx!");
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void initMenuBar() {
        
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        tabsMenu = new JMenu("Tabs");
        lineTerminationMenu = new JMenu("Line termination");
        viewMenu = new JMenu("View");
        themeMenu = new JMenu("Set Theme");
        helpMenu = new JMenu("Help");
        
        // FILE
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openAction));
        recentMenu = new RecentMenu() {
            public void onSelectFile(String filePath) {
                addTab(new File(filePath));
            }
        };
        fileMenu.add(recentMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(closeAction));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(saveAction));
        fileMenu.add(new JMenuItem(saveAsAction));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(exitAction));

        // EDIT
        editMenu.add(new JMenuItem(undoAction));
        editMenu.add(new JMenuItem(redoAction));
        editMenu.add(new JSeparator());
        editMenu.add(new JMenuItem(cutAction));
        editMenu.add(new JMenuItem(copyAction));
        editMenu.add(new JMenuItem(pasteAction));
        editMenu.add(new JSeparator());
        editMenu.add(new JMenuItem(findAction));
        editMenu.add(new JMenuItem(findNextAction));
        editMenu.add(new JMenuItem(replaceAction));
        editMenu.add(new JMenuItem(goToAction));
        editMenu.add(new JSeparator());
        tabsMenu.add(tabSizeAction);
        tabsMenu.add(emulatedTabs.createJCheckBoxMenuItem());
        tabsMenu.add(convertTabsToSpaces);
        tabsMenu.add(convertSpacesToTabs);
        editMenu.add(tabsMenu);
        lineTerminationMenu.add(pcTerminationStyle);
        lineTerminationMenu.add(unixTerminationStyle);
        editMenu.add(lineTerminationMenu);
        editMenu.add(new JSeparator());
        editMenu.add(new JMenuItem(completeCodeAction));
        editMenu.add(new JMenuItem(selectAllAction));

        viewMenu.add(lineWrapAction.createJCheckBoxMenuItem());
        viewMenu.add(lineNumbersAction.createJCheckBoxMenuItem());
        viewMenu.add(whiteSpaceAction.createJCheckBoxMenuItem());
        viewMenu.add(highlightCurrentLineAction.createJCheckBoxMenuItem());
        viewMenu.add(codeFoldingAction.createJCheckBoxMenuItem());
        setUpThemeMenu();
        viewMenu.add(themeMenu);
        
        helpMenu.add(new JMenuItem(contentsAction));
        helpMenu.add(new JSeparator());
        helpMenu.add(new JMenuItem(aboutAction));
        
        

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        
        
        //not for now for testing...
        if(UnrealEditor.isWindows || UnrealEditor.isDevBuild) {
            windowsMenu = new JMenu("UDK");
            windowsMenu.add(new JMenuItem(setDirectoryAction));
            windowsMenu.add(new JMenuItem(compileAction));
            windowsMenu.add(new JMenuItem(playAction));
            menuBar.add(windowsMenu);
        }
        
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        {
            ActionListener terminationStyleListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getTab(-1).setIsPC(pcTerminationStyle.isSelected());
                    updateStatusBar();
                }
            };
            pcTerminationStyle.addActionListener(terminationStyleListener);
            unixTerminationStyle.addActionListener(terminationStyleListener);
        }
    }
    
    private void setUpThemeMenu() {
        addThemeRadioButton(0);
        themeMenu.add(new JSeparator());
        for(int i = 1; i < GUIProperties.defaults.length; i++)
            addThemeRadioButton(i);
    }
    
    private void addThemeRadioButton(int myTh) {
        final int myTheme = myTh;
        JRadioButtonMenuItem radioMenuItem = new JRadioButtonMenuItem(GUIProperties.defaults[myTheme][0]);
        radioMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateLookAndFeel(myTheme);
            }
        });
        radioMenuItem.setSelected(UnrealEditor.guiProps.isOnyxTheme(myTheme));
        lafGroup.add(radioMenuItem);
        themeMenu.add(radioMenuItem);
    }

    /**
     * Tell if the current code section is in a variable ending with a }
     */
    private boolean endsWithCurlyBrace(Document doc, int offset) throws BadLocationException {
        int len = doc.getLength();
        int remaining = len - offset;
        if(remaining > 100)
            remaining = 100;
        String text = doc.getText(offset, remaining);
        char[] chars = text.toCharArray();
        for(char c : chars) {
            if(c == '}')
                return true;
            if(!Character.isJavaIdentifierPart(c))
                return false;
        }
        return false;
    }

    /**
     * Overridden so we can ask before exiting.
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if(e.getID() == WindowEvent.WINDOW_CLOSING) {
            exitAction.actionPerformed(null);
        } else {
            super.processWindowEvent(e);
        }
    }
    
    
    // ~WillyG - Add a tab to the pane
    public void addTab(File tabFile) {
        
        if(tabFile != null) {
            int i = isAlreadyOpen(tabFile);
            if(i > -1) {
                tabbedPane.setSelectedIndex(i);
            } else {
                UnrealTab newTab = new UnrealTab(this, tabFile);
                openTabs.add(newTab);
                
                if(!newTab.openFile(tabFile))
                    JOptionPane.showMessageDialog(MainTextPad.this,"Can't open file " 
                            + tabFile.getName(),APP_NAME,JOptionPane.ERROR_MESSAGE);
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            }
            recentMenu.addEntry(tabFile);
            
        } else {
            UnrealTab newTab = new UnrealTab(this, null);
            openTabs.add(newTab);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
        }
        
    }
    
    //returns -1 if file not already open, otherwise index
    private int isAlreadyOpen(File theFile) {
        File pointerFile;
        for(int i = 0; i < openTabs.size(); i++) {
            pointerFile = ((UnrealTab) openTabs.get(i)).getWF();
            if(pointerFile == null)
                continue;
            if(pointerFile.equals(theFile))
                return i;
        }
        return -1;
    }
    
    // Haha! Screw you static functions!!!
    public static MainTextPad getInstance() {
        if(instance == null)
            instance = new MainTextPad();
        return instance;
    }
    
    /**
     * Updates the look and feel of the application. Note that if the update
     * fails, we must reset the old theme via currentOnyxTheme.
     */
    public void updateLookAndFeel(int ot) {
        UnrealEditor.guiProps.setOnyxTheme(ot);
        try {
            UnrealEditor.guiProps.initializeTheme();
            UIManager.setLookAndFeel(UnrealEditor.guiProps.getLookAndFeel());
            getRootPane().updateUI();
        } catch (Exception ex) {
            UnrealEditor.guiProps.setOnyxTheme(currentOnyxTheme);
            System.err.println("[MainTextPad] Error: Failed to load look and feel");
            return;
        }
        currentOnyxTheme = UnrealEditor.guiProps.getOnyxTheme();
    }
}