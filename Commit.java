package gitlet;

import java.io.Serializable;;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;


/** Commit class for commit objects.
 * @author Noah Newfeld
 */

public class Commit implements Serializable {

    /**
     * Log message of commit.
     */
    private String _msg = null;

    /**
     * Parent id of this commit.
     */
    private String _parentpoint;

    /**
     * Date and time of the commit.
     */
    private String _timestamp;

    /**
     * True if the commit has a pointer from a merge commit.
     */
    private boolean hasMerge = false;
    /**
     * Commit id of the merge commit to this commit if applicable.
     */
    private String mergePointer = null;
    /**
     * Hashmap of blobs tracked by this commit,
     * maps String file name to String id.
     */
    private HashMap<String, String> comblobs;

    public Commit(String parent, HashMap<String, String> blobs, String msg) {
        _parentpoint = parent;
        _msg = msg;
        comblobs = blobs;
        if (msg == "initial commit") {
            _timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE MMM dd hh:mm:ss YYYY");
            Date date = new Date();
            _timestamp = formatter.format(date) + " -0800";
        }


    }

    public String getID() {
        byte[] thiscommit = Utils.serialize(this);
        String thisid = Utils.sha1(thiscommit);
        return thisid;
    }

    public String getMsg() {
        return _msg;
    }
    public String getTimestamp() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return time.format(formatter);
    }

    public String getParent() {
        return _parentpoint;
    }

    public String log() {
        return "===\n" + "commit " + getID() + "\n" + "Date: "
                        + _timestamp + "\n" + _msg + "\n";
    }
    public boolean isHasMerge() {
        return hasMerge;
    }

    public void indicateMerge() {
        hasMerge = true;
    }
    public String getMergePoint() {
        return mergePointer;
    }

    public void assignMergePoint(String mergeId) {
        mergePointer = mergeId;
    }

    public HashMap<String, String> getBlobs() {
        return comblobs;
    }

}
