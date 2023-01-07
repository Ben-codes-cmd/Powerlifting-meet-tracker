import java.text.DecimalFormat;

public class UniversalWeight{
    private double weightKG;
    private double weightLB;

    public UniversalWeight(){
        setWeight(0, Metric.KG);
    }

    public UniversalWeight(double weight, Metric metric){
        setWeight(weight, metric);
    }

    public void setWeight(double weight, Metric metric){
        if(metric == Metric.KG){
            weightKG = weight;
            weightLB = weight * 2.205;
        }else{
            weightLB = weight;
            weightKG = weight /2.205;
        }
    }

    public double getWeight(Metric metric){
        if(metric == Metric.KG){
            return weightKG;
        }else{
            return weightLB;
        }
    }

    @Override
    public String toString(){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(weightKG) + "KGs/ " + df.format(weightLB) + "LBs";
    }
}