# Gitlet Design Document
author: Noah Newfeld

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main 
Main driver class handling commands. 
#### Fields
1. File GITLET_FOLDER: Folder containing all files stored by the repo
2. File BLOBS_FOLDER: Folder containing all blobs.
3. File BRANCH_FOLDER: Folder containing the branches.
4. File COMMIT_FOLDER: Folder containing all commits.
5. File STAGING_FOLDER: Folder containing blob references added by ***gitlet add***. 
6. File REMOVEDFILES_FOLDER: Folder containing blob references added by ***gitlet rm***
7. String head: Name of branch that is the current branch. Initialized at "master".
8. String[] COMMANDS: String array of all commands in String format. 
9. 

### Commits
This class defines commits using commit IDs, maps them to the blobs they contain. 

#### Fields
1. Commit parent : Previous commit node
2. String ID: SHA1 of the commit object.
3. Date timestamp: Time and date at which commit was created.
4. String msg: Message corresponding to the commit.
5. Hashmap<String, String> blobs: Track the blobs stored in this commit object. 

#### Data Structures
Using a Hashmap<String, String> to map file name to blob SHA-1 ID for contents.

### Blobs
This class defines a blob object containing content to be stored in commits.

####Fields
1. String filename: Name of the file this blob stores.
2. String ID: SHA ID of the file contents.
3. String content: Serialized content of the Blob

### Branch
This class tracks the branches of our commit history.
#### Fields
1. String name: name of the branch
2. String head: commit ID of the head branch. 
3. 




## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

###Main Class
1. init(): initializes a repository and creates all folders necessary for gitlet to work.
  Creates an initial commit object containing no files, with a single commit message "initial commit".
  The timestamp of this initial commit is 00:00:00 UTC, Thursday, 1 January 1970. 
    Places this commit in the master branch, and points the head(current branch) to this commits ID.
    If there already exists a current working directory, print error message and exit.
2. add(String... args): Adds a copy of the files/blobs corresponding to the file names 
   in args to the STAGING_FOLDER. Checks if the file exists, if the file is already in the staging folder
  and if it is, it overwrites the one in the staging folder with the current version.
3. gitCommit(String msg): Creates a commit object with the given message. Takes the blobs in the STAGING_FOLDER 
   and updates them in the commit object. Also removes blobs from the REMOVEDFILES_FOLDER from the commit. Updates currBranch 
to point to this commit and makes the previous commit the parent commit. This new commit must 
record the timestamp of the commit, SHA-1 of the commit, a HashMap of the blob references, and
the parent commit. 
4. log(): Prints the commit history of the current head branch commit. Displays the commit 
SHA-1, timestamp, and the commit message of each commit in the history. 
5. checkout(String... args): 
   1. (-- [file name]) Takes the information for the file in the head commit and places it in the working 
      directory, overwriting any changes made since the head commit. 
   2. ([commit id] -- [file name]) Takes the information from the commit given by the commit id, and 
      places it in the working directory, overwriting changes made to the file since that commit.
   3. ([branch name]) Takes all the files found in the head commit of the branch given by the branch name
      and places them in the working directory, overwriting any changes made to the files since then. 

###Commits Class
1. Commit(String parent, HashMap<String, String> blobs, String msg): 
   class constructor. 
2. getID(): Retrieves the SHA-1 id of the commit object.
3. getBlobs(): Retrieves the Hashmap of blobs mapping file names to SHA-1 ids of the file.
4. getMsg(): Retrieves the commit message. 
5. getCommitTime(): Retrieves the commit timestamp.
6. getParent(): Retrieves the SHA-1 id of the parent commit.
7. 

###Blobs Class
1. getID(Blob blob): retrieves the ID of the blob corresponding to the file.
2. getContents(Blob blob, String ID?): retrieves the content of the file corresponding to the blob.

###Branch Class
1. getLeft(): Gets the left child of the branch.
2. getRight(): Gets the right child of the branch.
3. branchExists(): checks if this branch object exists, use to check if left or right child 
exists to see if there is a split point or if the next commit should be added as left(single child).
4. 
 


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

