import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application{

    private TabPane mainPane;
    private JudgePane judging;
    private StandingsPane standings;
    private HashMap<String, ArrayList<Lifter>> lifterGroups;
    public static final int WINSIZE_X = 1000, WINSIZE_Y = 600;
    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage primaryStage){
        // Initialize Lifter Groups
        lifterGroups = new HashMap<String, ArrayList<Lifter>>();
        // Initialize Tabs
        mainPane = new TabPane();
        judging = new JudgePane(lifterGroups);
        standings = new StandingsPane(lifterGroups);
        
        // create judging tab
        Tab tab1 = new Tab();
        tab1.setText("Judging");
        tab1.setContent(judging);

        // create Standings tab
        Tab tab2 = new Tab();
        tab2.setText("Standings");
        tab2.setContent(standings);

        // place tabs onto TabPane
        mainPane.getTabs().addAll(tab1, tab2);

        // Initialize Scene holding TabPane, add to stage
        Scene scene = new Scene(mainPane, WINSIZE_X, WINSIZE_Y);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Powerlifting App");
        primaryStage.show();
    }
}