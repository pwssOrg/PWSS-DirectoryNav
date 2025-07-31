package org.pwss.io_file;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.LoggerFactory;

/**
 * A worker class that traverses a file system starting from an initial path.
 * This class implements the {@link Callable} interface to allow for asynchronous
 * processing of files and directories.
 *
 * <p>This class is not intended to be subclassed by clients.</p>
 */
final class FileTraverseWorker implements Callable<List<File>> {

    private final List<File> files;
    private final List<File> directories;

    private final File startPath;

    private final org.slf4j.Logger log;

    /**
     * Constructs a new FileTraverseWorker with the specified starting file.
     *
     * @param file The starting file for traversal.
     */
    protected FileTraverseWorker(File file) {
        files = new LinkedList<>();
        directories = new LinkedList<>();

        this.log = LoggerFactory.getLogger(FileTraverseWorker.class);
        this.startPath = file;
        directories.add(this.startPath);
    }

     /**
     * Constructs a new FileTraverseWorker with the specified starting path.
     *
     * @param startPath The starting path for traversal.
     */
    protected FileTraverseWorker(final Path startPath) {
        files = new LinkedList<>();
        directories = new LinkedList<>();

        this.log = LoggerFactory.getLogger(FileTraverseWorker.class);

        if (startPath != null) {
            this.startPath = startPath.toFile();
            directories.add(this.startPath);
        } else {
            this.startPath = null;
            return;
        }
    }

    /**
     * Constructs a new FileTraverseWorker with the specified starting path string.
     *
     * @param startPath The starting path string for traversal.
     */
    protected FileTraverseWorker(final String startPath) {
        files = new LinkedList<>();
        directories = new LinkedList<>();

        this.log = LoggerFactory.getLogger(FileTraverseWorker.class);

        File file = null;

        try {

            file = new File(startPath);
        }

        catch (final Exception exception) {

            log.error("Could not initialize the input file - {}", exception.getMessage());
        }

        if (file != null) {
            this.startPath = file;
            directories.add(this.startPath);
        } else {
            log.info("File is null --- traverse(String path)");
            this.startPath = null;
            return;
        }

    }

     /**
     * Traverses the files and directories starting from the initial path.
     *
     * <p>This method processes each directory, collecting all files in a list.</p>
     *
     * @return A list of all files found during traversal.
     */
    @Override
    public List<File> call() {
        while (!directories.isEmpty()) {

            if (!directories.isEmpty()) {
                processFiles(directories.getFirst());
            }
        }

        return files;
    }

    /**
     * Processes a directory by adding its files to the list of files and
     * its subdirectories to the queue for further processing.
     *
     * @param folder The directory to be processed.
     */
    private final void processFiles(final File folder) {

        final File[] entries = folder.listFiles();
        if (entries != null) {
            for (final File entry : entries) {

                try {
                    if (entry.isFile()) {
                        files.add(entry);
                    }

                    if (entry.isDirectory() && !entry.equals(folder))
                        directories.add(entry);
                } catch (final Exception e) {
                    log.error("Access error occurred: {}", e.getMessage());

                }

            }

        }
        directories.remove(folder);
    }

}
