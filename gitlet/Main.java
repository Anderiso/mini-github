package gitlet;


import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @Andrew
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        List<String> valid = Arrays.asList(new String[]{"init", "add", "commit",
        "checkout", "log", "rm", "global-log", "find", "branch", "rm-branch",
        "status", "reset", "merge"});
        boolean initialized = false;
        if (Repository.GITLET_DIR.exists()) {
            initialized = true;
        }

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!valid.contains(firstArg)) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        if (!initialized && !firstArg.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.initCommand();
                System.exit(0);
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.addCommand(args[1]);
                System.exit(0);
                break;
            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                    break;
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.commitCommand(args[1], false, null);
                System.exit(0);
                break;
            case "checkout":
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutCommand(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutCommand(args[1], args[3]);
                } else if (args.length == 2){
                    Repository.checkoutBranchCommand(args[1]);
                }
                System.exit(0);
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.logCommand();
                System.exit(0);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.rmCommand(args[1]);
                System.exit(0);
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.globalLogCommand();
                System.exit(0);
                break;
            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.findCommand(args[1]);
                System.exit(0);
                break;
            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.branchCommand(args[1]);
                System.exit(0);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.rmBranchCommand(args[1]);
                System.exit(0);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.statusCommand();
                System.exit(0);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.resetCommand(args[1]);
                System.exit(0);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!initialized) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.mergeCommand(args[1]);
                System.exit(0);
                break;
        }
    }
}
