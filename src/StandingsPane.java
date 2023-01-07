import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class StandingsPane extends GridPane{
    GridPane grid;
    ComboBox<Comparator<Lifter>> sort;
    ComboBox<String> divisionCombo;
    ComboBox<String> weightClassCombo;
    Button report;
    ArrayList<Lifter> currentList;
    HashMap<String, ArrayList<Lifter>> lifterGroups;
    Comparator<Lifter> compareSquat;
    Comparator<Lifter> compareBench;
    Comparator<Lifter> compareDeadlift;
    Comparator<Lifter> compareTotal;

    public StandingsPane(HashMap<String, ArrayList<Lifter>> lifterGroups){
        this.lifterGroups = lifterGroups;
        this.setPadding(new Insets(5, 5, 5, 5));
        this.setGridLinesVisible(true);

        compareSquat = new sortSquat();
        compareBench = new sortBench();
        compareDeadlift = new sortDeadlift();
        compareTotal = new sortTotal();

        // CONSTRAINTS
        // Position column
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(5);
        c1.setHalignment(HPos.CENTER);
        
        // Fields
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(19);
        c2.setHalignment(HPos.CENTER);

        // Upper and lower controls
        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(7.5);
        r1.setValignment(VPos.CENTER);

        // Scroll Pane
        RowConstraints r2 = new RowConstraints();
        r2.setPercentHeight(85);

        this.getColumnConstraints().addAll(c1, c2, c2, c2, c2, c2);
        this.getRowConstraints().addAll(r1, r2, r1);

        // ADD ELEMENTS
        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);

        grid = new GridPane();
        grid.setVgap(5);
        grid.getColumnConstraints().addAll(c1, c2, c2, c2, c2, c2);

        scroll.setContent(grid);

        
        Label sortBy = new Label("Sort by");
        sort = new ComboBox<Comparator<Lifter>>();
        sort.getItems().addAll(compareTotal, compareSquat, compareBench, compareDeadlift);
        sort.setValue(compareTotal);
        sort.setOnAction(new sortingHandler());
        FlowPane sorting = new FlowPane(sortBy, sort);
        sorting.setAlignment(Pos.CENTER);
        sorting.setHgap(5);

        Label divisionLabel = new Label("Division");
        divisionCombo = new ComboBox<String>();
        divisionCombo.getItems().addAll("Sub-Junior", "Junior", "Open", "Masters-1", "Masters-2", "Masters-3", "Masters-4");
        divisionCombo.setValue("Sub-Junior");
        divisionCombo.setOnAction(new selectionHandler());
        FlowPane divisionSelection = new FlowPane(divisionLabel, divisionCombo);
        divisionSelection.setAlignment(Pos.CENTER);
        divisionSelection.setHgap(5);

        Label weightClassLabel = new Label("Weight Class");
        weightClassCombo = new ComboBox<String>();
        weightClassCombo.getItems().addAll("59kgs/130lbs", "66kgs/145lbs", "74kgs/163lbs", "83kgs/183lbs", "93kgs/205lbs", "105kgs/231lbs", "120kgs/265lbs", "120kgs+/265lb+");
        weightClassCombo.setValue("59kgs/130lbs");
        weightClassCombo.setOnAction(new selectionHandler());
        FlowPane weightClassSelection = new FlowPane(weightClassLabel, weightClassCombo);
        weightClassSelection.setAlignment(Pos.CENTER);
        weightClassSelection.setHgap(5);

        report = new Button("Compile meet report");
        report.setOnAction(new compileReportHandler());

        this.add(new Label("Rank"), 0, 0);
        this.add(new Label("Name"), 1, 0);
        this.add(new Label("Squat"), 2, 0);
        this.add(new Label("Bench Press"), 3, 0);
        this.add(new Label("Deadlift"), 4, 0);
        this.add(new Label("Total"), 5, 0);
        this.add(scroll, 0, 1, 6, 1);
        this.add(sorting, 1, 2);
        this.add(divisionSelection, 2, 2);
        this.add(weightClassSelection, 3, 2);
        this.add(report, 5, 2);
        // compile function
        // save/load roster
    }

    private void loadCurrentList(){
        grid.getChildren().clear();
        if(currentList != null){
            for(int i = 0; i < currentList.size(); i++){
                Lifter temp = currentList.get(i);
                grid.add(new Label(new Integer(i+1).toString()), 0, i);
                grid.add(new Label(temp.getFirstName() + " " + temp.getLastName()), 1, i);
                grid.add(new Label(temp.getMaxSquat().weightString()), 2, i);
                grid.add(new Label(temp.getMaxBench().weightString()), 3, i);
                grid.add(new Label(temp.getMaxDeadlift().weightString()), 4, i);
                grid.add(new Label(temp.printTotal()), 5, i);
            }
        }
    }

    private ArrayList<Lifter> sort(Comparator<Lifter> sort, ArrayList<Lifter> list){
        // Selection Sort implementation
        // iterate n-1 times
        for(int i = 0; i < list.size()-1; i++){
            int index = i;
            // compare with the rest of the unsorted values
            for(int j = i+1; j < list.size(); j++){
                if(sort.compare(list.get(index), list.get(j)) < 0){
                    index = j;
                }
            }
            if(index > i){
                // swap next largest
                Lifter temp = list.get(i);
                list.set(i, list.get(index));
                list.set(index, temp);
            }
        }
        return list;
    }

    private class selectionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            String key = weightClassCombo.getValue() + "-" + divisionCombo.getValue();
            currentList =  lifterGroups.get(key);
            if(currentList != null){
                // as to not re-sort the original order of the list
                currentList = (ArrayList<Lifter>) currentList.clone();
            }
            loadCurrentList();
        }
    }

    private class sortingHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            currentList = sort(sort.getValue(), currentList);
            loadCurrentList();
        }
    }

    private class compileReportHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try {
                // determine appropriate file name without overriding existing files
                String filename = new SimpleDateFormat("dd_MM_yyyy").format(new Date());
                File f = new File(filename + ".txt");
                int extension = 1;
                while(f.exists()){
                    f = new File(filename + "(" + extension + ").txt");
                    extension++;
                }
                // write report to file
                PrintWriter writer = new PrintWriter(f, "UTF-8");

                for(String division: divisionCombo.getItems()){
                    // age division heading
                    writer.write("Division: " + division + "\n");

                    for(String weightClass: weightClassCombo.getItems()){
                        // weight class heading
                        writer.write("Class: " + weightClass + "\n\n");
                        // query lifterGroups
                        String key = weightClass + "-" + division;
                        ArrayList<Lifter> cur = lifterGroups.get(key);

                        if(cur != null && cur.size() > 0){
                            // sort output by total 
                            cur = sort(compareTotal, cur);

                            for(int i = 0; i < cur.size(); i++){
                                // list each lifter including placement
                                writer.write(i+1 + " " + cur.get(i).toString() + "\n\n");
                            }
                        }else{
                            // no lifters exist
                            writer.write("No competitors present.\n\n");
                        }
                        writer.write("\n");
                    }
                    writer.write("--------------------------------------------------------\n");
                }
                writer.close();
            } catch (Exception e) {} 
        }
    }

    private class sortSquat implements Comparator<Lifter>{
        @Override
        public int compare(Lifter o1, Lifter o2) {
            return Double.compare(o1.getMaxSquat().getWeight(Metric.KG), o2.getMaxSquat().getWeight(Metric.KG));
        }

        @Override
        public String toString(){
            return "Squat";
        }
    }

    private class sortBench implements Comparator<Lifter>{
        @Override
        public int compare(Lifter o1, Lifter o2) {
            return Double.compare(o1.getMaxBench().getWeight(Metric.KG), o2.getMaxBench().getWeight(Metric.KG));
        }

        @Override
        public String toString(){
            return "Bench Press";
        }
    }

    private class sortDeadlift implements Comparator<Lifter>{
        @Override
        public int compare(Lifter o1, Lifter o2) {
            return Double.compare(o1.getMaxDeadlift().getWeight(Metric.KG), o2.getMaxDeadlift().getWeight(Metric.KG));
        }

        @Override
        public String toString(){
            return "Deadlift";
        }
    }

    private class sortTotal implements Comparator<Lifter>{
        @Override
        public int compare(Lifter o1, Lifter o2) {
            return Double.compare(o1.getTotal(Metric.KG), o2.getTotal(Metric.KG));
        }

        @Override
        public String toString(){
            return "Total";
        }
    }
}
