package unrealeditor;

import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


/**
 * A class to handle calls to the command client (DOS or Terminal). Although
 * DOS is the only one currently supported, this could be extended for Macs
 * later via:
 * proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
 * @author William
 */
public class CmdClient {
    public PrintStream out, ps;
    public TextAreaOutputStream os;
    
    
    /*
    public static CmdClient getInstance() {
        return new CmdClient();
    }*/
    
    public CmdClient() {
        // access to JAVA DEFAULT STREAM
        out = System.out;
        os = new TextAreaOutputStream();
        ps = new PrintStream(os);
    };
    
    /**
     * Redirect System.out
     * @param custom true is custom out to text area, false is default System.out
     */
    public void setOutput(boolean custom) {
        if(custom) {
            System.setOut(ps);
        } else {
            System.setOut(out);
        }
    }

    public void runClient(final String[] args) {
        setOutput(true);
        MainTextPad.getInstance().compileAction.setEnabled(false);
        
        //final String[] finalArgs = args;
        SwingWorker sw = new SwingWorker() {
            public Object doInBackground() {
                try{
                    Process proc = null;
                    try {
                        proc = Runtime.getRuntime().exec("cmd");
                    } catch(IOException e) {
                        Utils.showError("Unable to initialize DOS");
                    }
                    if(proc != null) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(proc.getOutputStream())), true);
                        for(int i = 0; i < args.length; i++) {
                            out.println(args[i]);
                        }
                        out.println("exit");
                        try {
                            String line;
                            while((line = in.readLine()) != null) {
                                System.out.println(line);
                            }
                            while((line = er.readLine()) != null) {
                                System.out.println(line);
                            }
                            proc.waitFor();
                            in.close();
                            er.close();
                            out.close();
                            proc.destroy();
                        } catch(Exception e) {
                            Utils.showError("Error executing external operation");
                        }
                    }
                } catch(Exception e) {
                    Utils.showError("Could not load background client thread");
                }
                return null;
            }
            
            protected void done() {
                setOutput(false);
                MainTextPad.getInstance().compileAction.setEnabled(true);
            }
        };
        
        sw.execute();
    }
    
    class TextAreaOutputStream extends OutputStream {
	public void flush() {}
	public void close() {}
        
	public void write(int b) throws IOException {
            updateTextArea(String.valueOf((char)b));
	}
        
        private void updateTextArea(final String text) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MainTextPad.getInstance().compilerArea.append(text);
                }
            });
        }
    }
}