package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Branch implements Serializable {
    HashMap<String, String> branches;

    public Branch() {
        this.branches = new HashMap<>();
    }
}
