package unrealeditor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.w3c.dom.Element;
import unrealeditor.searchable.SearchBoxController;
import unrealeditor.searchable.SearchableJTree;

/**
 * Handles the side bar (class tree and hopefully source browser).
 * @author William
 */
public class SideTree extends JTabbedPane {
    
    public DOMTreeFull classtree;
    
    public SideTree() {
        classtree = new DOMTreeFull(Utils.class.getResourceAsStream("/unrealeditor/packagetree.xml"), true);
        classtree.setShowsRootHandles(true);
        classtree.setEditable(false);
        classtree.setToggleClickCount(0);
        classtree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                TreePath selPath = classtree.getSelectionPath();
                if(classtree.getRowForLocation(e.getX(), e.getY()) != -1) {
                    if(e.getClickCount() == 2) {
                        String path = ((Element)classtree.getNode(selPath.getLastPathComponent())).getAttribute("href");
                        BrowserLauncher.openURL("http://uncodex.com/2011-08/" + path);
                    }
                }
            }
        });
        
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)classtree.getCellRenderer();
        ImageIcon gearIcon = Utils.loadImage("/images/gear.png");
        
        renderer.setLeafIcon(gearIcon);
        renderer.setClosedIcon(gearIcon);
        renderer.setOpenIcon(gearIcon);
        
        classtree.setRowHeight(18);
        
        SearchBoxController controller = SearchableJTree.makeSearchable(classtree);
        controller.setSelectAllMatches(false);
        
        /*
         * TO SET THE COLORS OF THE BOX:
         * controller.getSearchBoxUI().setBackground(Color.black);
         * controller.getSearchBoxUI().setForeground(Color.white);
         * 
         * SET OPACITY:
         * controller.getSearchBoxUI().setTransparency((float)((JSlider)e.getSource()).getValue()/100);
         */
        
        // TREE
        addTab("Class Tree", Utils.loadImage("/images/tree.gif"), new JScrollPane(classtree));
        addTab("Source Browser", new JScrollPane());
    }
}