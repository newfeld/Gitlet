package gitlet;




import java.io.IOException;
import java.util.Arrays;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Noah Newfeld
 */
public class Main {
    /**
     * List of valid Commands.
     */
    static final String[] COMMANDS = {"init", "add",
        "commit",
        "rm", "log", "global-log",
        "find", "status", "checkout", "branch",
        "rm-branch", "reset", "merge"};


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {

        Repo repo = new Repo();
        if (args.length == 0) {
            exitMessage("Please enter a command.");
        }
        if (!Arrays.stream(COMMANDS).toList().contains(args[0])) {
            exitMessage("No command with that name exists.");
        }
        switch (args[0]) {
        case "init" :
            repo.init();
            break;
        case "add" :
            if (args.length == 2) {
                repo.add(args);
            }
            break;
        case "commit" :
            if (args.length == 2 && !args[1].equals("")) {
                repo.gitCommit(args[1]);
            } else {
                exitMessage("Please enter a commit message.");
            }
            break;
        case "checkout" :
            repo.checkout(args);
            break;
        case "log" :
            repo.log();
            break;
        case "global-log" :
            repo.globalLog();
            break;
        case "rm" :
            repo.gitRemove(args);
            break;
        case "find" :
            repo.find(args);
            break;
        case "status" :
            repo.status();
            break;
        case "branch" :
            repo.branch(args);
            break;
        case "rm-branch" :
            repo.removeBranch(args);
            break;
        case "reset" :
            repo.reset(args);
            break;
        case "merge" :
            repo.merge(args);
            break;
        default:
            exitMessage("No command with that name exists.");
        }
        System.exit(0);
    }

    public static void exitMessage(String msg) {
        if (msg != null && msg != "") {
            System.out.println(msg);
        }
        System.exit(0);
    }
}
