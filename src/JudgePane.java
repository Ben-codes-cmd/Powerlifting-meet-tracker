import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;


public class JudgePane extends GridPane{
    /** Scroll pane in which to hold lifter cards */
    private ScrollPane scroll;

    /** Container holding all controls with which to navigate the lifter groups as well as select, create and remove lifters  */
    private VBox lifterSelection;

    /** Contains the current list of lifter cards being displayed */
    private VBox scrollChild;
    
    /** Leftmost menu including an individual's lifts, a timer and a plate diagram to assist with judging */
    private GridPane trackingMenu;

    /** Timer utility defined separately */
    private TimerWidget timerWidget;

    /** Bar loading widget defined separately */
    private PlateDiagram plateDiagram;

    /** Used to toggle the add lifter menu */
    private Button addButton;

    /** Used to toggle the remove lifter menu */
    private Button removeButton;

    /** Used to finalize the edit of a lift, pulling criteria from surrounding fields */
    private Button editSubmissionButton;

    /** Used to automatically load an attempt's weight into the plate diagram */
    private Button loadDiagramButton;

    /** Used to accept a weight to update an attempt */
    private TextField attemptInput;

    /** Holds each of the possible weight classes */
    private ComboBox<String> weightClassCombo;

    /** Holds each age division*/
    private ComboBox<String> divisionCombo;

    /** Used to update either squat, bench or deadlift */
    private ComboBox<String> liftCombo;

    /** Used to select which of the three attempts to update */
    private ComboBox<Integer> attemptCombo;

    /** Used to detail the status of a lift i.e good, failed, or projected */
    private ComboBox<String> statusCombo;

    /** Used to switch between lbs and kgs for input values */
    private ComboBox<Metric> metricCombo;

    /** Holds reference to the application's lists of lifters */
    private HashMap<String, ArrayList<Lifter>> lifterGroups;

    /** Holds the lifter object which is currently being edited */
    private Lifter selectedLifter;

    /** Label displaying each of the selected lifter's three squat attempts*/
    private Label squatLabel;

    /** Label displaying each of the selected lifter's three bench press attempts*/
    private Label benchLabel;

    /** Label displaying each of the selected lifter's three deadlift attempts*/
    private Label deadliftLabel;

    /** Label displaying the selected lifter's current total */
    private Label totalLabel;

    /** Label reflecting the current metric to input values in*/
    private Label liftEditMetricLabel;

    /** Label to display errors */
    private Label statusLabel;

    public JudgePane(HashMap<String, ArrayList<Lifter>> lifterGroups){
        this.lifterGroups = lifterGroups;
        this.heightProperty().addListener(new resizeListener());
        /*
        LEFT menu
        ____________________________________________________________________________________________
        */

        // gridPane with 3 labels (squat, bench deadlift)
        trackingMenu = new GridPane();
        trackingMenu.setVgap(5);
        trackingMenu.setGridLinesVisible(true);
        trackingMenu.setStyle("-fx-background-color: lightblue");
        
        // set column/row constraints for each section
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();

        RowConstraints r1 = new RowConstraints();
        RowConstraints r2 = new RowConstraints();
        RowConstraints r3 = new RowConstraints();
        RowConstraints r4 = new RowConstraints();

        c1.setPercentWidth(50);
        c1.setHalignment(HPos.CENTER);
        c2.setPercentWidth(50);
        c2.setHalignment(HPos.CENTER);
        trackingMenu.getColumnConstraints().addAll(c1, c2);

        r1.setPercentHeight(25); // lifts
        r2.setPercentHeight(25); 
        r3.setPercentHeight(10); // edit lift bar
        r4.setPercentHeight(40); // widgets
        trackingMenu.getRowConstraints().addAll(r1, r2, r3, r4);

        // create labels to display lifts
        squatLabel = new Label();
        benchLabel = new Label();
        deadliftLabel = new Label();
        totalLabel = new Label();
        
        // create  hbox for edit pane (edit lift label, lift combo, attempt combo, weight textbox)
        HBox liftEditor = new HBox(5);
        liftEditor.setAlignment(Pos.CENTER);

        Label editLiftLabel = new Label("Edit Lift: ");
        liftEditMetricLabel = new Label("KG");

        liftCombo = new ComboBox<String>();
        liftCombo.getItems().addAll("Squat", "Bench", "Deadlift");
        liftCombo.setValue("Squat");

        attemptCombo = new ComboBox<Integer>();
        attemptCombo.getItems().addAll(1, 2, 3);
        attemptCombo.setValue(1);

        attemptInput = new TextField();

        statusCombo = new ComboBox<String>();
        statusCombo.getItems().addAll("Good", "Fail", "Projection");
        statusCombo.setValue("Projection");

        editSubmissionButton = new Button("Submit");
        editSubmissionButton.setOnAction(new editSubmissionHandler());

        loadDiagramButton = new Button("Load");
        loadDiagramButton.setOnAction(new loadButtonHandler());

        // construct edit lift bar
        liftEditor.getChildren().addAll(editLiftLabel, liftCombo, attemptCombo, attemptInput, liftEditMetricLabel, statusCombo, editSubmissionButton, loadDiagramButton);

            // Panel with timer, plate diagram and light display
        timerWidget = new TimerWidget(60);
        plateDiagram = new PlateDiagram();

            // position labels within grid
        trackingMenu.add(squatLabel, 0, 0);
        trackingMenu.add(benchLabel, 1, 0);
        trackingMenu.add(deadliftLabel, 0, 1);
        trackingMenu.add(totalLabel, 1, 1);
        trackingMenu.add(liftEditor,0, 2, 2, 1);
        trackingMenu.add(timerWidget, 0, 3);
        trackingMenu.add(plateDiagram, 1, 3);

        /*
        RIGHT menu
        ____________________________________________________________________________________________
        */

        lifterSelection = new VBox();
        lifterSelection.setStyle("-fx-background-color: #ad8363");

        // SELECTION FILTER
        VBox listSelection = new VBox(5);
        listSelection.setAlignment(Pos.CENTER);

        Label divisionLabel = new Label("Division");

        divisionCombo = new ComboBox<String>();
        divisionCombo.getItems().addAll("Sub-Junior", "Junior", "Open", "Masters-1", "Masters-2", "Masters-3", "Masters-4");
        divisionCombo.setOnAction(new selectionCriteriaHandler());
        divisionCombo.setValue("Sub-Junior");

        Label weightClassLabel = new Label("Weight Class");

        weightClassCombo = new ComboBox<String>();
        weightClassCombo.getItems().addAll("59kgs/130lbs", "66kgs/145lbs", "74kgs/163lbs", "83kgs/183lbs", "93kgs/205lbs", "105kgs/231lbs", "120kgs/265lbs", "120kgs+/265lb+");
        weightClassCombo.setOnAction(new selectionCriteriaHandler());
        weightClassCombo.setValue("59kgs/130lbs");

        metricCombo = new ComboBox<Metric>();
        metricCombo.setOnAction(new switchMetricHandler());
        metricCombo.getItems().addAll(Metric.KG, Metric.LB);
        metricCombo.setValue(Metric.KG);

        // Organize within containers
        HBox weight = new HBox(5, weightClassLabel, weightClassCombo);
        weight.setAlignment(Pos.CENTER);

        HBox div = new HBox(5, divisionLabel, divisionCombo);
        div.setAlignment(Pos.CENTER);

        // add to vbox
        listSelection.getChildren().addAll(weight, div, metricCombo);

        // LIFTER SELECTION
        scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(1920/3);
        scroll.setMinWidth(150);
        scroll.setPrefHeight(Integer.MAX_VALUE);

        scrollChild = new VBox(5);
        scrollChild.setPadding(new Insets(10, 10, 10, 10));
        scrollChild.setPrefWidth(Integer.MAX_VALUE);

        scroll.setContent(scrollChild);

        // ADD & REMOVE LIFTER
        HBox listEdit = new HBox(5);
        listEdit.setAlignment(Pos.CENTER);

        addButton = new Button("Add");
        addButton.setOnAction(new addButtonHandler());

        removeButton = new Button("Remove");
        removeButton.setOnAction(new removeButtonHandler());

        listEdit.getChildren().addAll(addButton, removeButton);

        // status label for error messages
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red");
        statusLabel.setAlignment(Pos.CENTER);

        // Combine the contents of the right menu
        lifterSelection.getChildren().addAll(listSelection, scroll, listEdit, statusLabel);

        /*
        PANE GRID ALIGNMENT
        ____________________________________________________________________________________________
        */

        // tracking menu
        ColumnConstraints left = new ColumnConstraints();
        // lifter selection
        ColumnConstraints right = new ColumnConstraints();

        this.getColumnConstraints().addAll(left, right);

        // define column width scaling
        left.setPercentWidth(75);
        right.setPercentWidth(25);

        // add content to cells
        this.add(lifterSelection, 1, 0);
        this.add(trackingMenu, 0, 0);

    }

    /** Retrieve the roster associated with selection criteria combo boxes and display each lifter in the scrollPane */
    private void refreshLifterSelection(){
        // query the hashmap to retrieve the corresponding division/weightclass roster
        String key = weightClassCombo.getValue() + "-" + divisionCombo.getValue();

        ArrayList<Lifter> selectedList = lifterGroups.get(key);

        // clear previous content
        scrollChild.getChildren().clear();

        // array present and contains lifter/s
        if(selectedList != null && selectedList.size() > 0){

            // set selection to first lifter in list
            selectedLifter = selectedList.get(0);

            // Load first lifter in list into left Pane
            loadLifter();

            // add lifter cards to the grid
            for(int i = 0; i < selectedList.size(); i++){

                LifterCard tempCard = new LifterCard(selectedList.get(i));

                // denote automatically selected lifter 
                if(i == 0){
                    tempCard.setBackground("lightgray");
                }

                tempCard.setOnMouseClicked(new lifterSelectedHandler());
                scrollChild.getChildren().add(tempCard);
            }
        }else{
            // clear left pane
            selectedLifter = null;
            squatLabel.setText("Squat:");
            benchLabel.setText("Bench:");
            deadliftLabel.setText("Deadlift:");
            totalLabel.setText("Total: --");
        }
    }

    /**Helper method to load a lifter's attempt information into the left pane for judging */
    private void loadLifter(){
        // format for each section
        String template = "%s:\n1) %s\n2) %s\n3) %s";
        // populate each label with the formatted string containing each of the lifter's attempts
        squatLabel.setText(String.format(template, "Squat", selectedLifter.printSquat(1), selectedLifter.printSquat(2), selectedLifter.printSquat(3)));
        benchLabel.setText(String.format(template, "Bench Press", selectedLifter.printBench(1), selectedLifter.printBench(2), selectedLifter.printBench(3)));
        deadliftLabel.setText(String.format(template, "Deadlift", selectedLifter.printDeadLift(1), selectedLifter.printDeadLift(2), selectedLifter.printDeadLift(3)));
        totalLabel.setText("Total: " + selectedLifter.printTotal());
    }

    /** Create a dialog box to collect information with which to build a lifter object */
    private Optional<Lifter> createLifter(){
        // Create a dialog box for the user to complete
        GridPane dialogContent = new GridPane();
        ButtonType submitButton = new ButtonType("Submit", ButtonData.OK_DONE);
        Label firstNameLabel = new Label("First Name: ");
        Label lastNameLabel = new Label("Last Name: ");
        Label ageLabel = new Label("Age: ");
        Label hometownLabel = new Label("Hometown");
        Label bodyweightLabel = new Label("Bodyweight (" + metricCombo.getValue() +"s): ");
        TextField firstNameInput = new TextField();
        TextField lastNameInput = new TextField();
        TextField ageInput = new TextField();
        TextField hometownInput = new TextField();
        TextField bodyweightInput = new TextField();

        dialogContent.add(firstNameLabel, 0, 0);
        dialogContent.add(lastNameLabel, 0, 1);
        dialogContent.add(ageLabel, 0, 2);
        dialogContent.add(hometownLabel, 0, 3);
        dialogContent.add(bodyweightLabel, 0, 4);
        dialogContent.add(firstNameInput, 1, 0);
        dialogContent.add(lastNameInput, 1, 1);
        dialogContent.add(ageInput, 1, 2);
        dialogContent.add(hometownInput, 1, 3);
        dialogContent.add(bodyweightInput, 1, 4);

        Dialog<Lifter> dialogBox = new Dialog<Lifter>();
        dialogBox.setTitle("Create new lifter");
        dialogBox.setHeaderText("Please complete each of the following fields");
        dialogBox.getDialogPane().setContent(dialogContent);
        dialogBox.getDialogPane().getButtonTypes().add(submitButton);

        // determine how to process the information based on the exit button
        dialogBox.setResultConverter(new Callback<ButtonType, Lifter>(){
            // implement the call method to be triggered when the submission buttonType is pressed
            public Lifter call(ButtonType b) {
                if(b == submitButton){
                    try{
                        // construct a new lifter with the given input
                        return new Lifter(firstNameInput.getText(), lastNameInput.getText(), Integer.parseInt(ageInput.getText()), hometownInput.getText(), Double.parseDouble(bodyweightInput.getText()), metricCombo.getValue());
                    }catch(Exception e){ 
                        // invalid format
                    }
                }
                return null;
            }
        });
        // display the dialog box until an exit button is pressed
        return dialogBox.showAndWait();
    }

    /** Create a card representation of a lifter to fit within the scroll pane*/
    private class LifterCard extends StackPane{
        /** The corresponding lifter obect to be extracted when clicked */
        private Lifter lifter;
        /** Nested pane to allow for scaling and constraints */
        private GridPane info;

        public LifterCard(Lifter lifter){
            info = new GridPane();
            this.lifter = lifter;
            // Display first and last name
            Label name = new Label(lifter.getFirstName() + " " + lifter.getLastName());
            name.setStyle("-fx-font-size: 15; -fx-font-weight: bold");
            GridPane.setHalignment(name, HPos.CENTER);

            // display age
            Label age = new Label("Age: " + lifter.getAge());
            GridPane.setHalignment(age, HPos.CENTER);

            // display hometown
            Label homeTown = new Label("Hometown:\n" + lifter.getHomeTown());
            homeTown.setWrapText(true);
            GridPane.setHalignment(homeTown, HPos.CENTER);

            // display bodyweight
            Label bodyWeight = new Label("Bodyweight: " + lifter.printBodyweight());
            GridPane.setHalignment(bodyWeight, HPos.CENTER);
            
            // add labels to grid
            info.add(name, 0, 0, 2, 1);
            info.add(homeTown, 0, 1);
            info.add(age, 1, 1);
            info.add(bodyWeight, 0, 2, 2, 1);
            info.setStyle("-fx-background-color: gray");
            
            // add constraints to grid
            ColumnConstraints c1 = new ColumnConstraints();
            RowConstraints r1 = new RowConstraints();
            c1.setPercentWidth(50);
            r1.setPercentHeight(33);
            info.getColumnConstraints().addAll(c1, c1);
            info.getRowConstraints().addAll(r1, r1, r1);

            this.setPrefWidth(Integer.MAX_VALUE);
            info.setPrefWidth(Integer.MAX_VALUE);
    
            // add gridpane to stackpane
            this.getChildren().add(info);
            
            // bind height to window width for scaling
            info.prefHeightProperty().bind(Bindings.divide(this.widthProperty(), 2));
        }

        /** Allow background of nested GridPane to be manipulated externally to denote selected lifter*/
        public void setBackground(String color){
            info.setStyle("-fx-background-color: " + color);
        }
    }

    /** Execute lifter creation procedure on click*/
    private class addButtonHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            Optional<Lifter> res = createLifter();
            if(res.isPresent()){ // object successfully created

                Lifter tempLifter = res.get();

                // update the appropriate lifter array
                String key = tempLifter.getWeightClass() + "-" + tempLifter.getDivsion();

                if(lifterGroups.keySet().contains(key)){
                    // array already exists
                    lifterGroups.get(key).add(tempLifter);
                }else{
                    // create new key/value pair
                    ArrayList<Lifter> firstEntry = new ArrayList<Lifter>();
                    firstEntry.add(tempLifter);
                    lifterGroups.put(key, firstEntry);
                }
                refreshLifterSelection();
            }else{
                // TODO update label unsuccessful
                // statusLabel.setText("Creation Unsuccessful");
            }
        }
    }

    /** Create a dialog box to confirm the deletion of the currently selected lifter */
    private class removeButtonHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(selectedLifter != null){
                // create dialog box to confirm removal
                Dialog<Boolean> dialogBox = new Dialog<Boolean>();
                ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
                ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
                dialogBox.setTitle("Lifter Removal");
                dialogBox.setHeaderText("Are you sure you would like to remove " + selectedLifter.getFirstName() + " " + selectedLifter.getLastName() + " from the " + selectedLifter.getWeightClass() + " " + selectedLifter.getDivsion() + " Division?");
                dialogBox.getDialogPane().getButtonTypes().addAll(confirm, cancel);

                // Define return value based on button selected
                dialogBox.setResultConverter(new Callback<ButtonType, Boolean>(){
                    public Boolean call(ButtonType b) {
                        if(b.getButtonData() == ButtonData.OK_DONE){
                            // deletion confirmed
                            return new Boolean(true);
                        }else{
                            // deletion cancelled/ window closed
                            return new Boolean(false);
                        }
                    }
                });
                Optional<Boolean> res = dialogBox.showAndWait();
                if(res.get()){
                    // remove lifter from arrayList and refresh the cards
                    String key = selectedLifter.getWeightClass() + "-" + selectedLifter.getDivsion();
                    lifterGroups.get(key).remove(selectedLifter);
                    refreshLifterSelection();
                    // TODO add successful removal update
                    // statusLabel.setText("Lifter removed successfully");
                }
            }
        }
    }

    /** Update the selected list to reflect the most current selection criteria */
    private class selectionCriteriaHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            refreshLifterSelection();
        }
        
    }

    /** Load the selected lifter into the judging pane and update the currently selected lifter*/
    private class lifterSelectedHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent event) {

            for(int i = 0; i < scrollChild.getChildren().size(); i++){
                LifterCard lc = (LifterCard) scrollChild.getChildren().get(i);
                lc.setBackground("gray");
            }

            LifterCard selected = ((LifterCard) event.getSource());
            selected.setBackground("lightgray");
            selectedLifter = selected.lifter;
            loadLifter();
        }
        
    }

    /** Extract information from the lift edit fields to update one of the currently selected lifter's attempts */
    private class editSubmissionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try{
                String lift = liftCombo.getValue();
                int attempt = attemptCombo.getValue().intValue();
                double weight = Double.parseDouble(attemptInput.getText());
                String state = statusCombo.getValue();
                Metric metric = metricCombo.getValue();

                if(lift.equals("Squat")){
                    selectedLifter.setSquat(attempt, weight, state, metric);
                }else if(lift.equals("Bench")){
                    selectedLifter.setBench(attempt, weight, state, metric);
                }else{
                    selectedLifter.setDeadlift(attempt, weight, state, metric);
                }

                loadLifter();
            }catch(Exception e){
                // TODO update status label
                // statusLabel.setText("Please fill in all fields");
            }
        }
    }

    /** Automatically load the weight specified in the lift edit input into the plate diagram */
    private class loadButtonHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try{
                plateDiagram.loadNewDiagram(Double.parseDouble(attemptInput.getText()), metricCombo.getValue());
            }catch(Exception e){}
        }
    }

    /** Set the current metric to accept weights in including bodyweight and lift weights */
    private class switchMetricHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            liftEditMetricLabel.setText(metricCombo.getValue().toString());
        }
    }

    /** Scale the label fonts to maintain proportionality with the size of the window */
    private class resizeListener implements ChangeListener<Number>{
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Font resized = new Font(newValue.intValue()/30);
            squatLabel.fontProperty().set(resized);
            benchLabel.fontProperty().set(resized);
            deadliftLabel.fontProperty().set(resized);
            totalLabel.fontProperty().set(resized);
        }
    }
}
