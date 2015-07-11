package com.cherokeelessons.gui;


import java.io.PrintStream;
/**
 * {@link http://stackoverflow.com/questions/1356706/copy-stdout-to-file-without-stopping-it-showing-onscreen}
 * @author michael
 *
 */
public class TeeStream extends PrintStream {
    PrintStream out;
    public TeeStream(PrintStream out1, PrintStream out2) {
        super(out1);
        this.out = out2;
    }
    public void write(byte buf[], int off, int len) {
        try {
            super.write(buf, off, len);
            out.write(buf, off, len);
        } catch (Exception e) {
        }
    }
    public void flush() {
        super.flush();
        out.flush();
    }
}