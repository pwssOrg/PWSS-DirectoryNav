package org.pwss.io_file;

import java.io.File;
import java.nio.file.Path;

import java.util.List;

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
public final class FileTraverserImpl implements FileTraverser {

    private final ExecutorService executorService;
    private final org.slf4j.Logger log;
    private final int THREAD_POOL_SIZE = 100;

    public FileTraverserImpl() {

        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

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
        log.debug("shutting down executorService...");
        executorService.shutdownNow();

        while (!executorService.isShutdown()) {
        }
        executorService.close();
        log.debug("executorService Closed!");
    }

    @Override
    public final Future<List<File>> traverse(final File folder) {

        if (folder == null) {
            log.info("File is null --- traverse(File folder)");
            return null;
        }
        final Future<List<File>> future = executorService.submit(new FileTraverseWorker(folder));
        log.debug("Returning processing Future (in progress..) . Is done = {}", future.isDone());
        return future;
    }

}