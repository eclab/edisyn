package edisyn.synth.novationastation;

/**
 * interface defining boundaries (limits) on a value
 */
public interface Boundary {
    /**
     * get list of the only possible (string) values
     *
     * @return String array or null if not applicable
     */
    String[] getValues();

    /**
     * get minimum value allowed
     *
     * @return min value
     */
    int getMin();

    /**
     * get maximum value allowed
     *
     * @return max value
     */
    int getMax();

    /**
     * get offset to be applied on a value (before presenting it)
     * if set, this typically defines the "zero" point for a symmetric range (defined by min and max)
     *
     * @return offset
     */
    int getOffset();

    /**
     * validate if a given value matches the boundaries
     *
     * @param value value to validate
     * @return true if validated fine.
     */
    default boolean validate(int value) {
        return value >= getMin() && value <= getMax();
    }

    ;
}
