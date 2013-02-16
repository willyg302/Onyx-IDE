package unrealeditor;

import java.beans.Beans;
import java.io.File;
import javax.swing.UIManager;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;

public class UnrealEditor {
    static boolean runsStandalone = false;
    public static GUIProperties guiProps = new GUIProperties();
    public static MainTextPad app;
    public static boolean isWindows, isMac;
    
    // Whether this is a dev build (enables Windows-only stuff on Mac)
    public static boolean isDevBuild = false;
    
    // So that initApp can still grab the command line even though it is housed
    // in dynamic Runnable();
    public static String[] argTemp;
    
    public static void main(String[] args) {
        argTemp = args;
        if(!validateApp())
            return;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {initApp();}
        });
    }
    
    public static boolean validateApp() {
        String id = UnrealEditor.class.getName();
        boolean start;
        try {
            JUnique.acquireLock(id, new MessageHandler() {
                public String handle(String message) {
                    if(app != null) {
                        app.addTab(new File(message));
                        int i = app.getExtendedState();
                        app.setExtendedState(MainTextPad.ICONIFIED);
                        app.setExtendedState(i);
                    }
                    return null;
                }
            });
            start = true;
        } catch (AlreadyLockedException e) {
            // Application already running.
            start = false;
        }
        if (!start && argTemp.length != 0) {
            // Sends arguments to the already active instance.
            for (int i = 0; i < argTemp.length; i++) {
                JUnique.sendMessage(id, argTemp[i]);
            }
        }
        return start;
    }

    public static void initApp() {    
        runsStandalone = true;
        
        isWindows = (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
        isMac = (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
        
        // So Apple-Q is intercepted, plus a few other things
        if(isMac) {
            try {
                Beans.instantiate(UnrealEditor.class.getClassLoader(), "unrealeditor.SpecialMacHandler");
            } catch (Exception ex) {
                System.err.println("Failed to load extended Mac support.");
            }
        }
        try {
            guiProps.initializeTheme();
            UIManager.setLookAndFeel(guiProps.getLookAndFeel());
        } catch (Exception ex) {}
        app = new MainTextPad();
        app.setVisible(true);
        
        if(argTemp.length != 0) {
            for(String s : argTemp)
                app.addTab(new File(s));
        }
    }
}