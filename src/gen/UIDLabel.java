package gen;

/**
 * UIDLabel
 */
public class UIDLabel {
    private int curr_id = 1;

    public UIDLabel() {}

    public String mk(String label) {
        return String.format("%s_%d", label, curr_id++);
    }

}