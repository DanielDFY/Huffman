package Constant;

import java.util.HashSet;

public class Constants {
    private Constants () {};

    public final static String HFM_SUFFIX = ".hfm";
    public final static boolean FILE_BIT = true;
    public final static boolean DIR_BIT = false;
    public final static boolean EMPTY_BIT = true;

    public final static HashSet<String> IGNORE_SET = new HashSet<>();

    static {
        IGNORE_SET.add(".DS_Store");
    }

    // GUI
    public final static String TITLE = "Huffman";

    public final static String CSS = "Css/Main.css";
    public final static String LOGO = "Images/logo.png";
    public final static String ICON = "Icon/Icon.png";
    public final static String BACKGROUND = "Images/bg.jpg";
    public final static String HEADER = "Huffman";
    public final static String SIGNATURE = " by Daniel";

    public final static String FILEPATH = "Latest task: ";
    public final static String COMPRESS_RATIO = "Compression Ratio: ";
    public final static String PROCESS_TIME = "Time: ";
    public final static String TASK_COUNT = "Current tasks: ";

    public final static String TIME_UNIT = "ms";
    public final static String PROCESSING = "Processing...";

    public final static String HFM_DESCRIPTION= "Huffman Files(*.hfm)";
    public final static String HFM_EXTENSION = "*.hfm";
}
