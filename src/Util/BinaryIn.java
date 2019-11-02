package Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public final class BinaryIn {
    private static final int EOF = -1;   // end of file

    private BufferedInputStream in;      // the input stream
    private int buffer;                  // one character buffer
    private int n;                       // number of bits left in buffer

    public BinaryIn(InputStream is) {
        in = new BufferedInputStream(is);
        fillBuffer();
    }

    public BinaryIn(String fileName) {

        try {
            // first try to read file from local file system
            File file = new File(fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                in = new BufferedInputStream(fis);
                fillBuffer();
            }
        }
        catch (IOException ioe) {
            System.err.println("Could not open " + fileName);
        }
    }

    public BinaryIn(File file) {

        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                in = new BufferedInputStream(fis);
                fillBuffer();
            }
        }
        catch (IOException ioe) {
            System.err.println("Could not open " + file.getName());
        }
    }

    private void fillBuffer() {
        try {
            buffer = in.read();
            n = 8;
        }
        catch (IOException e) {
            System.err.println("EOF");
            buffer = EOF;
            n = -1;
        }
    }

    public boolean exists()  {
        return in != null;
    }

    public boolean isEmpty() {
        return buffer == EOF;
    }

    public boolean readBoolean() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
        n--;
        boolean bit = ((buffer >> n) & 1) == 1;
        if (n == 0) fillBuffer();
        return bit;
    }

    public char readChar() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

        // special case when aligned byte
        if (n == 8) {
            int x = buffer;
            fillBuffer();
            return (char) (x & 0xff);
        }

        // combine last N bits of current buffer with first 8-N bits of new buffer
        int x = buffer;
        x <<= (8 - n);
        int oldN = n;
        fillBuffer();
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
        n = oldN;
        x |= (buffer >>> n);
        return (char) (x & 0xff);
        // the above code doesn't quite work for the last character if N = 8
        // because buffer will be -1
    }

    public String readString() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

        StringBuilder sb = new StringBuilder();
        while (!isEmpty()) {
            char c = readChar();
            sb.append(c);
        }
        return sb.toString();
    }

    public int readInt() {
        int x = 0;
        for (int i = 0; i < 4; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

    public byte readByte() {
        char c = readChar();
        return (byte) (c & 0xff);
    }

    public static void main(String[] args) {
        byte[] bytes = {'a', 'b', 'c'};
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BinaryIn  in  = new BinaryIn(byteArrayInputStream);

        while (!in.isEmpty()) {
            char c = in.readChar();
            System.out.print(c);
        }
    }
}
