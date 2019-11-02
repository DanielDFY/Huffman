package Kernel;

import Util.BinaryIn;
import Util.BinaryOut;
import Constant.Constants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class API {
    public static void compress(File src, BinaryOut binaryOut) {
        if (null == src)
            throw new IllegalArgumentException("Null source file for compress");

        if (src.isFile()) {
            // compress single file
            compressFile(src, binaryOut);
        } else if (src.isDirectory()) {
            // compress directory
            compressDir(src, binaryOut);
        } else
            throw new RuntimeException("Unknown kind of source");
    }

    private static void compressFile(File file, BinaryOut binaryOut) {
        assert file.isFile();

        String fileName = file.getName();
        boolean isEmptyFile = (file.length() == 0);

        // write file info
        writeFileHead(fileName, isEmptyFile, binaryOut);

        // only compress non-empty file
        if (!isEmptyFile) {
            BinaryIn binaryIn = new BinaryIn(file);
            Huffman.compress(binaryIn, binaryOut);
        }
    }

    private static void writeFileHead(String fileName, boolean isEmptyFile, BinaryOut binaryOut) {
        // to signal file header
        binaryOut.write(Constants.FILE_BIT);

        // write in file name info
        byte[] bytes = fileName.getBytes();
        binaryOut.write(bytes.length);
        binaryOut.write(bytes);

        // to signal whether its empty
        binaryOut.write(isEmptyFile);
    }

    private static void compressDir(File dir, BinaryOut binaryOut) {
        assert dir.isDirectory();

        File[] files = dir.listFiles();

        if (null == files)
            throw new RuntimeException("Null file list of dir");

        ArrayList<File> list = new ArrayList<>();
        for (File file : files) {
            // ignore unwanted files
            if (Constants.IGNORE_SET.contains(file.getName()))
                continue;
            list.add(file);
        }
        int length = list.size();

        writeDirHead(dir.getName(), length, binaryOut);

        // compress each content respectively
        for (File file : list) {
            compress(file, binaryOut);
        }
    }

    private static void writeDirHead(String dirName, int length, BinaryOut binaryOut) {
        // to signal the directory header
        binaryOut.write(Constants.DIR_BIT);

        // write in directory name info
        byte[] bytes = dirName.getBytes();
        binaryOut.write(bytes.length);
        binaryOut.write(bytes);

        if (length == 0) {
            binaryOut.write(Constants.EMPTY_BIT);      // true for empty directory
        } else {
            binaryOut.write(!Constants.EMPTY_BIT);     // false for non-empty directory
            binaryOut.write(length);
        }
    }

    public static void expand(File file) {
        if (null == file)
            throw new IllegalArgumentException("Null source file for expand");

        // only support .hfm suffix
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (!suffix.equals(Constants.HFM_SUFFIX))
            throw new RuntimeException("Unsupported file suffix");

        File parent = new File(file.getAbsoluteFile().getParent());
        expand(parent, new BinaryIn(file));
    }

    private static void expand(File parent, BinaryIn binaryIn) {
        if (binaryIn.readBoolean() == Constants.FILE_BIT) {
            // expand single file
            expandFile(parent, binaryIn);
        } else {
            // expand directory

            // get name info
            int nameLength = binaryIn.readInt();
            byte[] bytes = new byte[nameLength];
            for (int i = 0; i < nameLength; ++i) {
                bytes[i] = binaryIn.readByte();
            }

            // deal with chinese
            String dirName = new String(bytes, StandardCharsets.UTF_8);

            File dir = new File(parent, dirName);
            if (!dir.mkdirs()) {
                throw new RuntimeException("Failed to make dir: " + dirName);
            }

            if (binaryIn.readBoolean() != Constants.EMPTY_BIT) {
                // expand each content respectively
                int length = binaryIn.readInt();
                for (int i = 0; i < length; ++i) {
                    expand(dir, binaryIn);
                }
            }
        }
    }

    private static void expandFile(File parent, BinaryIn binaryIn) {
        // get name info
        int nameLength = binaryIn.readInt();
        byte[] bytes = new byte[nameLength];
        for (int i = 0; i < nameLength; ++i) {
            bytes[i] = binaryIn.readByte();
        }

        // deal with chinese file name
        String fileName = new String(bytes, StandardCharsets.UTF_8);
        BinaryOut binaryOut = new BinaryOut(new File(parent, fileName));

        if (binaryIn.readBoolean() != Constants.EMPTY_BIT) {
            Huffman.expand(binaryIn, binaryOut);
        }

        binaryOut.close();
    }

    public static void main(String[] args) {
        long startTime, endTime;
        File src;
        BinaryOut binaryOut;

        src = new File("test/TestCases");
        binaryOut = new BinaryOut("test.hfm");

        startTime =  System.currentTimeMillis();
        compress(src, binaryOut);
        endTime =  System.currentTimeMillis();
        System.out.println ("compress time: " + (endTime-startTime) + "ms");

        binaryOut.close();

        src = new File("test.hfm");

        startTime =  System.currentTimeMillis();
        expand(src);
        endTime =  System.currentTimeMillis();
        System.out.println ("expand time: " + (endTime-startTime) + "ms");
    }
}
