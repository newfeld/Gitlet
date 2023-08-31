package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    /**
     * Name of the branch.
     */
    private String _name;
    /**
     * Commit id of the head commit.
     */
    private String _head;


    public Branch(String name, String commitid) {
        _name = name;
        _head = commitid;
    }

    public String getName() {
        return _name;
    }

    public String getHead() {
        return _head;
    }


}
