package gitlet;


import java.io.File;
import java.util.*;


import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @Andrew
 */
public class Repository {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_FILE = join(GITLET_DIR, "branch");
    public static final File CURRBRANCH_FILE = join(GITLET_DIR, "current_branch");
    public static final File ADDITION_STAGED_DIR = join(GITLET_DIR, "staged_for_addition");
    public static final File REMOVAL_STAGED_DIR = join(GITLET_DIR, "staged_for_removal");

    private static Branch branch;
    private static String currentBranch;

    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        ADDITION_STAGED_DIR.mkdir();
        REMOVAL_STAGED_DIR.mkdir();
        Commit initialCommit = new Commit("initial commit", null);
        String code = sha1(Utils.serialize(initialCommit));
        File commitFile = join(COMMITS_DIR, code);
        writeObject(commitFile, initialCommit);

        branch = new Branch();
        currentBranch = "master";
        writeObject(CURRBRANCH_FILE, currentBranch);
        branch.branches.put(currentBranch, code);
        writeObject(BRANCH_FILE, branch);

    }

    public static void addCommand(String file) {
        File fileName = join(CWD, file);
        if (!fileName.exists()) {
            System.out.println("File does not exist");
            System.exit(0);
        }
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        HashMap<String, String> blobs = currCommit.getBlobs();
        copyFile(CWD, file, ADDITION_STAGED_DIR, file);
        if (blobs.containsKey(file)) {
            String cwdCode = hashContents(fileName);
            String commitCode = blobs.get(file);
            if (cwdCode.equals(commitCode)) {
                if (plainFilenamesIn(ADDITION_STAGED_DIR).contains(file)) {
                    File temp = join(ADDITION_STAGED_DIR, file);
                    temp.delete();
                }
            }
        }

        if (plainFilenamesIn(REMOVAL_STAGED_DIR).contains(file)) {
            File temp = join(REMOVAL_STAGED_DIR, file);
            temp.delete();
        }
    }

    public static void commitCommand(String message, boolean merge, String branchname) {
        if (!checkForChanges()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        String head = branch.branches.get(currentBranch);
        Commit currentCommit = new Commit(message, head);
        if (merge) {
            currentCommit = new Commit(message, head, branch.branches.get(branchname));
        }

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();

        for (String file :ADDITION_STAGED_DIR.list()) {
            File target = join(ADDITION_STAGED_DIR, file);
            byte[] contents = readContents(target);
            String code = sha1(contents);
            copyFile(ADDITION_STAGED_DIR, file, BLOBS_DIR, code);
            currentBlobs.put(file, code);
        }
        for (String file :REMOVAL_STAGED_DIR.list()) {
            currentBlobs.remove(file);
        }

        clearStagingArea();

        String code = sha1(Utils.serialize(currentCommit));
        File currentCommitFile = join(COMMITS_DIR, code);
        writeObject(currentCommitFile, currentCommit);

        branch.branches.put(currentBranch, code);
        writeObject(BRANCH_FILE, branch);

    }

    public static void logCommand() {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currentCommit = Commit.getCommit(branch.branches.get(currentBranch));
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit "
                    + currentCommit.getCode());
            System.out.println(String.format("Date: %1$ta %1$tb %1$te "
                    + "%1$tT %1$tY %1$tz", currentCommit.getTimestamp()));
            System.out.println(currentCommit.getMessage());
            System.out.println();
            currentCommit = Commit.getCommit(currentCommit.getParent());
        }
    }

    public static void checkoutCommand(String name) {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        HashMap<String, String> blobs = currCommit.getBlobs();
        if (!blobs.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File dest = join(CWD, name);
        File data = join(BLOBS_DIR, blobs.get(name));
        writeContents(dest, readContents(data));
    }

    public static void checkoutCommand(String uid, String name) {
        Commit currCommit = Commit.getCommit(uid);
        if (currCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
            return;
        }
        HashMap<String, String> blobs = currCommit.getBlobs();
        if (!blobs.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
            return;
        }
        File dest = join(CWD, name);
        File data = join(BLOBS_DIR, blobs.get(name));
        writeContents(dest, readContents(data));
    }

    public static void checkoutBranchCommand(String name) {
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        branch = readObject(BRANCH_FILE, Branch.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        HashMap<String, String> blobs = currCommit.getBlobs();
        if (!branch.branches.containsKey(name)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Commit givenCommit = Commit.getCommit(branch.branches.get(name));
        HashMap<String, String> givenblobs = givenCommit.getBlobs();
        for (String file : plainFilenamesIn(CWD)) {
            File temp = join(CWD, file);
            if (givenblobs.containsKey(file)
                    && !givenblobs.get(file).equals(hashContents(temp))
                    && !blobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }
        }

        if (currentBranch.equals(name)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        for (String key : blobs.keySet()) {
            File temp = join(CWD, key);
            temp.delete();
        }
        for (String key : givenblobs.keySet()) {
            copyFile(BLOBS_DIR, givenblobs.get(key), CWD, key);
        }

        currentBranch = name;
        writeObject(CURRBRANCH_FILE, currentBranch);
    }

    public static void rmCommand(String fileName) {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        boolean x = false;
        if (currCommit.getBlobs().containsKey(fileName)) {
            File temp = join(REMOVAL_STAGED_DIR, fileName);
            writeContents(temp, "");
            File f = join(CWD, fileName);
            f.delete();
            x = true;
        }
        if (plainFilenamesIn(ADDITION_STAGED_DIR).contains(fileName)) {
            File temp = join(ADDITION_STAGED_DIR, fileName);
            temp.delete();
            x = true;
        }
        if (!x) {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void globalLogCommand() {
        for (String commit : Utils.plainFilenamesIn(COMMITS_DIR)) {
            Commit commitObject = Commit.getCommit(commit);
            System.out.println("===");
            System.out.println("commit "
                    + commit);
            System.out.println(String.format("Date: %1$ta %1$tb %1$te "
                    + "%1$tT %1$tY %1$tz", commitObject.getTimestamp()));
            System.out.println(commitObject.getMessage());
            System.out.println();
        }
    }

    public static void statusCommand() {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        System.out.println("=== Branches ===");
        String[] branchnames = new String[branch.branches.keySet().size()];
        int counter = 0;
        for (Map.Entry<String, String> mapElement : branch.branches.entrySet()) {
            branchnames[counter] = mapElement.getKey();
            counter += 1;
        }
        Arrays.sort(branchnames);
        for (String name : branchnames) {
            if (name.equals(currentBranch)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String name : plainFilenamesIn(ADDITION_STAGED_DIR)) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String name : plainFilenamesIn(REMOVAL_STAGED_DIR)) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String name : plainFilenamesIn(CWD)) {
            File temp = join(CWD, name);
            if (currCommit.getBlobs().containsKey(name)
                    && !currCommit.getBlobs().equals(hashContents(temp))) {
                System.out.println(name + " (modified)");
            }
            if (plainFilenamesIn(ADDITION_STAGED_DIR).contains(name)) {
                File add = join(ADDITION_STAGED_DIR, name);
                if (!hashContents(add).equals(hashContents(temp))) {
                    System.out.println(name + " (modified)");
                }
            }
        }
        for (String name : plainFilenamesIn(ADDITION_STAGED_DIR)) {
            if (!plainFilenamesIn(CWD).contains(name)) {
                System.out.println(name + " (deleted)");
            }
        }
        for (Map.Entry<String, String> name : currCommit.getBlobs().entrySet()) {
            if (!plainFilenamesIn(REMOVAL_STAGED_DIR).contains(name.getKey())
                    && !plainFilenamesIn(CWD).contains(name.getKey())) {
                System.out.println(name + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String name : plainFilenamesIn(CWD)) {
            if (!plainFilenamesIn(ADDITION_STAGED_DIR).contains(name) &&
            !currCommit.getBlobs().containsKey(name)) {
                System.out.println("name");
            }
        }
        System.out.println();
    }

    public static void findCommand(String message) {
        boolean found = false;
        for (String commit : Utils.plainFilenamesIn(COMMITS_DIR)) {
            Commit commitObject = Commit.getCommit(commit);
            if (commitObject.getMessage().equals(message)) {
                System.out.println(commitObject.getCode());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branchCommand(String name) {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        if (branch.branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
            return;
        }
        branch.branches.put(name, branch.branches.get(currentBranch));
        writeObject(BRANCH_FILE, branch);
    }

    public static void rmBranchCommand(String name) {
        branch = readObject(BRANCH_FILE, Branch.class);
        if (!branch.branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
            return;
        }
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        if (name.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
            return;
        }
        branch.branches.remove(name);
        writeObject(BRANCH_FILE, branch);
    }

    public static void resetCommand(String uid) {
        branch = readObject(BRANCH_FILE, Branch.class);
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit currCommit = Commit.getCommit(branch.branches.get(currentBranch));
        HashMap<String, String> currentBlobs = currCommit.getBlobs();
        Commit givenCommit = Commit.getCommit(uid);
        if (givenCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
            return;
        }
        HashMap<String, String> blobs = givenCommit.getBlobs();

        for (String file : plainFilenamesIn(CWD)) {
            File temp = join(CWD, file);
            if (blobs.containsKey(file) && !currentBlobs.containsKey(file)
                    && !blobs.get(file).equals(hashContents(temp))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
                return;
            }
        }
        clearStagingArea();
        for (Map.Entry<String, String> file : blobs.entrySet()) {
            checkoutCommand(uid, file.getKey());
        }

        for (String file : plainFilenamesIn(CWD)) {
            if (!blobs.containsKey(file)) {
                File temp = join(CWD, file);
                temp.delete();
            }
        }
        branch.branches.put(currentBranch, uid);
        writeObject(BRANCH_FILE, branch);
    }

    public static void mergeCommand(String name) {
        branch = readObject(BRANCH_FILE, Branch.class);
        if (!branch.branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
            return;
        }
        currentBranch = readObject(CURRBRANCH_FILE, String.class);
        Commit givenCommit = Commit.getCommit(branch.branches.get(name));
        Commit currentCommit = Commit.getCommit(branch.branches.get(currentBranch));
        Commit splitPoint = Commit.getCommit(locateSplitPoint(currentCommit,
                givenCommit));
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        HashMap<String, String> splitBlobs = splitPoint.getBlobs();
        HashSet<String> allFiles = initiateHashSet(givenBlobs, currentBlobs, splitBlobs);
        if (checkForChanges()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        for (String file : allFiles) {
            File temp = join(CWD, file);
            if (temp.exists() && givenBlobs.containsKey(file)
                    && !currentBlobs.containsKey(file)
                    && !givenBlobs.get(file).equals(hashContents(temp))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (currentBranch.equals(name)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        boolean conflict = false;
        if (splitPoint.equals(givenCommit)) {
            System.out.println("Given branch is an ancestor of the current branch");
            System.exit(0);
        }
        if (splitPoint.equals(currentCommit)) {
            checkoutBranchCommand(name);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (String file : allFiles) {
            String result = mergeResult(currentCommit, givenCommit, splitPoint, file);
            if (result.equals("checkout")) {
                copyFile(BLOBS_DIR, givenBlobs.get(file), CWD, file);
                copyFile(BLOBS_DIR, givenBlobs.get(file), ADDITION_STAGED_DIR, file);
            }
            if (result.equals("remove")) {
                File temp = join(REMOVAL_STAGED_DIR, file);
                writeContents(temp, "");
                File f = join(CWD, file);
                f.delete();
            }
            if (result.equals("conflict")) {
                String currentFile = currentBlobs.get(file);
                String givenFile = givenBlobs.get(file);
                conflictHandler(file, currentFile, givenFile);
                conflict = true;
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        commitCommand("Merged " + name + " into " + currentBranch
                + ".", true, name);
    }

    private static String locateSplitPoint(Commit a, Commit b) {
        String otherpath = null;
        if (a.getCode().equals(b.getCode())) {
            return a.getCode();
        }
        if (a.getParent() == null) {
            return a.getCode();
        }
        if (splitHelper(a, b) != null) {
            return a.getCode();
        }
        if (a.getSecondParent() != null) {
            otherpath = locateSplitPoint(Commit.getCommit(a.getSecondParent()), b);
        }
        return findMostRecent(otherpath, locateSplitPoint(Commit.getCommit(a.getParent()), b));
    }

    private static void copyFile(File sourcedir, String sourcename, File destdir, String destname) {
        File temp = join(sourcedir, sourcename);
        byte[] contents = readContents(temp);
        File copy = join(destdir, destname);
        writeContents(copy, contents);
    }

    private static void clearStagingArea() {
        for (String file : plainFilenamesIn(ADDITION_STAGED_DIR)) {
            File temp = join(ADDITION_STAGED_DIR, file);
            temp.delete();
        }
        for (String file : plainFilenamesIn(REMOVAL_STAGED_DIR)) {
            File temp = join(REMOVAL_STAGED_DIR, file);
            temp.delete();
        }
    }

    private static boolean checkForChanges() {
        if ((ADDITION_STAGED_DIR.list() == null || ADDITION_STAGED_DIR.list().length == 0)
                && (REMOVAL_STAGED_DIR.list() == null || REMOVAL_STAGED_DIR.list().length == 0)) {
            return false;
        }
        return true;
    }

    private static String hashContents(File file) {
        byte[] contents = readContents(file);
        String code = sha1(contents);
        return code;
    }

    private static String mergeResult(Commit a, Commit b, Commit split, String file) {
        String acode = a.getBlobs().get(file);
        String bcode = b.getBlobs().get(file);
        String splitcode = split.getBlobs().get(file);
        acode = unNull(acode);
        bcode = unNull(bcode);
        splitcode = unNull(splitcode);

        if (!splitcode.equals(acode) && !splitcode.equals(bcode) && !acode.equals(bcode)) {
            return "conflict";
        }
        if (splitcode.equals(acode) && !splitcode.equals(bcode)) {
            if (bcode.equals("")) {
                return "remove";
            } else {
                return "checkout";
            }
        }
        return "do nothing";
    }

    private static String unNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    private static HashSet<String> initiateHashSet(HashMap<String, String> a,
                                                   HashMap<String, String> b,
                                                   HashMap<String, String> c) {
        HashSet<String> result = new HashSet<>();
        for (Map.Entry<String, String> mapElement : a.entrySet()) {
            result.add(mapElement.getKey());
        }
        for (Map.Entry<String, String> mapElement : b.entrySet()) {
            result.add(mapElement.getKey());
        }
        for (Map.Entry<String, String> mapElement : c.entrySet()) {
            result.add(mapElement.getKey());
        }
        return result;
    }

    private static void conflictHandler(String file, String curr, String given) {
        File target = join(CWD, file);
        byte[] currentContent = new byte[0];
        byte[] givenContent = new byte[0];
        if (curr != null) {
            File currentFile = join(BLOBS_DIR, curr);
            currentContent = readContents(currentFile);
        }
        if (given != null) {
            File givenFile = join(BLOBS_DIR, given);
            givenContent = readContents(givenFile);
        }
        writeContents(target, "<<<<<<< HEAD\n", currentContent,
                "=======\n", givenContent, ">>>>>>>\n");
        addCommand(file);
    }

    private static String findMostRecent(String a, String b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (Commit.getCommit(a).getTimestamp().
                compareTo(Commit.getCommit(b).getTimestamp()) < 0) {
            return a;
        }
        return b;
    }

    private static String splitHelper(Commit a, Commit b) {
        if (b.getParent() == null && b.getSecondParent() == null) {
            return null;
        }
        if (b.getParent() == null) {
            return splitHelper(a, Commit.getCommit(b.getSecondParent()));
        }
        if (b.getSecondParent() == null) {
            return splitHelper(a, Commit.getCommit(b.getParent()));
        }
        if (b.getCode().equals(a.getCode())) {
            return a.getCode();
        }
        return findMostRecent(splitHelper(a, Commit.
                        getCommit(b.getParent())),
                splitHelper(a, Commit.getCommit(b.getSecondParent())));
    }
}

