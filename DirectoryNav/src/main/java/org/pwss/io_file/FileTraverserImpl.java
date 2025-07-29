package org.pwss.io_file;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

/**
 * The FileTraverseImpl class provides an implementation of the
 * {@link FileTraverser}
 * interface.
 * 
 * @apiNote Uses {@link java.io.File} as traversing strategy
 */
public class FileTraverserImpl implements FileTraverser {

    private final List<File> files;
    private final List<File> directories;
    private final ExecutorService executorService;
    private final ExecutorService processTaskExecutorService;

    private final org.slf4j.Logger log;

    public FileTraverserImpl() {
        files = new LinkedList<>();
        directories = new LinkedList<>();
        executorService = Executors.newSingleThreadExecutor();
        processTaskExecutorService = Executors.newSingleThreadExecutor();

        this.log = LoggerFactory.getLogger(FileTraverserImpl.class);
    }

    @Override
    public final Future<List<File>> traverse(final Path path) {

        if (path != null)
            return traverse(path.toFile());
        else {
            log.info("Path is null --- traverse(Path path)");
            return null;
        }
    }
    @Override
    public final Future<List<File>> traverse(final String path) {

        File file = null;

        try {

            file = new File(path);
        }

        catch (final Exception exception) {

            log.error("Could not initialize the input file - {}", exception.getMessage());
        }

        if (file != null)
            return traverse(file);
        else {
            log.info("File is null --- traverse(String path)");
            return null;
        }

    }
    @Override
    public final void shutdownThreadPool() {
        processTaskExecutorService.shutdownNow();
        executorService.shutdownNow();

        while (!executorService.isShutdown()) {
            log.debug("shutting down executorService...");
        }

        while (!processTaskExecutorService.isShutdown()) {
            log.debug("shutting down processTaskExecutorService...");
        }
        processTaskExecutorService.close();
        log.debug("processTaskExecutorService Closed!");
        executorService.close();
        log.debug("executorService Closed!");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((files == null) ? 0 : files.hashCode());
        result = prime * result + ((directories == null) ? 0 : directories.hashCode());
        result = prime * result + ((executorService == null) ? 0 : executorService.hashCode());
        result = prime * result + ((processTaskExecutorService == null) ? 0 : processTaskExecutorService.hashCode());
        result = prime * result + ((log == null) ? 0 : log.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FileTraverserImpl other = (FileTraverserImpl) obj;
        if (files == null) {
            if (other.files != null)
                return false;
        } else if (!files.equals(other.files))
            return false;
        if (directories == null) {
            if (other.directories != null)
                return false;
        } else if (!directories.equals(other.directories))
            return false;
        if (executorService == null) {
            if (other.executorService != null)
                return false;
        } else if (!executorService.equals(other.executorService))
            return false;
        if (processTaskExecutorService == null) {
            if (other.processTaskExecutorService != null)
                return false;
        } else if (!processTaskExecutorService.equals(other.processTaskExecutorService))
            return false;
        if (log == null) {
            if (other.log != null)
                return false;
        } else if (!log.equals(other.log))
            return false;
        return true;
    }
    @Override
    public final Future<List<File>> traverse(final File folder) {

        if (folder == null) {
            log.info("File is null --- traverse(File folder)");
            return null;
        }

        clearLists();

        directories.add(folder);
        final Future<List<File>> future = executorService.submit(new Callable<List<File>>() {
            @Override
            public List<File> call() {

                while (!directories.isEmpty()) {

                    processTaskExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!directories.isEmpty()) {
                                processFiles(directories.getFirst());
                            }
                        }
                    });

                }
                return files;
            }
        });

        log.debug("Returning processing Future (in progress..) . Is done = {}", future.isDone());
        return future;
    }

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

    private final void clearLists() {

        files.clear();
        directories.clear();
    }

}