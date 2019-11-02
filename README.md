# Development Document

### Design and Implementation

This Java compression project is based on Huffman lossless compression algorithm and uses Javafx framework to develop its GUI. 

* **Huffman Kernel**

  Huffman tree is a kind of `Trie` and can be built with the help of minimum `PriorityQueue` and a custom `Node` class.

  ```java
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
  ```

  Each `Node` record its uncoded 8-bit data as a char and its frequency (for comparison during the trie building).

  * To compress the given data, **first count the frequency for each different symbol (256 ASCII character in total)**. Then create an empty minimum priority queue. For each character with non-zero frequency, add a new `Node` into the priority queue.

    ```java
    if (pq.size() == 1) {
    		if (freq['\0'] == 0)
      			pq.add(new Node('\0', 0, null, null));
      	else
        		pq.add(new Node('\1', 0, null, null));
    }
    ```

    If there is only one character with a nonzero frequency, add a parent `Node` to help encoding.

    ```java
    while (pq.size() > 1) {
    		Node left = pq.remove();
    				Node right = pq.remove();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.add(parent);
    }
    ```

    Apply the Huffman encoding algorithm. Finally get the root of the tree.

    

    Then traverse the whole tree to develop a look-up table for encoding.

    ```java
    private static void buildCode(String[] st, Node x, String s) {
    		if (!x.isLeaf()) {
        		buildCode(st, x.left,  s + '0');
            buildCode(st, x.right, s + '1');
        }
        else {
        		st[x.ch] = s;
        }
    }
    
    buildCode(st, root, "");
    ```

    During the traversal, add `'0'` to the string of left child, add `'1'` to the string of right child. If meets a leaf node, add the string to the table array.

    ​	

    Now we can do the compression. First write the Huffman tree into the output.

    ```java
    // binaryOut is an instance of a custom IO util class
    if (x.isLeaf()) {
    		binaryOut.write(true);
    		binaryOut.write(x.ch, 8);
    		return;
    }
    	binaryOut.write(false);
    
    	writeTrie(binaryOut, x.left);
    	writeTrie(binaryOut, x.right);
    ```

    From the root node, if the node is a leaf node, write binary 1 its 8-bit char. Otherwise write false as a non-leaf node, then recursively do the same for its child nodes.

    **Next write the number of bytes of the original uncompressed data (write an int)**. Then go through the data 8-bit by 8-bit and encode with the look-up table array. For every bit write 0 for '0', 1 for '1'.

    ```java
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
    ```

    After this the compression for the data completes.

  * To epand the compressed data, as the write-in order of the compression, first read and rebuild the tree.

    ```java
    boolean isLeaf = in.readBoolean();
    if (isLeaf) {
    		return new Node(in.readChar(), -1, null, null);
    } else {
    		return new Node('\0', -1, readTrie(in), readTrie(in));
    }
    ```

    This time frequency can be ignored because the relation between the nodes can be easily known.

    **Next read the length of the original data (read an int).** Then read bit-by-bit to find a leaf node in the tree and write the char stored in it.

    ```java
    for (int i = 0; i < length; i++) {
    		Node x = root;
    		while (!x.isLeaf()) {
    				boolean bit = binaryIn.readBoolean();
            if (bit) x = x.right;
            else     x = x.left;
        }
        binaryOut.write(x.ch, 8);
    }
    ```

* **Kernel API**

  Aside from the compression of the data, the information of the file and the structure of the directory should be put into the pakage file.

  * For compression, first find out whether the source is a file or direcoty.

    If it's a file, write its information as the header and compress its content.

    ```java
    // binaryIn is also an instance of a custom IO util class
    // class 'Constants' stores constants for convenience
    
    String fileName = file.getName();
    boolean isEmptyFile = (file.length() == 0);
    
    // write file info
    writeFileHead(fileName, isEmptyFile, binaryOut);
    
    // only compress non-empty file
    if (!isEmptyFile) {
    		BinaryIn binaryIn = new BinaryIn(file);
    		Huffman.compress(binaryIn, binaryOut);
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
    ```

    If it's a directory, write the information of its name and the total size as header and recursively compress its child files. This process records the whole directory structure at the same time.

    ```java
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
    ```

  * For decompression, **first read a bit to find out whether its a single file or a directory**. Then expand and create the file or directory structure based on the compression process.

    ```java
    // expand single file
    
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
    ```

* **GUI**

  This project uses Javafx framework to construct the GUI. The detailed instructions can be find in [UserManual](./UserManual.md). When user create a task, a new thread will be created and the number of runnning tasks and the information of the latest task will be displayed on the panel.



### Performance Test

Environment: Macbook pro 13-inch

| CPU                   | Memory              |
| --------------------- | ------------------- |
| 2.3 GHz Intel Core i5 | 16G 2133 Mhz LPDDR3 |

* **Test 1 - single file**

  | File    | Compress time | Decompress time | Compression Rate |
  | ------- | ------------- | --------------- | ---------------- |
  | 1.txt   | 114ms         | 68ms            | 55.97%           |
  | 2.pdb   | 2ms           | 1ms             | 44.39%           |
  | 3.evy   | 2ms           | 1ms             | 101.36%          |
  | 4.gz    | 1ms           | <1ms            | 179.72%          |
  | 5.hpgl  | 4ms           | 3ms             | 47.48%           |
  | 6.ma    | 11ms          | 8ms             | 58.03%           |
  | 7.pdf   | 10ms          | 5ms             | 80.53%           |
  | 8.sgml  | 1ms           | 1ms             | 65.86%           |
  | 9.htm   | 34ms          | 19ms            | 69.29%           |
  | 10.cgm  | 2ms           | 1ms             | 91.93%           |
  | 11.g3f  | 1ms           | 1ms             | 87.91%           |
  | 12.gif  | 1ms           | 1ms             | 104.25%          |
  | 13.jpg  | 3ms           | 1ms             | 101.44%          |
  | 14.png  | 1ms           | <1ms            | 99.22%           |
  | 15.ps   | 5ms           | 4ms             | 59.46%           |
  | 16.svg  | 1ms           | <1ms            | 68.21%           |
  | 17.tif  | 1ms           | 1ms             | 98.72%           |
  | 18.xbm  | <1ms          | <1ms            | 35.70%           |
  | 19.msh  | 10ms          | 7ms             | 64.23%           |
  | 20.mov  | 147ms         | 81ms            | 94.74%           |
  | 21.mpeg | 13ms          | 5ms             | 98.48%           |
  | 22.igs  | 58ms          | 32ms            | 40.97%           |
  | 23.v5d  | 47ms          | 20ms            | 93.55%           |
  | 24.wrl  | 2ms           | 1ms             | 61.89%           |
  | 25.aiff | 2ms           | 1ms             | 65.22%           |
  | 26.au   | 3ms           | 2ms             | 80.80%           |
  | 27.mp3  | 142ms         | 53ms            | 99.17%           |
  | 28.ra   | 4ms           | 3ms             | 100.09%          |
  | 29.wav  | 1ms           | 1ms             | 103.53%          |
  | 30.ram  | <1ms          | 1ms             | 180.00%          |
  | 31.aiff | 5ms           | 3ms             | 89.31%           |
  | 32.aiff | 17ms          | 7ms             | 88.90%           |
  | 33.aifc | 33ms          | 17ms            | 87.38%           |
  | 34.tsv  | 3ms           | 1ms             | 49.91%           |
  | 35.avi  | 3ms           | 2ms             | 88.07%           |

* **Test 2 - folder**

  | Directory | Compress time | Decompress time | Compression Rate |
  | --------- | ------------- | --------------- | ---------------- |
  | 1         | 193ms         | 125ms           | 60.94%           |
  | 2         | 756ms         | 385ms           | 63.17%           |
  | 3         | 200ms         | 112ms           | 64.32%           |

* **Test 3 - empty file and folder**

  | Source       | Compress time | Decompress time | Compression Rate |
  | ------------ | ------------- | --------------- | ---------------- |
  | Empty file   | <1ms          | <1ms            | $\infty^+$       |
  | Empty folder | <1ms          | <1ms            | $\infty^+$       |

* **Test 4 - large file**

  | File  | Compress time | Decompress time | Compression Rate |
  | ----- | ------------- | --------------- | ---------------- |
  | 1.jpg | 1937ms        | 635ms           | 99.72%           |
  | 2.csv | 7086ms        | 3783ms          | 64.15%           |
  | 3.csv | 5950ms        | 3442ms          | 63.70%           |



### Problem Encountered 

* The main problem is how to efficiently write different types of data (such as int, char, boolean) into a file stream. To solve this, util classes `BinaryIn` and `BinaryOut` are created. In both class, there is an one-character buffer, with these API data can be manipulated on bit level instead of byte level.

* Another rough patch is that for Chinese characters, each of them is not 8-bit long. Therefore, before passing any Chinese string to the Huffman kernel program,  it needs to be translated into `UTF-8`.

  ```java
  String s = new String(s.getBytes(), StandardCharsets.UTF_8);
  ```

  

### Optimization

* Create custom util classes `BinaryIn` and `BinaryOut` with **8-bit buffer** and buffered stream classes (`BufferedInputStream` and `BufferedOutputStream`) to efficiently manipulate bit-level IO reading and writing.
* Use multithreading to process multiple compression and expansion tasks at the same time.



### Comparison 

Compression/Decompression target: A folder containing all of the 4 test cases

| Test case               | Compress time | Decompress time | Compression Rate |
| ----------------------- | ------------- | --------------- | ---------------- |
| This Program            | 14871ms       | 8096ms          | 66.88%           |
| MacOS Built-in Archiver | ≈10670ms      | ≈4100ms         | 27.50%           |
| Keka                    | ≈17120ms      | ≈3700ms         | 25.59%           |
