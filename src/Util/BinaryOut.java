package Util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class BinaryOut {

    private BufferedOutputStream out;  // the output stream
    private int buffer;                // 8-bit buffer of bits to write out
    private int n;                     // number of bits remaining in buffer

    public BinaryOut(OutputStream os) {
        out = new BufferedOutputStream(os);
    }

    public BinaryOut(String filename) {
        try {
            OutputStream os = new FileOutputStream(filename);
            out = new BufferedOutputStream(os);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BinaryOut(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            out = new BufferedOutputStream(os);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBit(boolean x) {
        // add bit to buffer
        buffer <<= 1;
        if (x) buffer |= 1;

        // if buffer is full (8 bits), write out as a single byte
        n++;
        if (n == 8) clearBuffer();
    }

    private void writeByte(int x) {
        assert x >= 0 && x < 256;

        // optimized if byte-aligned
        if (n == 0) {
            try {
                out.write(x);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // otherwise write one bit at a time
        for (int i = 0; i < 8; i++) {
            boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    // write out any remaining bits in buffer to the binary output stream, padding with 0s
    private void clearBuffer() {
        if (n == 0) return;
        if (n > 0) buffer <<= (8 - n);
        try {
            out.write(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        n = 0;
        buffer = 0;
    }

    public void flush() {
        clearBuffer();
        try {
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        flush();
        try {
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(boolean x) {
        writeBit(x);
    }

    public void write(byte x) {
        writeByte(x & 0xff);
    }

    public void write(int x) {
        writeByte((x >>> 24) & 0xff);
        writeByte((x >>> 16) & 0xff);
        writeByte((x >>>  8) & 0xff);
        writeByte((x >>>  0) & 0xff);
    }

    // Writes the r-bit int to the binary output stream.
    public void write(int x, int r) {
        if (r == 32) {
            write(x);
            return;
        }
        if (r < 1 || r > 32) throw new IllegalArgumentException("Illegal value for r = " + r);
        if (x >= (1 << r))   throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    public void write(char x) {
        if (x < 0 || x >= 256) throw new IllegalArgumentException("Illegal 8-bit char = " + x);
        writeByte(x);
    }

    // Writes the r-bit char to the binary output stream.
    public void write(char x, int r) {
        if (r == 8) {
            write(x);
            return;
        }
        if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value for r = " + r);
        if (x >= (1 << r))   throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    public void write(byte[] bytes) {
        for (byte b : bytes) {
            write(b);
        }
    }

    public static void main(String[] args) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryOut binaryOut = new BinaryOut(byteArrayOutputStream);
        binaryOut.write("test".getBytes());
        binaryOut.flush();

        System.out.println(Arrays.toString(byteArrayOutputStream.toByteArray()));
    }

}
