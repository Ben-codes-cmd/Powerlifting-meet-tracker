public class Lift {
    private UniversalWeight weight;
    private String state;

    public Lift(){
        this.weight = new UniversalWeight();
        this.state = "Empty";
    }

    public double getWeight(Metric metric){
        return weight.getWeight(metric);
    }

    public String getState(){
        return state;
    }

    public void setWeight(double weight, Metric metric){
        this.weight.setWeight(weight, metric);
    }

    public void setState(String state){
        this.state = state;
    }

    public String weightString(){
        return weight.toString();
    }
    
    @Override
    public String toString(){
        return weight.toString() + "(" + state + ")";
    }
}
