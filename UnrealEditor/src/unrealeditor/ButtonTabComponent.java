package unrealeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Tab with a close button
 */
public class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;
    private JLabel label = null;
    private final MainTextPad pad;
    
    private static ImageIcon closerImage = Utils.loadImage("/images/closer.gif");
    private static ImageIcon closerRolloverImage = Utils.loadImage("/images/closer_rollover.gif");
    private static ImageIcon closerPressedImage = Utils.loadImage("/images/closer_pressed.gif");
    private JButton closeButton = null;
     
    
    public ButtonTabComponent(String title, final MainTextPad pad) {
        super(new BorderLayout());
        
        if(pad.tabbedPane == null)
            throw new NullPointerException("TabbedPane is null");
        this.pane = pad.tabbedPane;
        this.pad = pad;
        
        
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        label = new JLabel(title);
        label.setOpaque(false);
        
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        

        closeButton = new JButton(closerImage);
        closeButton.setRolloverIcon(closerRolloverImage);
        closeButton.setPressedIcon(closerPressedImage);
        closeButton.setBorderPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setFocusPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setPreferredSize(new Dimension(closerImage.getIconWidth(), closerImage.getIconHeight()));
        closeButton.setSize(new Dimension(closerImage.getIconWidth(), closerImage.getIconHeight()));
        
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if(i != -1)
                    pad.closeTab(i);
            }
        });
            
        add(label, BorderLayout.CENTER);
        add(closeButton, BorderLayout.EAST);
    }
    
    public String getText() {
        return label.getText();
    }

    public void setNewLabel(String newLabel, Color newColor) {
        label.setText(newLabel);
        label.setForeground(newColor);
    }
}