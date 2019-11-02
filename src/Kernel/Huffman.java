package Kernel;

import Util.BinaryIn;
import Util.BinaryOut;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.PriorityQueue;

public class Huffman {
    // alphabet size of extended ASCII
    private static final int R = 256;

    // Do not instantiate
    private Huffman() { }

    // Kernel.Huffman trie node
    private static class Node implements Comparable<Node> {
        private final char ch;
        private final int freq;
        private final Node left;
        private final Node right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch    = ch;
            this.freq  = freq;
            this.left  = left;
            this.right = right;
        }

        private boolean isLeaf() {
            assert ((left == null) && (right == null)) || ((left != null) && (right != null));
            return (left == null) && (right == null);
        }

        // compare, based on frequency
        @Override
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }

    public static void compress(BinaryIn binaryIn, BinaryOut binaryOut) {
        String data = binaryIn.readString();
        char[] input = data.toCharArray();

        // tabulate frequency counts
        int[] freq = new int[R];
        for (char c : input) ++freq[c];

        // build Kernel.Huffman trie
        Node root = buildTrie(freq);

        // build code table
        String[] st = new String[R];
        buildCode(st, root, "");

        // write trie for decoder
        writeTrie(binaryOut, root);

        // write number of bytes of the original uncompressed data
        binaryOut.write(input.length);

        // use Kernel.Huffman code to encode input
        for (char c : input) {
            String code = st[c];
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') {
                    binaryOut.write(false);
                } else if (code.charAt(j) == '1') {
                    binaryOut.write(true);
                } else throw new IllegalStateException("Illegal state");
            }
        }
    }

    private static Node buildTrie(int[] freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (char i = 0; i < R; ++i) {
            if (freq[i] > 0)
                pq.add(new Node(i, freq[i], null, null));
        }

        // in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0)
                pq.add(new Node('\0', 0, null, null));
            else
                pq.add(new Node('\1', 0, null, null));
        }

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left = pq.remove();
            Node right = pq.remove();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.add(parent);
        }

        return pq.remove();
    }

    // make a look-up table from symbols and their encodings
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left,  s + '0');
            buildCode(st, x.right, s + '1');
        }
        else {
            st[x.ch] = s;
        }
    }

    private static void writeTrie(BinaryOut binaryOut, Node x) {
        if (x.isLeaf()) {
            binaryOut.write(true);
            binaryOut.write(x.ch, 8);
            return;
        }
        binaryOut.write(false);

        writeTrie(binaryOut, x.left);
        writeTrie(binaryOut, x.right);
    }


    public static void expand(BinaryIn binaryIn, BinaryOut binaryOut) {
        // read in Kernel.Huffman trie from input stream
        Node root = readTrie(binaryIn);

        // number of bytes to write
        int length = binaryIn.readInt();

        // expand using the Kernel.Huffman trie
        for (int i = 0; i < length; i++) {
            Node x = root;
            while (!x.isLeaf()) {
                boolean bit = binaryIn.readBoolean();
                if (bit) x = x.right;
                else     x = x.left;
            }
            binaryOut.write(x.ch, 8);
        }
    }


    private static Node readTrie(BinaryIn in) {
        boolean isLeaf = in.readBoolean();
        if (isLeaf) {
            return new Node(in.readChar(), -1, null, null);
        } else {
            return new Node('\0', -1, readTrie(in), readTrie(in));
        }
    }

    public static void main(String[] args) {
        String data = "hello, world!";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryIn in = new BinaryIn(byteArrayInputStream);
        BinaryOut out = new BinaryOut(byteArrayOutputStream);
        compress(in, out);
        out.close();

        byte[] compressed = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();

        BinaryIn in2 = new BinaryIn(byteArrayInputStream2);
        BinaryOut out2 = new BinaryOut(byteArrayOutputStream2);
        expand(in2, out2);
        out2.close();

        System.out.println(byteArrayOutputStream2.toString());
    }
}
