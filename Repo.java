package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.List;



public class Repo {
    /**
     * Staging area of this repository.
     */
    private StagingArea stage;
    /**
     * Current working directory.
     */
    private File _CWD;

    /**
     * Boolean to indicate if a merge is happening.
     */
    private boolean isMerge = false;
    /**
     * indicating if there is a merge conflict.
     */
    private boolean hasConflict = false;
    /**
     * Commit id of the given branch if isMerge is true.
     */
    private String mergeID = null;
    /**
     * Main folder containing all necessary files.
     */
    static final File GITLETFOLDER = new File(".gitlet");
    /**
     * Folder for blobs.
     */
    static final File BLOBSFOLDER = new File(".gitlet/blobs");
    /**
     * Folder for commits.
     */
    static final File COMMITFOLDER = new File(".gitlet/commits");
    /**
     * Folder for branches.
     */
    static final File BRANCHFOLDER = new File(".gitlet/branch");
    /**
     * Folder to store the staging area.
     */
    static final File STAGINGFOLDER = new File(".gitlet/staging");

    /**
     * Records what the head branch is.
     */
    private static String headBranch = "master";

    public Repo() {
        _CWD = new File(System.getProperty("user.dir"));
        File stagingArea = new File(".gitlet/staging/stage.txt");
        if (stagingArea.exists()) {
            stage = Utils.readObject(stagingArea, StagingArea.class);
        }
        File headbranch = new File(".gitlet/branch/head.txt");
        if (headbranch.exists()) {
            headBranch = Utils.readContentsAsString(headbranch);
        }
    }

    /** Executes the init command, creating folders for gitlet, initializes an
     *  initial commit.
     */
    public void init() throws IOException {

        if (GITLETFOLDER.exists()) {
            exitMessage("A Gitlet version-control system "
                    + "already exists in "
                    + "the current directory.\n");
        }

        GITLETFOLDER.mkdir();
        BLOBSFOLDER.mkdir();
        COMMITFOLDER.mkdir();
        BRANCHFOLDER.mkdir();
        STAGINGFOLDER.mkdir();

        File stageFile = new File(".gitlet/staging/stage.txt");
        stage = new StagingArea();
        byte[] stageSer = Utils.serialize(stage);
        Utils.writeContents(stageFile, stageSer);

        Commit initcomm = new Commit(null, new HashMap<>(), "initial commit");
        File initcommfile = new File(".gitlet/commits/" + initcomm.getID()
                + ".txt");
        initcommfile.createNewFile();
        byte[] initcommser = Utils.serialize(initcomm);
        Utils.writeContents(initcommfile, initcommser);

        File headTracker = new File(".gitlet/branch/head.txt");
        Utils.writeContents(headTracker, "master");

        File headBranchFile = new File(".gitlet/branch/master.txt");
        headBranchFile.createNewFile();
        Branch master = new Branch("master", initcomm.getID());
        byte[] masterSer = Utils.serialize(master);
        Utils.writeContents(headBranchFile, masterSer);
    }

    /**
     *
     * @param args file names to be added to the staging folder.
     */
    public void add(String... args) {
        File adder = new File(args[1]);
        if (adder.exists()) {
            byte[] blob = Utils.readContents(adder);
            String blobID = Utils.sha1(blob);
            if (stage.getAddingArea() != null
                    && stage.getAddingArea().containsKey(args[1])) {
                if (stage.getAddingArea().get(args[1])
                        == currentCommit().getBlobs().get(args[1])) {
                    stage.getAddingArea().remove(args[1]);
                } else {
                    stage.getAddingArea().replace(args[1], blobID);
                }
            }
            if (stage.getRemovalArea() != null
                    && stage.getRemovalArea().contains(args[1])) {
                stage.getRemovalArea().remove(args[1]);
            }
            if (blobID.equals(currentCommit().getBlobs().get(args[1])))   {
                File stageFile = new File(".gitlet/staging/stage.txt");
                byte[] stageSer = Utils.serialize(stage);
                Utils.writeContents(stageFile, stageSer);
                return;
            }
            stage.toAdd(args[1], blobID);
            if (stage.getAddingArea().get(args[1]).equals(
                            currentCommit().getBlobs().get(args[1]))) {
                stage.getAddingArea().remove(args[1]);
            }
            File tempFile = new File(".gitlet/blobs/" + blobID + ".txt");
            Utils.writeContents(tempFile, blob);

            File stageFile = new File(".gitlet/staging/stage.txt");
            byte[] stageSer = Utils.serialize(stage);
            Utils.writeContents(stageFile, stageSer);

        } else {
            exitMessage("File does not exist.");
        }

    }


    public void gitCommit(String args) {
        if (stage.getAddingArea().isEmpty()
                && stage.getRemovalArea().isEmpty()) {
            exitMessage("No changes added to the commit.");
        }
        Commit parent = currentCommit();
        HashMap<String, String> blobmapcopy = new HashMap<>();
        Set<String> blobmapkeys = parent.getBlobs().keySet();
        for (String name : blobmapkeys) {
            if (!stage.getAddingArea().containsKey(name)
                    && !stage.getRemovalArea().contains(name)) {
                blobmapcopy.put(name, parent.getBlobs().get(name));
            }
            if (stage.getAddingArea().containsKey(name)) {
                blobmapcopy.replace(name, stage.getAddingArea().get(name));
            }
            if (stage.getRemovalArea().contains(name)) {
                blobmapcopy.remove(name);
            }
        }
        Set<String> stageKeys = stage.getAddingArea().keySet();
        for (String key : stageKeys) {
            blobmapcopy.put(key, stage.getAddingArea().get(key));
        }

        Commit newcommit = new Commit(parent.getID(), blobmapcopy, args);
        if (isMerge) {
            newcommit.indicateMerge();
            newcommit.assignMergePoint(mergeID);
        }
        byte[] serializedComm = Utils.serialize(newcommit);
        Utils.writeContents(new File(".gitlet/commits/"
                + newcommit.getID() + ".txt"), serializedComm);

        File updateHead = new File(".gitlet/branch/"
                + headBranch + ".txt");
        Branch branch = new Branch(headBranch, newcommit.getID());
        byte[] serializedBranch = Utils.serialize(branch);
        Utils.writeContents(updateHead, serializedBranch);

        stage.clearAddingArea();
        stage.clearRemovalArea();
        byte[] stageSer = Utils.serialize(stage);
        Utils.writeContents(new File(".gitlet/staging/stage.txt"),
                stageSer);
    }

    public void checkoutFile(String... args) {
        if (!args[1].equals("--")) {
            exitMessage("Incorrect operands.");
        }
        Commit curr = currentCommit();
        File checkFile = new File(_CWD.getPath(), args[2]);
        if (!curr.getBlobs().containsKey(args[2])) {
            exitMessage("File does not exist in that commit.");
        }
        File asItExists = new File(".gitlet/blobs/"
                + curr.getBlobs().get(args[2]) + ".txt");
        if (checkFile.exists()) {
            Utils.restrictedDelete(checkFile);
        }
        byte[] oldBlob = Utils.readContents(asItExists);
        File restoredFile = new File(_CWD.getPath(), args[2]);
        Utils.writeContents(restoredFile, oldBlob);
    }

    public void checkoutCommit(String... args) {
        if (!args[2].equals("--")) {
            exitMessage("Incorrect operands.");
        }
        File checkFile = new File(_CWD.getPath(), args[3]);
        String[] commitList = COMMITFOLDER.list();
        boolean contains = false;
        String commitid = null;
        for (String id : commitList) {
            if (id.startsWith(args[1])) {
                commitid = id;
                contains = true;
            }
        }
        if (!contains) {
            exitMessage("No commit with that id exists.");
        }
        if (checkFile.exists()) {
            Utils.restrictedDelete(checkFile);
        }
        File commitFile = new File(".gitlet/commits/" + commitid);
        Commit retrieveCommit = Utils.readObject(commitFile, Commit.class);
        if (!retrieveCommit.getBlobs().containsKey(args[3])) {
            exitMessage("File does not exist in that commit.");
        }
        File asItExists = new File(".gitlet/blobs/"
                + retrieveCommit.getBlobs().get(args[3]) + ".txt");
        byte[] oldBlob = Utils.readContents(asItExists);
        File restoredFile = new File(_CWD.getPath(), args[3]);
        Utils.writeContents(restoredFile, oldBlob);
    }

    public void checkoutBranch(String... args) {
        if (args[1].equals(headBranch)) {
            exitMessage("No need to checkout the current branch.");
        }
        List<String> branchList = Utils.plainFilenamesIn(new
                File(".gitlet/branch"));
        boolean branchexists = false;
        for (String branch : branchList) {
            if (branch.equals(args[1] + ".txt")) {
                branchexists = true;
            }
        }
        if (!branchexists) {
            exitMessage("No such branch exists.");
        }
        String branchName = args[1];
        Commit currCommit = currentCommit();

        File givenBranchFile = new File(".gitlet/branch/"
                + branchName + ".txt");
        Branch givenBranch = Utils.readObject(givenBranchFile, Branch.class);
        File givenCommitFile = new File(".gitlet/commits/"
                + givenBranch.getHead() + ".txt");
        Commit givenCommit = Utils.readObject(givenCommitFile, Commit.class);
        Set<String> givenBlobs = givenCommit.getBlobs().keySet();
        for (String file : Utils.plainFilenamesIn(_CWD.getPath())) {
            if (!currCommit.getBlobs().containsKey(file)
                    && givenBlobs.contains(file)) {
                exitMessage("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            } else if (currCommit.getBlobs().containsKey(file)
                    && givenBlobs.contains(file)) {
                File fromGiven = new File(".gitlet/blobs/"
                        + givenCommit.getBlobs().get(file) + ".txt");
                byte[] fromGivenSer = Utils.readContents(fromGiven);
                Utils.writeContents(new File(_CWD.getPath(), file),
                        fromGivenSer);
            } else if (!givenBlobs.contains(file)) {
                Utils.restrictedDelete(new File(_CWD.getPath(), file));
            }
        }
        for (String addFile : givenBlobs) {
            if (!new File(_CWD.getPath(), addFile).exists()) {
                File addToCWD = new File(".gitlet/blobs/"
                        + givenCommit.getBlobs().get(addFile) + ".txt");
                File added = new File(_CWD.getPath(), addFile);
                String addContents = Utils.readContentsAsString(addToCWD);
                Utils.writeContents(added, addContents);
            }
        }
        File headTracker = new File(".gitlet/branch/head.txt");
        Utils.writeContents(headTracker, branchName);
        headBranch = branchName;

        stage.clearAddingArea();
        stage.clearRemovalArea();
        File stageFile = new File(".gitlet/staging/stage.txt");
        byte[] stageSer = Utils.serialize(stage);
        Utils.writeContents(stageFile, stageSer);
    }


    public void checkout(String... args) {
        if (args.length == 3) {
            checkoutFile(args);
        } else if (args.length == 4) {
            checkoutCommit(args);
        } else if (args.length == 2) {
            checkoutBranch(args);
        }
    }
    public void log() {
        Commit currCommit = currentCommit();
        while (currCommit != null) {
            System.out.println(currCommit.log());
            if (currCommit.getParent() != null) {
                File prevCommit = new File(".gitlet/commits/"
                        + currCommit.getParent() + ".txt");
                currCommit = Utils.readObject(prevCommit, Commit.class);
            } else {
                return;
            }
        }
    }

    public void globalLog() {
        String commitDir = ".gitlet/commits/";
        List<String> commitList = Utils.plainFilenamesIn(
                                new File(".gitlet/commits"));
        for (String commit : commitList) {
            File commitFile = new File(commitDir + commit);
            Commit currCommit = Utils.readObject(commitFile, Commit.class);
            System.out.println(currCommit.log());
        }
    }

    public void gitRemove(String... args) {
        String fileName = args[1];
        Commit currCommit = currentCommit();
        if (!stage.getAddingArea().containsKey(fileName)
                && !currCommit.getBlobs().containsKey(fileName)) {
            exitMessage("No reason to remove the file.");
        }
        if (stage.getAddingArea().containsKey(fileName)) {
            stage.getAddingArea().remove(fileName);
        }
        if (currCommit.getBlobs().containsKey(fileName)) {
            stage.getRemovalArea().add(fileName);
            if (new File(_CWD.getPath(), fileName).exists()) {
                Utils.restrictedDelete(new File(_CWD.getPath(), fileName));
            }
        }
        byte[] stageSer = Utils.serialize(stage);
        File stageFile = new File(".gitlet/staging/stage.txt");
        Utils.writeContents(stageFile, stageSer);
    }

    public void status() {
        if (!GITLETFOLDER.exists()) {
            exitMessage("Not in an initialized Gitlet directory.");
        }
        System.out.println("=== Branches ===");
        List<String> branchList = Utils.plainFilenamesIn(".gitlet/branch");
        for (String branchName : branchList) {
            if (!branchName.equals("head.txt")) {
                File currBranchFile = new File(".gitlet/branch/"
                        + branchName);
                Branch currBranch = Utils.readObject(currBranchFile,
                        Branch.class);
                if (currBranch.getName().equals(headBranch)) {
                    System.out.println("*" + currBranch.getName());
                } else {
                    System.out.println(currBranch.getName());
                }
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Set<String> stagedFiles = stage.getAddingArea().keySet();
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removedFiles = stage.getRemovalArea();
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }



    public void find(String... args) {
        String message = args[1];
        String commitDir = ".gitlet/commits/";
        List<String> commitList = Utils.plainFilenamesIn(
                new File(".gitlet/commits"));
        boolean exists = false;
        for (String commit : commitList) {
            File commitFile = new File(commitDir + commit);
            Commit currCommit = Utils.readObject(commitFile, Commit.class);
            if (currCommit.getMsg().equals(message)) {
                System.out.println(currCommit.getID());
                exists = true;
            }
        }
        if (!exists) {
            exitMessage("Found no commit with that message.");
        }
    }

    public void branch(String... args) {
        String branchName = args[1];
        if (new File(".gitlet/branch/"
                + branchName + ".txt").exists()) {
            exitMessage("A branch with that name already exists.");
        }
        Commit currCommit = currentCommit();
        Branch newBranch = new Branch(branchName, currCommit.getID());
        byte[] branchSer = Utils.serialize(newBranch);
        File branchFile = new File(".gitlet/branch/"
                + newBranch.getName() + ".txt");
        Utils.writeContents(branchFile, branchSer);
    }

    public void removeBranch(String... args) {
        String branchName = args[1];
        File thisBranchFile = new File(".gitlet/branch/"
                + branchName + ".txt");
        if (!thisBranchFile.exists()) {
            exitMessage("A branch with that name does not exist.");
        } else if (branchName.equals(headBranch)) {
            exitMessage("Cannot remove the current branch.");
        } else {
            thisBranchFile.delete();
        }
    }

    public void reset(String... args) {
        String commitID = args[1];
        File givenCommitFile = new File(".gitlet/commits/"
                + commitID + ".txt");
        if (!givenCommitFile.exists()) {
            exitMessage("No commit with that id exists.");
        }
        Commit currCommit = currentCommit();
        Commit givenCommit = Utils.readObject(givenCommitFile, Commit.class);
        for (String file : Utils.plainFilenamesIn(_CWD.getPath())) {
            if (!currCommit.getBlobs().containsKey(file)
                    && givenCommit.getBlobs().containsKey(file)) {
                exitMessage("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            } else if (currCommit.getBlobs().containsKey(file)
                    && givenCommit.getBlobs().containsKey(file)) {
                File fromGiven = new File(".gitlet/blobs/"
                        + givenCommit.getBlobs().get(file) + ".txt");
                byte[] givenBlobSer = Utils.readContents(fromGiven);
                Utils.writeContents(new File(_CWD.getPath(), file),
                        givenBlobSer);
            } else if (!givenCommit.getBlobs().containsKey(file)) {
                Utils.restrictedDelete(new File(_CWD.getPath(), file));
            }
        }
        for (String addFile : givenCommit.getBlobs().keySet()) {
            if (!new File(_CWD.getPath(), addFile).exists()) {
                File addingFile = new File(".gitlet/blobs/"
                        + givenCommit.getBlobs().get(addFile) + ".txt");
                File addTo = new File(_CWD.getPath(), addFile);
                byte[] addingSer = Utils.readContents(addingFile);
                Utils.writeContents(addTo, addingSer);
            }
        }
        File headBranchFile = new File(".gitlet/branch/"
                + headBranch + ".txt");
        Branch head = new Branch(headBranch, commitID);
        byte[] headSer = Utils.serialize(head);
        Utils.writeContents(headBranchFile, headSer);


        stage.clearAddingArea();
        stage.clearRemovalArea();
        byte[] stageSer = Utils.serialize(stage);
        Utils.writeContents(new File(".gitlet/staging/stage.txt"),
                stageSer);
    }

    public void merge(String... args) {
        isMerge = true;
        String branchName = args[1];
        File givenBranchFile = new File(".gitlet/branch/"
                + branchName + ".txt");
        if (!stage.getAddingArea().isEmpty()
                | !stage.getRemovalArea().isEmpty()) {
            exitMessage("You have uncommitted changes.");
        } else if (!givenBranchFile.exists()) {
            exitMessage("A branch with that name does not exist.");
        } else if (branchName.equals(headBranch)) {
            exitMessage("Cannot merge a branch with itself.");
        }
        Commit currCommit = currentCommit();
        Branch otherBranch = Utils.readObject(givenBranchFile, Branch.class);
        Commit otherCommit = Utils.readObject(new File(
                        ".gitlet/commits/" + otherBranch.getHead()
                                + ".txt"), Commit.class);
        mergeID = otherCommit.getID();
        Commit split = splitFinder(currCommit, otherCommit);
        if (split.getID().equals(currCommit.getID())) {
            String[] fastFor = {"checkout", branchName};
            String oldHead = headBranch;
            checkout(fastFor);
            headBranch = oldHead;
            exitMessage("Current branch fast-forwarded.");
        } else if (split.getID().equals(otherCommit.getID())) {
            exitMessage("Given branch is an ancestor of "
                    + "the current branch.");
        }
        for (String file : Utils.plainFilenamesIn(_CWD.getPath())) {
            if (currCommit.getBlobs().get(file) == null
                    && otherCommit.getBlobs().get(file) != null) {
                exitMessage("There is an untracked file in the "
                    + "way; delete it, or add and commit it first.");
            }
        }
        for (String file : currCommit.getBlobs().keySet()) {
            mergeChecker1(split, currCommit, otherCommit, file, branchName);
        }
        for (String file : otherCommit.getBlobs().keySet()) {
            if (split.getBlobs().get(file) == null
                    && currCommit.getBlobs().get(file) == null) {
                checkout("checkout", otherCommit.getID(),
                        "--", file);
                stage.toAdd(file, otherCommit.getBlobs().get(file));
            }
        }
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        gitCommit("Merged "  + branchName
                + " into " + headBranch + ". ");
        hasConflict = false;
        isMerge = false;
        mergeID = null;
    }

    public void mergeChecker1(Commit split, Commit current, Commit given,
                              String file,
                              String branchName) {
        String splitBlob = split.getBlobs().get(file);
        String currBlob = current.getBlobs().get(file);
        String givenBlob = given.getBlobs().get(file);
        File givenBranchFile = new File(".gitlet/branch/"
                + branchName + ".txt");
        Branch otherBranch = Utils.readObject(givenBranchFile, Branch.class);
        Commit otherCommit = Utils.readObject(new File(
                ".gitlet/commits/" + otherBranch.getHead()
                        + ".txt"), Commit.class);
        if (splitBlob != null && givenBlob != null) {
            if (splitBlob.equals(currBlob)
                    && !currBlob.equals(givenBlob)) {
                checkout("checkout", otherCommit.getID(),
                        "--", file);
                stage.toAdd(file, givenBlob);
            } else if (!currBlob.equals(givenBlob)
                    && !givenBlob.equals(splitBlob)) {
                hasConflict = true;
                conflict(file, currBlob, givenBlob);
            }
        } else if (splitBlob != null
                && !splitBlob.equals(currBlob)
                && givenBlob == null) {
            hasConflict = true;
            conflict(file, currBlob, givenBlob);
        } else if (splitBlob != null
                && givenBlob == null) {
            gitRemove("rm", file);
        } else if (!currBlob.equals(givenBlob)
                && givenBlob != null) {
            hasConflict = true;
            conflict(file, currBlob, givenBlob);
        }
    }

    public void conflict(String fileName, String headBlob, String otherBlob) {
        if (headBlob == null) {
            File confFile = new File(_CWD.getPath(), fileName);
            String builder = "<<<<<<< HEAD\n" + "=======";
            File blobCont = new File(".gitlet/blobs/"
                    + otherBlob + ".txt");
            builder = builder + Utils.readContentsAsString(blobCont);
            builder = builder + ">>>>>>>\n";
            Utils.writeContents(confFile, builder);
            byte[] build = Utils.readContents(confFile);
            String sha1 = Utils.sha1(build);
            stage.toAdd(fileName, sha1);
        } else if (otherBlob == null) {
            File confFile = new File(_CWD.getPath(), fileName);
            String builder = "<<<<<<< HEAD\n";
            File blobCont = new File(".gitlet/blobs/"
                    + headBlob + ".txt");
            builder = builder + Utils.readContentsAsString(blobCont)
                    + "=======\n";
            builder = builder + ">>>>>>>\n";
            Utils.writeContents(confFile, builder);
            byte[] build = Utils.readContents(confFile);
            String sha1 = Utils.sha1(build);
            stage.toAdd(fileName, sha1);
        } else {
            File confFile = new File(_CWD.getPath(), fileName);
            String builder = "<<<<<<< HEAD\n";
            File blobCont = new File(".gitlet/blobs/"
                    + headBlob + ".txt");
            builder = builder + Utils.readContentsAsString(blobCont)
                    + "=======\n";
            File blobCont2 = new File(".gitlet/blobs/"
                    + otherBlob + ".txt");
            builder = builder + Utils.readContentsAsString(blobCont2);
            builder = builder + ">>>>>>>\n";
            Utils.writeContents(confFile, builder);
            byte[] build = Utils.readContents(confFile);
            String sha1 = Utils.sha1(build);
            stage.toAdd(fileName, sha1);
        }

    }


    private Commit splitFinder(Commit headCom, Commit givenCom) {
        HashMap<String, String> givenCommits = branchCommits(givenCom);
        ArrayDeque<Commit> bfsHead = new ArrayDeque<>();
        bfsHead.push(headCom);
        while (!bfsHead.isEmpty()) {
            Commit current = bfsHead.poll();
            if (givenCommits.get(current.getID()) != null) {
                return current;
            } else {
                File parent1 = new File(".gitlet/commits/"
                        + current.getParent() + ".txt");
                Commit child1 = Utils.readObject(parent1, Commit.class);
                bfsHead.addLast(child1);
                if (current.isHasMerge()) {
                    File parent2 = new File(".gitlet/commits/"
                            + current.getMergePoint() + ".txt");
                    Commit child2 = Utils.readObject(parent2, Commit.class);
                    bfsHead.addLast(child2);
                }
            }
        }

        return null;
    }

    public HashMap<String, String> branchCommits(Commit headCom) {
        HashMap<String, String> commitList = new HashMap<>();
        ArrayDeque<Commit> bfsQueue = new ArrayDeque<>();
        bfsQueue.push(headCom);
        while (!bfsQueue.isEmpty()) {
            Commit current = bfsQueue.poll();
            commitList.put(current.getID(), "present");
            if (current.getParent() != null) {
                File parentFile = new File(".gitlet/commits/"
                        + current.getParent() + ".txt");
                Commit child1 = Utils.readObject(parentFile, Commit.class);
                bfsQueue.addLast(child1);
            }
            if (current.isHasMerge()) {
                File parentFile2 = new File(".gitlet/commits/"
                        + current.getMergePoint() + ".txt");
                Commit child2 = Utils.readObject(parentFile2, Commit.class);
                bfsQueue.addLast(child2);
            }
        }
        return commitList;
    }


    public Commit currentCommit() {
        File currBranch = new File(".gitlet/branch/"
                + headBranch + ".txt");
        Branch branch = Utils.readObject(currBranch, Branch.class);
        File commit = new File(".gitlet/commits/"
                + branch.getHead() + ".txt");
        return Utils.readObject(commit, Commit.class);
    }

    public void exitMessage(String msg) {
        if (msg != null && msg != "") {
            System.out.println(msg);
        }
        System.exit(0);
    }
}
