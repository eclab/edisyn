package edisyn.synth.novationastation;

/**
 * interface defining boundaries (limits) on a value
 */
public interface Boundary {
    /**
     * get list of the only possible (string) values
     * @return String array or null if not applicable
     */
    String[] getValues();

    /**
     * get minimum value allowed
     * @return
     */
    int getMin();

    /**
     * get maximum value allowed
     * @return
     */
    int getMax();

    /**
     * get offset to be applied on a value (before presenting it)
     * if set, this typically defines the "zero" point for a symmetric range (defined by min and max)
     * @return
     */
    int getOffset();
}
