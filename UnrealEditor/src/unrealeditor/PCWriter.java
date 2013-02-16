package unrealeditor;

import java.io.*;

public class PCWriter extends Writer {
    protected Writer out;
    private int lastWritten;

    public PCWriter(Writer out) {
        super(out);
        this.out = out;
    }

    @Override
    public void write(int c) throws IOException {
        if(c == '\n' && lastWritten != '\r')
            out.write('\r');
        out.write(c);
        lastWritten = c;                
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int i = off; i<off+len; i++) {
            int c = cbuf[i];
            if(c == '\n' && lastWritten != '\r')
                sb.append('\r');
            sb.append((char)c);
            lastWritten = c;
        }
        out.write(sb.toString());
    }

    @Override public void flush() throws IOException {out.flush();}
    @Override public void close() throws IOException {out.close();}
}