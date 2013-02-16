package unrealeditor;

import java.io.IOException;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class AboutDialog extends JDialog {
    
    JTabbedPane tabbedPane = new JTabbedPane();
        
    
    public AboutDialog(Component aParent) {
        super(JOptionPane.getFrameForComponent(aParent), "About", true);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(new Insets(10, 0, 0, 0)));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        
        addPane("About", "/docs/about.html", 0);
        addPane("License", "/docs/license.txt", 1);
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(contentPanel);
        showDlg();
    }
    
    private void showDlg() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = new Dimension(540, 380);
        int dlgPosX = (screenSize.width - dlgSize.width) / 2;
        int dlgPosY = (screenSize.height - dlgSize.height) / 2;
        setLocation(dlgPosX, dlgPosY);
        setSize(dlgSize);
        setResizable(false);
        setVisible(true);
        pack();
    }
    
    private void addPane(String title, String text, int index) {
        JEditorPane thePane = new JEditorPane();
        thePane.setEditable(false);
        thePane.setContentType("text/html");
        try {
            thePane.setPage(Utils.class.getResource(text));
        } catch (IOException ex) {
            thePane.setText("ERROR: Unable to load " + text);
        }
        
        
        thePane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        thePane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if(HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
                    BrowserLauncher.openURL(hle.getURL().toString());
            }
        });
        JScrollPane theScroll = new JScrollPane(thePane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        theScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tabbedPane.add(title, theScroll);
    }
}