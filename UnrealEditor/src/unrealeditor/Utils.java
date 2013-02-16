package unrealeditor;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.fife.io.UnicodeReader;

/**
 * Holds common non-class specific operations
 */
public class Utils {

    public static byte[] readBytes(File f) throws IOException {
        byte[] buf = new byte[(int) f.length()];
        FileInputStream in = new FileInputStream(f);

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while(offset < buf.length && (numRead = in.read(buf, offset, buf.length - offset)) >= 0)
            offset += numRead;
        in.close();

        // Ensure all the bytes have been read in
        if(offset < buf.length)
            throw new IOException("Could not completely read file " + f.getName());
        return buf;
    }
    
    public static String getFileText(File file) {
        String text = "";
        try {
            byte[] raw = readBytes(file);
            StringCodec sc = new StringCodec();
            String tmp = sc.decode(raw);
            
            String encoding = sc.getDetectedEncoding();
            StringBuilder buf = new StringBuilder();
            BufferedReader reader = new BufferedReader(new UnicodeReader(new ByteArrayInputStream(raw), encoding));
            String line;
            while((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append('\n');
            }
            text = buf.toString();
        } catch(IOException ex) {
            return "";
        }
        return text;
    }
    
    /**
     * Gets a file's text. In this case the file must be in the project's
     * directory structure to be loaded via string.
     */
    public static String getFileText(String name) {
        URL url = Utils.class.getResource(name);
        return getFileText(new File(url.getPath()));
    }
    
    /**
     * Gets an image from the project's directory structure.
     */
    public static ImageIcon loadImage(String name) {
        ImageIcon image = null;
        try {
            URL url = Utils.class.getResource(name);
            if(url != null) {
                java.awt.Image img = Toolkit.getDefaultToolkit().createImage(url);
                if(img != null)
                    image = new ImageIcon(img);
            }
        } catch(Throwable ex) {
            System.out.println("ERROR: loading image " + name + " failed");
        }
        return image;
    }
    
    public static int booleanArrayToInt(boolean[] a) {
        int result = 0;
        for(int i = 0; i < a.length; i++)
            result |= ((a[i] ? 1 : 0) << i);
        return result;
    }
    
    public static boolean[] intToBooleanArray(int a, int length) {
        boolean result[] = new boolean[length];
        for(int i = 0; i < length; i++)
            result[i] = ((a & (1 << i)) >> i) == 1;
        return result;
    }
    
    public static void showError(String error) {
        JOptionPane.showMessageDialog(MainTextPad.getInstance(),
                error,"ERROR!",JOptionPane.ERROR_MESSAGE);
    }
}