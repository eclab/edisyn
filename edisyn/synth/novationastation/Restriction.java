package edisyn.synth.novationastation;

public interface Restriction {
    String[] getValues();
    Integer getMin();
    Integer getMax();
    Integer getOffset();
}
