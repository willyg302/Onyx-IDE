package unrealeditor;

import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.apple.eawt.OpenFilesHandler;
import java.io.File;
import java.util.List;

public class SpecialMacHandler implements ApplicationListener {
    public SpecialMacHandler() {
        setSystemProperties();
        registerAsApplicationListener(this);
    }

    private static void registerAsApplicationListener(ApplicationListener l) {
        Application app = Application.getApplication();
        app.addApplicationListener(l);
        
        app.setOpenFileHandler(new OpenFilesHandler() {

            public void openFiles(OpenFilesEvent ofe) {
                List<File> files = ofe.getFiles();
                for(File file : files) {
                    MainTextPad.getInstance().addTab(file);
                }
            }
        });
        
        app.setDockIconImage(Utils.loadImage("/images/logo.gif").getImage());
        
    }

    private static void setSystemProperties() {
        //System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", MainTextPad.APP_NAME);
        System.setProperty("apple.awt.fileDialogForDirectories", "true");

        // Set L&F settings
        System.setProperty("apple.awt.antialiasing", "on");
        System.setProperty("apple.awt.interpolation", "bilinear");

        // Turn off AWT compatibility mode (operations longer than 0.5 sec are interrupted)
        System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");

        // Use quartz graphics pipeline on Leopard to maximize compatibility with Tiger
        System.setProperty("apple.awt.graphics.UseQuartz", "true");
    }

    

    public void handleQuit(ApplicationEvent ae) {
        MainTextPad.getInstance().exitAction.actionPerformed(null);
    }

    public void handleAbout(ApplicationEvent ae) {
        ae.setHandled(true);
        MainTextPad.getInstance().aboutAction.actionPerformed(null);
    }
    
    public void handleOpenFile(ApplicationEvent ae) {
        MainTextPad.getInstance().addTab(new File(ae.getFilename()));
    }
    
    public void handleOpenApplication(ApplicationEvent ae) {}
    public void handlePreferences(ApplicationEvent ae) {}
    public void handlePrintFile(ApplicationEvent ae) {}
    public void handleReOpenApplication(ApplicationEvent ae) {}
}