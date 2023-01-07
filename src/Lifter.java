public class Lifter {
    private String firstName;
    private String lastName;
    private int age;
    private String homeTown;
    private UniversalWeight bodyweight;
    private String division;
    private String weightClass;
    private Lift[] squat;
    private Lift[] bench;
    private Lift[] deadlift;
    private UniversalWeight total;

    public Lifter(String first, String last, int age, String homeTown, double bodyweight, Metric metric){
        this.firstName = first;
        this.lastName = last;
        this.age = age;
        this.homeTown = homeTown;
        this.bodyweight = new UniversalWeight(bodyweight, metric);
        squat = new Lift[]{new Lift(), new Lift(), new Lift()};
        bench = new Lift[]{new Lift(), new Lift(), new Lift()};
        deadlift = new Lift[]{new Lift(), new Lift(), new Lift()};
        total = new UniversalWeight();

        // determine division and weightclass
        if(age >= 70){
            division = "Masters-4";
        }else if(age >= 60){
            division = "Masters-3";
        }else if(age >= 50){
            division = "Masters-2";
        }else if(age >= 40){
            division = "Masters-1";
        }else if(age >= 24){
            division = "Open";
        }else if(age >= 19){
            division = "Junior";
        }else{
            division = "Sub-Junior";
        }

        bodyweight = this.bodyweight.getWeight(Metric.KG);

        if(bodyweight > 120){
            weightClass = "120kgs+/265lb+";
        }else if(bodyweight > 105){
            weightClass = "120kgs/265lbs";
        }else if(bodyweight > 93){
            weightClass = "105kgs/231lbs";
        }else if(bodyweight > 83){
            weightClass = "93kgs/205lbs";
        }else if(bodyweight > 74){
            weightClass = "83kgs/183lbs";
        }else if(bodyweight > 66){
            weightClass = "74kgs/163lbs";
        }else if(bodyweight > 59){
            weightClass = "66kgs/145lbs";
        }else{
            weightClass = "59kgs/130lbs";
        }
    }

    // GETTERS

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public int getAge(){
        return age;
    }

    public String getHomeTown(){
        return homeTown;
    }

    public double getBodyweight(Metric metric){
        return bodyweight.getWeight(metric);
    }

    public String getDivsion(){
        return division;
    }

    public String getWeightClass(){
        return weightClass;
    }

    public Lift getSquat(int attempt){
        return squat[attempt-1];
    }

    public Lift getBench(int attempt){
        return bench[attempt-1];
    }

    public Lift getDeadlift(int attempt){
        return deadlift[attempt-1];
    }

    public double getTotal(Metric metric){
        return total.getWeight(metric);
    }

    public Lift getMaxSquat(){
        return findLargest(squat);
    }

    public Lift getMaxBench(){
        return findLargest(bench);
    }

    public Lift getMaxDeadlift(){
        return findLargest(deadlift);
    }

    // SETTERS

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public void setAge(int age){
        this.age = age;
    }

    public void setHomeTown(String homeTown){
        this.homeTown = homeTown;
    }

    public void setBodyweight(double bodyweight, Metric metric){
        this.bodyweight.setWeight(bodyweight, metric);
    }

    public void setSquat(int attempt, double weight, String status, Metric metric){
        squat[attempt-1].setWeight(weight, metric);
        squat[attempt-1].setState(status);
        updateTotal();
    }

    public void setBench(int attempt, double weight, String status, Metric metric){
        bench[attempt-1].setWeight(weight, metric);
        bench[attempt-1].setState(status);
        updateTotal();
    }

    public void setDeadlift(int attempt, double weight, String status, Metric metric){
        deadlift[attempt-1].setWeight(weight, metric);
        deadlift[attempt-1].setState(status);
        updateTotal();
    }

    // DISPLAY METHODS

    public String printTotal(){
        return total.toString();
    }

    public String printSquat(int attempt){
        return squat[attempt-1].toString();
    }

    public String printBench(int attempt){
        return bench[attempt-1].toString();
    }

    public String printDeadLift(int attempt){
        return deadlift[attempt-1].toString();
    }

    public String printBodyweight(){
        return bodyweight.toString();
    }

    @Override 
    public String toString(){
        return firstName + " " + lastName + ": (S)" + getMaxSquat().weightString() + " | (B)" + getMaxBench().weightString() + " | (D)" + getMaxDeadlift().weightString() + " | (Total)" + total.toString();
    }
    // HELPERS

    private void updateTotal(){
        total.setWeight(findLargest(squat).getWeight(Metric.KG) + findLargest(bench).getWeight(Metric.KG) + findLargest(deadlift).getWeight(Metric.KG), Metric.KG);
    }

    private static Lift findLargest(Lift[] arr){
        double max = 0;
        Lift maxLift = new Lift();
        for(int i = 0; i < 3; i++){
            if(arr[i].getWeight(Metric.KG) > max && arr[i].getState().equals("Good")){
                // check if lift good via lift class
                maxLift = arr[i];
            }
        }
        return maxLift;
    }
}
