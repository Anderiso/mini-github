package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.io.File;

public class StagingArea implements Serializable {
    HashMap<String, File> addTarget;
    HashMap<String, File> removeTarget;

    public StagingArea() {
        this.addTarget = new HashMap<>();
        this.removeTarget = new HashMap<>();
    }

    public HashMap<String, File> getAddTarget() {
        return addTarget;
    }

    public HashMap<String, File> getRemoveTarget() {
        return removeTarget;
    }
}
