package GUI;

import Constant.Constants;
import Kernel.API;
import Util.BinaryOut;

import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;

public class Main extends Application {

    private StackPane stackPane;
    private static int task = 0;
    private Label taskLabel = new Label();
    private Label pathLabel = new Label();
    private Label ratioLabel = new Label();
    private Label timeLabel = new Label();

    @Override
    public void start(Stage primaryStage) {
        try {
            init(primaryStage);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            errorAlert(e.getMessage());
        }
}

    private void init(Stage primaryStage) {
        // load the CSS file
        Application.setUserAgentStylesheet(Main.class.getResource(Constants.CSS).toString());

        Label[] labels = {taskLabel, pathLabel, ratioLabel, timeLabel};
        for (Label label : labels) {
            label.getStyleClass().add("myLabel");
        }

        stackPane = new StackPane();
        BorderPane mainPane = getMainPane(primaryStage);
        primaryStage.setTitle(Constants.TITLE);
        primaryStage.getIcons().add(new Image(Main.class.getResource(Constants.ICON).toString()));

        // load background image
        ImageView bgImg = new ImageView(Main.class.getResource(Constants.BACKGROUND).toString());
        bgImg.setFitWidth(900);
        bgImg.setPreserveRatio(true);

        stackPane.getChildren().addAll(bgImg, mainPane);

        Scene scene = new Scene(stackPane, 800, 440);
        primaryStage.setScene(scene);

        // moving background
        Path path = new Path();
        path.getElements().add(new MoveTo(440, 250));
        path.getElements().add(new LineTo(460, 250));
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(10000));
        pathTransition.setPath(path);
        pathTransition.setNode(bgImg);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(Timeline.INDEFINITE);
        pathTransition.setAutoReverse(true);
        pathTransition.play();
    }

    private BorderPane getMainPane(Stage stage) {
        BorderPane mainPane = new BorderPane();

        // divide the pane into 3 parts
        HBox topPane = getTopPane();
        VBox middlePane = getMiddlePane();
        HBox bottomPane = getBottomPane(stage);

        mainPane.setTop(topPane);
        mainPane.setCenter(middlePane);
        mainPane.setBottom(bottomPane);

        return mainPane;
    }

    private HBox getTopPane() {
        HBox topPane = new HBox();
        topPane.setMinWidth(500);
        topPane.setMinHeight(30);
        topPane.setAlignment(Pos.TOP_CENTER);
        topPane.setPadding(new Insets(40, 0, 10, -40));

        // load logo image
        ImageView logoImg = new ImageView(new Image(Main.class.getResource(Constants.LOGO).toString()));
        logoImg.setFitWidth(120);
        logoImg.setFitHeight(120);

        // add header text
        VBox textPane = new VBox();
        Label header = new Label(Constants.HEADER);
        header.setPadding(new Insets(30, 0, 10, 0));
        header.getStyleClass().add("header");

        // add signature
        Label signature = new Label(Constants.SIGNATURE);
        signature.setPadding(new Insets(0, 0, 0, 20));
        signature.getStyleClass().add("signature");

        Label[] labels = {header, signature};
        for (Label label : labels) {
            label.getStyleClass().add("myLabel");
        }

        // combine the panes
        textPane.getChildren().addAll(header, signature);
        topPane.getChildren().addAll(logoImg, textPane);
        return topPane;
    }

    private VBox getMiddlePane() {
        VBox middlePane = new VBox();
        middlePane.setMinWidth(800);
        middlePane.setPadding(new Insets(0, 0, 20, 80));
        middlePane.setAlignment(Pos.CENTER_LEFT);
        taskLabel.setText(Constants.TASK_COUNT + task);
        middlePane.getChildren().addAll(taskLabel, pathLabel, timeLabel, ratioLabel);
        return middlePane;
    }

    private HBox getBottomPane(Stage stage) {
        HBox bottomPane = new HBox();
        bottomPane.setMaxSize(820, 80);
        bottomPane.setMinSize(820, 80);
        bottomPane.setAlignment(Pos.BOTTOM_CENTER);
        bottomPane.setPadding(new Insets(5));

        Label compressLabel = new Label("Compress");
        Label comFileLabel = new Label("Single File");
        Label comDirLabel = new Label("Directory");
        Label expandLabel = new Label("Expand");
        Label exitLabel = new Label("Exit");

        Label[] labels = {compressLabel, comFileLabel, comDirLabel, expandLabel, exitLabel};
        for (Label label : labels) {
            label.getStyleClass().add("myLabel");
            label.getStyleClass().add("button");
        }

        compressLabel.setMinSize(180, 70);
        expandLabel.setMinSize(180, 70);
        exitLabel.setMinSize(180, 70);

        // select single file or directory to compress
        HBox branchPane = new HBox();
        branchPane.setAlignment(Pos.BOTTOM_CENTER);
        branchPane.setMaxSize(200, 290);
        branchPane.setMinSize(200, 290);
        comFileLabel.setMaxSize(180, 50);
        comFileLabel.setMinSize(180, 50);
        comDirLabel.setMaxSize(180, 50);
        comDirLabel.setMinSize(180, 50);
        branchPane.getChildren().addAll(comFileLabel, comDirLabel);

        // compress click event
        compressLabel.setOnMouseClicked(event -> {
            if (stackPane.getChildren().contains(branchPane)) {
                stackPane.getChildren().remove(branchPane);
            } else
                stackPane.getChildren().add(branchPane);
        });

        // compress single file
        comFileLabel.setOnMouseClicked(event -> {
            stackPane.getChildren().remove(branchPane);
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                compress(file);
            }
        });

        // compress directory
        comDirLabel.setOnMouseClicked(event -> {
            stackPane.getChildren().remove(branchPane);
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(stage);
            if (dir != null) {
                compress(dir);
            }
        });

        // expand .hfm file
        expandLabel.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Constants.HFM_DESCRIPTION, Constants.HFM_EXTENSION));
            File src = fileChooser.showOpenDialog(stage);

            if (src != null) {
                expand(src);
            }
        });

        // exit program
        exitLabel.setOnMouseClicked(event -> {
            stage.close();
            System.exit(0);
        });

        bottomPane.getChildren().addAll(compressLabel, expandLabel, exitLabel);
        return bottomPane;
    }

    private void compress(File src) {
        timeLabel.setText(Constants.PROCESS_TIME);
        ratioLabel.setText(Constants.COMPRESS_RATIO);
        pathLabel.setText(Constants.FILEPATH + src.getAbsolutePath());
        timeLabel.setText(Constants.PROCESS_TIME + Constants.PROCESSING);

        // create new compress thread
        new Thread(() -> {
            Platform.runLater(() -> taskLabel.setText(Constants.TASK_COUNT + (++task)));
            try {
                File output = new File(src.getAbsolutePath() + Constants.HFM_SUFFIX);
                BinaryOut binaryOut = new BinaryOut(output);

                long startTime = System.currentTimeMillis();
                API.compress(src, binaryOut);
                binaryOut.close();
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                long newSize = output.length();
                Double ratio = ((double) newSize / getDirLength(src)) * 100;
                DecimalFormat df = new DecimalFormat( "0.00");
                String ratioStr = df.format(ratio) + "%";

                Platform.runLater(() -> updateState(--task, src.getAbsolutePath(), time, Constants.COMPRESS_RATIO + ratioStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void expand(File src) {
        timeLabel.setText(Constants.PROCESS_TIME);
        ratioLabel.setText("");
        pathLabel.setText(Constants.FILEPATH + src.getAbsolutePath());
        timeLabel.setText(Constants.PROCESS_TIME + Constants.PROCESSING);

        // create new expand thread
        new Thread(() -> {
            Platform.runLater(() -> taskLabel.setText(Constants.TASK_COUNT + (++task)));
            try {
                long startTime = System.currentTimeMillis();
                API.expand(src);
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                Platform.runLater(() -> updateState(--task, src.getAbsolutePath(), time, ""));
            } catch (Exception e) {
                errorAlert(e.getMessage());
            }
        }).start();
    }

    private void updateState(int task, String path, long timeConsuming, String ratio) {
        taskLabel.setText(Constants.TASK_COUNT + task);
        pathLabel.setText(Constants.FILEPATH + path);
        timeLabel.setText(Constants.PROCESS_TIME + timeConsuming + Constants.TIME_UNIT);
        ratioLabel.setText(ratio);
    }

    private void errorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // get the total length of all the files in given directory
    private long getDirLength(File file) {
        if(file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            long length = 0;
            File[] list = file.listFiles();
            if (null == list)
                return 0;
            for (File content : list) {
                length += getDirLength(content);
            }
            return length;
        } else {
            throw new RuntimeException("Unknown file type");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
