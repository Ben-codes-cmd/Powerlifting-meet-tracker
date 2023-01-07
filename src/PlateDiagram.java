/** Plate Diagram Widget
 *  Fully scalable plate diagram widget to be embedded into UI
 *  Uses data collected from common weight plate sets to represent half of a loaded bar totaling a specified weight
 *  Allows for entry and conversion between pounds and kilograms
 *  Author: Ben Jordan 
 *  12/27/2022
 */

import java.util.ArrayList;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public class PlateDiagram extends StackPane{

    /** Preset weight set using dimensions from kilogram plates */
    private weightSet kgSet = new weightSet(new double[]{25, 20, 15, 10, 5, 2.5, 1.25, 0.5, 0.25}, 
                                            new String[]{"red", "blue", "yellow", "green", "white", "black", "silver", "silver", "silver"}, 
                                            new double[]{6.24, 5.20, 4.85, 4.85, 4.97, 3.70, 2.77, 1.85, 1.39}, 
                                            new double[]{1.000, 1.000, 0.889, 0.722, 0.507, 0.422, 0.356, 0.298, 0.249},
                                            20);

    /** Preset weight set using dimensions from poundage plates */
    private weightSet lbSet = new weightSet(new double[]{45, 35, 25, 10, 5, 2.5}, 
                                            new String[]{"Silver", "Silver", "Silver", "Silver", "Silver", "Silver"}, 
                                            new double[]{13.16, 8.08, 8.08, 5.87, 3.67, 2.93}, 
                                            new double[]{1.000, .806, .601, .5, .439, .354},
                                            45);

    /** Determines which weight set to load bar with */
    private weightSet selectedSet;

    /** Wraps diagram pane to allow for elements above and below */
    private GridPane wrapper;

    /** Houses plate representation */
    private GridPane diagram;

    /** Number of plates currently loaded onto the bar */
    private int numPlates;

    /** Percentage of barbell sleeve occupied by plates */
    private int percentLoaded;

    /** Array of plates currently loaded to be checked and scaled by height observer */
    private ArrayList<Plate> plateArray;

    /** Label displaying error messages */
    private Label error;

    /** Label prompting user to enter weight*/
    private Label weightLabel;

    /** Label displaying the weight conversion to the non-selected metric */
    private Label alternateMetric;

    /** Text field used to gather user input */
    private TextField input;

    /** Combo box allowing user to select weight metric */
    private ComboBox<Metric> metricSelector;

    /** Instantiate a new plate diagram in the context of a stackPane */
    public PlateDiagram(){
        // this.setStyle("-fx-background-color: #2a2e2b");
        this.setPadding(new Insets(10, 10, 10, 10));
        // GRIDPANE
        wrapper = new GridPane();
        wrapper.setVgap(5);
        wrapper.setAlignment(Pos.CENTER);
        diagram = new GridPane();
        diagram.setStyle("-fx-background-color: lightblue");

        // add gap between plates
        diagram.setHgap(2);

        // Allow diagram to fill vertical space
        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(100);
        diagram.getRowConstraints().add(r1);

        // prevent from disallowing window resizing
        diagram.setMinWidth(0);
        diagram.setMinHeight(0);

        // Fill vertical and horizontal window space
        diagram.setPrefWidth(Integer.MAX_VALUE);
        diagram.setPrefHeight(Integer.MAX_VALUE);

        // maintain proportional diagram dimensions (relative to the 433mm sleeve of a bar)
        diagram.maxHeightProperty().bind(Bindings.divide(this.widthProperty(), 2));
        diagram.maxWidthProperty().bind(Bindings.multiply(this.heightProperty(), 2));
        
        // bind listener to height of diagram to scale plates
        diagram.heightProperty().addListener(new heightAlignmentListener());

        // Align diagram within wrapper
        GridPane.setHalignment(diagram, HPos.CENTER);

        // Create labels
        error = new Label();
        error.setStyle("-fx-text-fill: red;");
        GridPane.setHalignment(error, HPos.CENTER);

        weightLabel = new Label("Input Weight: ");
        alternateMetric = new Label("LBS: ");

        // Create user input field
        input = new TextField();
        input.setPrefWidth(45);
        input.textProperty().addListener(new inputListener());

        // Create metric selection combo
        metricSelector = new ComboBox<Metric>();
        metricSelector.getItems().addAll(Metric.KG, Metric.LB);
        metricSelector.setOnAction(new metricSelectionListener()); // update selected weightSet and diagram on change
        metricSelector.setValue(Metric.KG); // set default value to kilograms
        selectedSet = kgSet; // set default selected set to Kilograms

        // Initialize variables
        numPlates = 0;
        percentLoaded = 0;
        plateArray = new ArrayList<Plate>();

        // add input elements to a container
        HBox inputBar = new HBox();
        inputBar.setSpacing(5);
        inputBar.setPrefWidth(Integer.MAX_VALUE);
        inputBar.setAlignment(Pos.CENTER);
        inputBar.getChildren().addAll(metricSelector, weightLabel, input, alternateMetric);
        GridPane.setHalignment(inputBar, HPos.CENTER);

        //  add visible elements to the wrapper grid
        wrapper.add(inputBar, 0, 0);
        wrapper.add(diagram, 0, 1);
        wrapper.add(error, 0, 2);

        // add the wrapper grid to this stackPane
        this.getChildren().add(wrapper);
    }

    /** Helper method to add a plate to the diagram*/
    private void addPlate(Plate plate){

        // Check if the bar will be full
        if(plate.widthPercentage+percentLoaded <= 93){
            diagram.add(plate, numPlates, 0);

            // add the plate's unique scaling constraints to its grid cell
            diagram.getColumnConstraints().add(plate.widthConstraint);

            // allow plate to recieve height updates
            plateArray.add(plate);

            // increment the next open plate position in the grid
            numPlates++;
            percentLoaded += plate.widthPercentage;
        }else{
            error.setText("Too many plates - Bar Full");
        }
    }

    /** Reset the diagram cells and reinitialize tracking variables */
    private void clearPlates(){
        diagram.getChildren().clear();
        diagram.getColumnConstraints().clear();
        numPlates = 0;
        percentLoaded = 0;
        plateArray.clear();
    }

    /** Public method to autofill the weight input */
    public void loadNewDiagram(double weight, Metric metric){
        metricSelector.setValue(metric);
        input.setText(new Double(weight).toString());
    }

    /** Use the selected weight set to load the plates necessary to fill half of the bar, not including the weight of the bar specified*/
    private void loadPlates(double weight){

        clearPlates();
        if(weight >=selectedSet.barWeight){

            // determine how much weight is to be loaded onto one side
            double toLoad = (weight-selectedSet.barWeight)/2;

            // parallel array to track the number of each plate type needed
            int[] platesNeeded = new int[selectedSet.plateOptions.length];

            // determine if combination possible
            for(int i = 0; i < selectedSet.plateOptions.length; i++){

                // calculate maximum number of plates that can be loaded of the next heaviest weight
                int numPlates = (int)(toLoad/selectedSet.plateOptions[i]);

                // add number of plates to parallel array
                platesNeeded[i] = numPlates;

                // subtract from weight to be loaded
                toLoad -= numPlates * selectedSet.plateOptions[i];
            }
            if(toLoad == 0){
                // for each type of plate
                for(int i = 0; i < platesNeeded.length; i++){
                    // add the number of plates stated in the parallel array
                    for(int j = 0; j < platesNeeded[i]; j++){
                        Plate newPlate = new Plate(selectedSet.colors[i], selectedSet.widthPercentages[i], selectedSet.heightFactors[i], selectedSet.plateOptions[i]);
                        addPlate(newPlate);
                    }
                }
                // add collar to the end of the configuration
                Plate collar = new Plate("#242526", 7, .2, 0.0);
                addPlate(collar);
                Plate bar = new Plate("silver", Math.min(93-percentLoaded, 5), .1, 0);
                addPlate(bar);
            }else{
                error.setText("Plate combination not possible");
            }
        }
   }

   /** Defines attributes of a weight plate to be added to the plate diagram*/
    private class Plate extends StackPane{

        /** Defines a percentage with which to scale the width of the plate */
        private ColumnConstraints widthConstraint;

        /** A factor by which to scale the plate height relative to the largest plate in the set*/
        private double heightFactor;

        /** Used to calculate the remaining capacity of the bar*/
        private double widthPercentage;

        /** Displays the weight of the plate */
        private Label label;

        public Plate(String color, double widthPercentage, double heightFactor, double weight){
            this.heightFactor = heightFactor;
            this.widthPercentage = widthPercentage;

            // Prepare alignment for diagram
            GridPane.setValignment(this, VPos.CENTER);

            // set initial height and width to expand, add style elements
            this.setStyle("-fx-background-color: " + color);
            this.setWidth(Integer.MAX_VALUE);
            this.setMaxHeight(diagram.getHeight()*heightFactor);

            // initialize column constraints and height attributes for scalability
            widthConstraint = new ColumnConstraints();
            widthConstraint.setPercentWidth(widthPercentage);

            // create label to display weight
            label = new Label(new Double(weight).toString());
            label.setWrapText(true);

            // determine what color to set the weight label
            if(weight != 5 && weight != 15 && metricSelector.getValue() == Metric.KG){
                label.setStyle("-fx-text-fill: white;");
            }
            if(weight == 0.0){
                label.setVisible(false);
            }
            this.getChildren().add(label);
        }
    }

    /** Provides the properties of each weight in a set to make plate calculating and loading methods more dynamic*/
    private class weightSet{

        /** The weights of each plate in a set, sorted from heaviest to lightest */
        double[] plateOptions;

        /** Color of each plate - parallel to plate options */
        String[] colors;

        /** Percentage of 433 mm bar sleeve that each plate occupies - parallel to plateOptions (0 to 100)*/
        double[] widthPercentages;

        /** scale of each plate's height relative to the tallest plate in the set - parallel to plateOptions (0 to 1)*/
        double[] heightFactors;
        
        /** The weight of a bar used for this set, consistent with the plateOption metric */
        int barWeight;
        
        public weightSet(double[] plateOptions, String[] colors, double[] widthPercentages, double[] heightFactors, int barWeight){
            this.plateOptions = plateOptions;
            this.colors = colors;
            this.widthPercentages = widthPercentages;
            this.heightFactors = heightFactors;
            this.barWeight = barWeight;
        }
    }

    /** Handles events on the metricSelection Combo to update the selectedSet and refresh the plate diagram accordingly */
    private class metricSelectionListener implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {

            // retrieve the source of the event
            ComboBox<Metric> source = (ComboBox<Metric>) event.getSource();

            // determine the user selection
            if(source.getValue() == Metric.KG){
                selectedSet = kgSet;
            }else{
                selectedSet = lbSet;
            }

            // trigger the textField listener to refresh the plate diagram with the new weight set
            String refreshText = input.getText();
            input.setText("");
            input.setText(refreshText);
        }

    }

    /** Observe the height of the diagram to scale plate height - bound to the heightProperty */
    private class heightAlignmentListener implements ChangeListener<Number>{

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            // iterate through each plate on bar
            for(int i = 0; i < plateArray.size(); i++){
                Plate curPlate = plateArray.get(i);
                // update the height to scale
                curPlate.setMaxHeight(newValue.intValue()*curPlate.heightFactor);
            }
        }
    }

    /** Listens for user input in the textField - bound to input textProperty */
    private class inputListener implements ChangeListener<String>{
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            
            // reset error message
            error.setText("");

            try{
                Double inputVal = Double.parseDouble(newValue);

                // determine which metric to interpret value with
                if(metricSelector.getValue() == Metric.KG){
                    // input in kgs
                    alternateMetric.setText(String.format("%.2f", inputVal*2.205) + " lbs");
                    loadPlates(inputVal);
                }else{
                    // input in lbs
                    alternateMetric.setText(String.format("%.2f", inputVal/2.205) + " kgs");
                    loadPlates(inputVal);
                }
            }catch(NumberFormatException e){
                // could not parse double value from input
                if(newValue.equals("")){
                    clearPlates();
                    alternateMetric.setText("");
                }
            }  
        }
    }  
}