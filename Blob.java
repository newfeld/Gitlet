package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    /**
     * Name of the File attached to the blob.
     */
    private String _filename;
    /**
     * SHA1-ID of the contents.
     */
    private String _id;

    /**
     * Folder where blobs are located.
     */
    private static File blobsfolder = new File(".gitlet/blobs");

    public Blob(String filename) {
        _filename = filename;
        _id = getID();
    }

    public String getFilename() {
        return _filename;
    }

    public String getID() {
        File tempFile = new File(".gitlet/blobs/" + _id + ".txt");
        byte[] contents = Utils.readContents(tempFile);
        return Utils.sha1(contents);
    }

    public static Blob fromFile(String filename) {
        String[] filelist = blobsfolder.list();
        for (String file : filelist) {
            if (file.equals(filename)) {
                String path = ".gitlet/blobs/" + file + ".txt";
                File tempFile = new File(path);
                return Utils.readObject(tempFile, Blob.class);
            }
        }
        throw new IllegalArgumentException();
    }

    public String getContents() {
        File tempFile = new File(".gitlet/blobs/" + _id
                                + ".txt");
        return Utils.readContentsAsString(tempFile);
    }
}
