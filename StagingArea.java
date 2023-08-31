package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class StagingArea implements Serializable {
    /**
     * Adding stage.
     */
    private TreeMap<String, String> addingArea;
    /**
     * Removal stage.
     */
    private ArrayList<String> removalArea;

    public StagingArea() {
        addingArea = new TreeMap<>();
        removalArea = new ArrayList<>();
    }
    public String getPath() {
        return ".gitlet/staging/stage.txt";
    }

    public void toAdd(String filename, String id) {
        addingArea.put(filename, id);
    }
    public void toRemove(String filename) {
        removalArea.add(filename);
    }

    public TreeMap<String, String> getAddingArea() {
        return addingArea;
    }
    public ArrayList<String> getRemovalArea() {
        return removalArea;
    }

    public void clearAddingArea() {
        addingArea = new TreeMap<>();
    }
    public void clearRemovalArea() {
        removalArea = new ArrayList<>();
    }

}
