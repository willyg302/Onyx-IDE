package unrealeditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
//import se.datadosen.component.RiverLayout;

public class JFindDialog extends JDialog {
    
    enum Mode {find, replace};
    
    private MainTextPad owner;
    JTextField toFind = new JTextField(15);
    JLabel toReplaceLabel = new JLabel("Replace with:");
    JTextField replaceWith = new JTextField(15);
    
    JCheckBox matchCase = new JCheckBox("Match case");
    JCheckBox wholeWords = new JCheckBox("Whole words");
    JCheckBox regExp = new JCheckBox("Regular expression", true);
    JRadioButton up = new JRadioButton("Up");
    JRadioButton down = new JRadioButton("Down", true);

    JButton replaceNextButton;
    JButton replaceAllButton;
    
    Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    };
        
    public JFindDialog(MainTextPad owner) {
        super(owner, "Find", false);
        this.owner = owner;
        init();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        toFind.requestFocusInWindow();
    }
    
    private void init() {
        {
            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            JComponent pane = getLayeredPane(); // So pressing escape won't conflict with menus
            pane.registerKeyboardAction(cancelAction, "cancel", esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        
        {
            ButtonGroup bg = new ButtonGroup();
            bg.add(up);
            bg.add(down);
        }
        JButton findNextButton = new JButton(owner.findNextAction);
        this.getRootPane().setDefaultButton(findNextButton);
        replaceNextButton = new JButton(owner.replaceNextAction);
        replaceAllButton = new JButton(owner.replaceAllAction);
        
        // Layout components
        setLayout(new BorderLayout());
        JPanel c = new JPanel();
        c.setLayout(new RiverLayout());
        c.add(new JLabel("Find what:"));
        c.add("tab hfill", toFind);
        c.add("br", toReplaceLabel);
        c.add("tab hfill", replaceWith);
        JPanel cbPanel = new JPanel();
        cbPanel.setLayout(new GridLayout(0,1));
        cbPanel.add(matchCase);
        cbPanel.add(wholeWords);
        cbPanel.add(regExp);
        c.add("br", cbPanel);
        ControlPanel directionPanel = new ControlPanel("Direction");
        directionPanel.add(up);
        directionPanel.add(down);
        c.add(directionPanel);
        
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new RiverLayout(4,2));
        actionPanel.add("hfill", findNextButton);
        actionPanel.add("br hfill", replaceNextButton);
        actionPanel.add("br hfill", replaceAllButton);
        actionPanel.add("br hfill", new JButton(cancelAction));
        
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(4,2,4,2));
        content.add(c, BorderLayout.CENTER);
        content.add(actionPanel, BorderLayout.AFTER_LINE_ENDS);
        getContentPane().add(content, BorderLayout.CENTER);
        
        setMode(Mode.find);
    }
    
    public void setMode(Mode mode) {
        boolean replaceMode = (mode == Mode.replace);
        setTitle(replaceMode ? "Replace" : "Find");
        toReplaceLabel.setVisible(replaceMode);
        replaceWith.setVisible(replaceMode);
        replaceNextButton.setVisible(replaceMode);
        replaceAllButton.setVisible(replaceMode);
        pack();
    }
    
    class ControlPanel extends JPanel {
        public ControlPanel(String title) {
            setLayout(new RiverLayout());
            setBorder(BorderFactory.createTitledBorder(title));
        }
    }
}