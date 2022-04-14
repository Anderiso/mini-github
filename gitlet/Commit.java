package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Repository.COMMITS_DIR;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @Andrew
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private String parent;
    private String secondParent;
    private HashMap<String, String> blobs;
    private Boolean split;

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.split = false;
        this.secondParent = null;
        if (this.parent == null) {
            this.timestamp = new Date(0);
            this.blobs = new HashMap<>();
        } else {
            this.timestamp = new Date();
            Commit myParent = getCommit(this.parent);
            this.blobs = myParent.blobs;
        }

    }

    public Commit(String message, String parent, String secondParent) {
        this.message = message;
        this.parent = parent;
        this.split = false;
        this.secondParent = secondParent;
        if (this.parent == null) {
            this.timestamp = new Date(0);
            this.blobs = new HashMap<>();
        } else {
            this.timestamp = new Date();
            Commit myParent = getCommit(this.parent);
            this.blobs = myParent.blobs;
        }
    }

    public void setSplit() {
        this.split = true;
    }

    public static Commit getCommit(String c) {
        if (c == null) {
            return null;
        }
        if (c.length() < 40) {
            for (String name : plainFilenamesIn(COMMITS_DIR)) {
                if (c.equals(name.substring(0, c.length()))) {
                    c = name;
                }
            }
        }
        File target = Utils.join(COMMITS_DIR, c);
        if (target.exists()) {
            Commit result = readObject(target, Commit.class);
            return result;
        } else {
            return null;
        }
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String getParent() {
        return parent;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return sha1(Utils.serialize(this));
    }

    public Boolean getSplit() {
        return split;
    }

    public String getSecondParent() {
        return secondParent;
    }
}
