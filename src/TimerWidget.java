/** Timer Widget 
 *  Fully scalable timer widget to be embedded into UI
 *  Author: Ben Jordan 
 */

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.util.Duration;

public class TimerWidget extends StackPane{

    private GridPane gridPane;
    private Label clock;
    private int originalTime;
    private int curTime;
    private boolean isPaused;
    private Timeline tl;
    private Button pausePlay;
    private Button reset;
    private Button increment;
    private Button decrement;

    /** Construct a TimerWidget Object which starts with the number of seconds passed in
     * Precondition: seconds > 0
    */
    public TimerWidget(int seconds){
        // this.setStyle("-fx-background-color: #2a2e2b");
        this.setPadding(new Insets(10, 10, 10, 10));
        // create refresh timeline
        this.tl = new Timeline(new KeyFrame(Duration.millis(1000), new tickHandler()));
        tl.setCycleCount(Timeline.INDEFINITE);

        // CLOCKFACE
        this.originalTime = seconds;
        this.clock = new Label();

            // alignment
        GridPane.setHalignment(clock, HPos.CENTER);
        GridPane.setValignment(clock, VPos.CENTER);
        initialize();

        // PAUSE/PLAY button
        this.pausePlay = createCenteredButton(new pausePlayHandler(), "⏵⏸");

        // RESET button
        this.reset = createCenteredButton(new resetHandler(), "↺");

        // INCREMENT button
        increment = createCenteredButton(new incrementHandler(), "▲");

        // DECREMENT button
        decrement = createCenteredButton(new incrementHandler(), "▼");

        // GRIDPANE (houses controls)
        gridPane = new GridPane();

            // style and functionality
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setStyle("-fx-background-color:gray;");
        gridPane.widthProperty().addListener(new resizeListener());

            // position elements on grid
        gridPane.add(clock, 0, 0, 2, 2);
        gridPane.add(pausePlay, 0, 2);
        gridPane.add(reset, 1, 2);
        gridPane.add(increment, 2, 0);
        gridPane.add(decrement, 2, 1);

            // set column/row constraints
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        RowConstraints r1 = new RowConstraints();

        c1.setPercentWidth(30);
        c2.setPercentWidth(40);
        r1.setPercentHeight(33);

        gridPane.getColumnConstraints().addAll(c1, c1, c2);
        gridPane.getRowConstraints().addAll(r1, r1, r1);

            // add gridPane to StackPane (centered)
        this.getChildren().add(gridPane);

            // add bindings to maintain grid proportions while sliding within stack pane
        gridPane.maxHeightProperty().bind(Bindings.divide(this.widthProperty(), 2));
        gridPane.maxWidthProperty().bind(Bindings.multiply(this.heightProperty(), 2));
    }

    /** reset the clock to the current original value */
    private void initialize(){
        pause();
        clock.setText(formatTime(originalTime));
        curTime = originalTime;
    }

    /** Start the timer */
    private void resume(){
        isPaused = false;
        tl.play();
    }

    /** Pause the timer */
    private void pause(){
        isPaused = true;
        tl.pause();
    }

    /** Update the originalTime value and reset the clock */
    private void setTimer(int newTime){
        originalTime = newTime;
        initialize();
    }

    // Utility methods 

    /**update the clockface given a number of seconds*/
    private static String formatTime(int timeSecs){
        int minutes = timeSecs/60;
        // add padding to seconds
        String seconds = String.format("%02d", timeSecs%60);
        return minutes + ":" + seconds;
    }

    /** Clean up repetitive button creation for control elements
     * returns an expanding button that will center within a grid cell and is bound to a desired handler
     */
    private static Button createCenteredButton(EventHandler<ActionEvent> handler, String label){
        Button button = new Button(label);
        button.setOnAction(handler);
        button.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        return button;
    }

    // Event Handlers and listeners

    /** Decrement the timer on every cycle of the timeline */
    private class tickHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            curTime --;
            clock.setText(formatTime(curTime));
            if(curTime == 0){
            tl.pause();
            }
        }
    }

    /** Toggle pause/play on the timer */
    private class pausePlayHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(isPaused){
                resume();
            }else{
                pause();
            }
        }
    }

    /** reset the clock */
    private class resetHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            initialize();
        }
    }

    /** Determine whether to increment or decrement the timer by 15 seconds */
    private class incrementHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(event.getSource() == increment){
                setTimer(originalTime+15);
            }else{
                setTimer(originalTime-15);
            }
        }
    }

    /** Scale the label fonts to maintain proportionality with the size of the window */
    private class resizeListener implements ChangeListener<Number>{
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Font resized = new Font(newValue.intValue()/22);
            clock.fontProperty().set(new Font(newValue.intValue()/4));
            pausePlay.fontProperty().set(resized);
            reset.fontProperty().set(resized);
            increment.fontProperty().set(resized);
            decrement.fontProperty().set(resized);
        }
    }
}