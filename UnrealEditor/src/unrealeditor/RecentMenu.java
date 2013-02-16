package unrealeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public abstract class RecentMenu extends JMenu {
    private final static int maxCount = 10;
    private ArrayList<File> recentEntries;
    
    public RecentMenu() {
        super();
        setText("Open Recent");
        recentEntries = new ArrayList<File>();
        setEnabled(false);
    }
    
    public void loadEntries(String[] entries) {
        if(entries.length == 0)
            return;
        for(int i = 0; i < entries.length; i++) {
            File f = new File(entries[i]);
            if(f.exists())
                addEntry(f);
        }
    }
    
    public String saveEntries() {
        String ret = "";
        if(recentEntries.size() == 0)
            return "%";
        for(File f : recentEntries)
            ret = ret + f.getAbsolutePath() + "%";
        return ret;
            
    }

    public void addEntry(File f) {
        setEnabled(true);
        removeAll();
        recentEntries.remove(f);
        recentEntries.add(0, f);
        if(recentEntries.size() > maxCount)
            recentEntries.remove(maxCount);
        
        for(int i = 0; i< recentEntries.size(); i++) {
            JMenuItem menuItem = new JMenuItem();
            menuItem.setText(recentEntries.get(i).getName());
            menuItem.setToolTipText(recentEntries.get(i).getAbsolutePath());
            menuItem.setActionCommand(recentEntries.get(i).getAbsolutePath());
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    onSelectFile(actionEvent.getActionCommand());
                }
            });
            add(menuItem);
        }
    }

    public abstract void onSelectFile(String filePath);
}