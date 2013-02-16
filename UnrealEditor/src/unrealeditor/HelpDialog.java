package unrealeditor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class HelpDialog extends JDialog implements HyperlinkListener {
    
    private final String HELP_PATH = "/docs/";
    private final String HOME_PAGE = HELP_PATH + "home.html";
    private final String INDEX_PROPERTIES = HELP_PATH + "index.properties";
    private final int MAX_HISTORY_SIZE = 30;
    
    private JButton prevButton, nextButton, homeButton, exitButton;
    
    private JToolBar toolBar;
    private JTree indexTree;
    private JEditorPane helpPane;
    
    private ArrayList<TreePath> helpStack;
    private int helpStackIndex;
    private boolean blockHelpStack = false;
    
    public HelpDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent), "Help", true);
        helpStack = new ArrayList<TreePath>();
        helpStackIndex = 0;
        JPanel contentPanel = new JPanel(new BorderLayout());
        initToolBar();
        initIndexTree();
        initHelpPane();
        JPanel indexPanel = new JPanel(new BorderLayout());
        indexPanel.add(toolBar, BorderLayout.NORTH);
        indexPanel.add(new JScrollPane(indexTree), BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, indexPanel, new JScrollPane(helpPane));
        contentPanel.add(splitPane, BorderLayout.CENTER);
        setContentPane(contentPanel);
        showDlg();
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            try {
                pane.setPage(e.getURL());
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    private void showDlg() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = new Dimension(860, 640);
        int dlgPosX = (screenSize.width - dlgSize.width) / 2;
        int dlgPosY = (screenSize.height - dlgSize.height) / 2;
        setLocation(dlgPosX, dlgPosY);
        setSize(dlgSize);
        setResizable(false);
        setVisible(true);
        pack();
    }
    
    private void initToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        prevButton = new NoFocusButton(Utils.loadImage("/images/prev.gif"));
        prevButton.setToolTipText("Previous Page");
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(helpStackIndex > 0) {
                    blockHelpStack = true;
                    helpStackIndex--;
                    indexTree.setSelectionPath(helpStack.get(helpStackIndex));
                    blockHelpStack = false;
                }
            }
        });
        
        nextButton = new NoFocusButton(Utils.loadImage("/images/next.gif"));
        nextButton.setToolTipText("Next Page");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(helpStackIndex < (helpStack.size() - 1)) {
                    blockHelpStack = true;
                    helpStackIndex++;
                    indexTree.setSelectionPath(helpStack.get(helpStackIndex));
                    blockHelpStack = false;
                }
            }
        });
        
        homeButton = new NoFocusButton(Utils.loadImage("/images/home.gif"));
        homeButton.setToolTipText("Home Page");
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                indexTree.setSelectionRow(0);
            }
        });
        
        exitButton = new NoFocusButton(Utils.loadImage("/images/exit.gif"));
        exitButton.setToolTipText("Quit Help");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        toolBar.add(prevButton);
        toolBar.add(nextButton);
        toolBar.add(homeButton);
        toolBar.addSeparator(new Dimension(110, 8));
        toolBar.add(exitButton);
    }
    
    private void initIndexTree() {
        DefaultMutableTreeNode home = new DefaultMutableTreeNode(new TreeNodeData("Home", "home.html"));
        DefaultMutableTreeNode nodeI = null;
        try {
            URL url = getClass().getResource(INDEX_PROPERTIES);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s = null;
            while((s = reader.readLine()) != null) {
                s = s.trim();
                if((s.length() > 0) && (s.charAt(0) != '#')) {
                    int posCategory = s.indexOf(';');
                    int posFileName = s.indexOf('=');
                    String category = s.substring(0, posCategory);
                    String displayString = s.substring(posCategory + 1, posFileName);
                    String fileName = s.substring(posFileName + 1);
                    //B=branch, L=leaf
                    if (category.equals("B")) {
                        nodeI = new DefaultMutableTreeNode(new TreeNodeData(displayString, fileName));
                        home.add(nodeI);
                    } else if (category.equals("L")) {
                        home.add(new DefaultMutableTreeNode(new TreeNodeData(displayString, fileName)));
                    } else if (category.equals("BL")) {
                        nodeI.add(new DefaultMutableTreeNode(new TreeNodeData(displayString, fileName)));
                    }
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        indexTree = new JTree(home);
        indexTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        indexTree.requestFocus();
        indexTree.setSelectionRow(0);
        helpStack.add(indexTree.getSelectionPath());
        updateHelpStack();
        indexTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selPath = indexTree.getSelectionPath();
                if(selPath != null) {
                    DefaultMutableTreeNode selPage = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                    TreeNodeData nodeData = (TreeNodeData) selPage.getUserObject();
                    try {
                        helpPane.setPage(getClass().getResource(nodeData.getURL()));
                        if(!blockHelpStack) {
                            helpStack.add(selPath);
                            if(helpStack.size() > MAX_HISTORY_SIZE)
                                helpStack.remove(0);
                            helpStackIndex = helpStack.size() - 1;
                        }
                        updateHelpStack();
                    } catch(Exception ex) {
                        helpPane.setText("<html><h2>ERROR: " + nodeData.getURL() + "</h2></html>");
                    }
                    helpPane.validate();
                }
            }
        });
    }
    
    private void initHelpPane() {
        try {
            URL url = getClass().getResource(HOME_PAGE);
            helpPane = new JEditorPane(url) {
                public void paint(Graphics g) {
                    Graphics2D g2D = (Graphics2D)g;
                    g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paint(g);
                }
            };
            helpPane.setEditable(false);
            helpPane.addHyperlinkListener(this);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateHelpStack() {
        prevButton.setEnabled((helpStack.size() > 1) && (helpStackIndex > 0));
        nextButton.setEnabled((helpStack.size() > 1) && (helpStackIndex < (helpStack.size() - 1)));
    }
    
    private class NoFocusButton extends JButton {
        public NoFocusButton(ImageIcon icon) {
            super(icon);
            setFocusPainted(false);
            setRolloverEnabled(true);
        }
        public boolean isFocusTraversable() {return false;}
        public void requestFocus() {}
    }
    
    private class TreeNodeData {
        private String displayString = null;
        private String url = null;
        
        public TreeNodeData(String aDisplayString, String fileName) {
            displayString = aDisplayString;
            url = HELP_PATH + fileName;
        }
        
        public String getDisplayString() {return displayString;}
        public String getURL() {return url;}
        public String toString() {return displayString;}
    }
}