package org.pwss;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

/**
 * The FileNavigatorImpl class provides an implementation of the FileNavigator
 * interface.
 * It allows traversing files in a non-recursive, multithreaded manner by
 * accepting
 * different types of directory inputs (String, Path, or File). This class is
 * designed
 * to handle large directory structures efficiently while providing detailed
 * logging.
 */
public final class FileNavigatorImpl implements FileNavigator {

    private final org.slf4j.Logger log;

    private final int THREAD_POOL_SIZE = 5;

    private ExecutorService fileTraverseSingleExecutorReference;

    private ExecutorService executorDirectoriesReference;

    private ExecutorService easyFileTraverseSingleExecutorReference;

    private final Path startPath;

    /**
     * Constructs a FileNavigatorImpl object using the specified directory path as a
     * String.
     *
     * @param directoryPath The path of the directory to be traversed, represented
     *                      as a string.
     */
    public FileNavigatorImpl(final String directoryPath) {
        this.log = LoggerFactory.getLogger(FileNavigatorImpl.class);
        this.startPath = Paths.get(directoryPath);
    }

    /**
     * Constructs a FileNavigatorImpl object using the specified directory path.
     *
     * @param directory The directory to be traversed, represented as a
     *                  java.nio.file.Path object.
     */
    public FileNavigatorImpl(final Path directory) {
        this.log = LoggerFactory.getLogger(FileNavigatorImpl.class);
        this.startPath = directory;
    }

    /**
     * Constructs a FileNavigatorImpl object using the specified file directory.
     *
     * @param directory The directory to be traversed, represented as a java.io.File
     *                  object.
     */
    public FileNavigatorImpl(final File directory) {
        this.log = LoggerFactory.getLogger(FileNavigatorImpl.class);
        this.startPath = directory.toPath();
    }

    @Override
   public final Future<List<Future<List<Path>>>> traverseFiles() throws IOException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    ExecutorService executorOneThread = Executors.newSingleThreadExecutor();
    final List<Future<List<Path>>> futures = new ArrayList<>();

    Future<List<Future<List<Path>>>> listFuture = executorOneThread.submit(new Callable<List<Future<List<Path>>>>() {

        @Override
        public List<Future<List<Path>>> call() {
            try {
                final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {

                        if(!Files.isDirectory(file))
                        return FileVisitResult.SKIP_SIBLINGS;
                        else 
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
throws IOException {
                        try {
                            if (true) {
                                futures.add(executor.submit(() -> {
                                     List<Path> visitedPaths = new ArrayList<>();
                                    try (Stream<Path> stream = Files.walk(dir)) {
                                        
                                        visitedPaths = stream.filter(a -> !a.startsWith(".")).toList();
                                          
                                    } catch (final IOException e) {
                                        log.error("Error traversing directory: {} - {}", dir, e.getMessage());
                                    }
                                    return visitedPaths;
                                }));
                            }
                        } catch (SecurityException se) {
                            // Skip directories that we can't access
                            log.debug("SecurityException while traversing files: {)",se.getMessage());
                           
                            return FileVisitResult.SKIP_SIBLINGS;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
                                throws IOException {
                        log.error("Error accessing file: {} - {}", file, exc.getMessage());
                        // Continue traversing even if a file access fails
                        return FileVisitResult.CONTINUE;
                    }
                };
             Files.walkFileTree(startPath, visitor);
              

            } catch (final AccessDeniedException e) {
                log.error("Access Denied: {} - {}", startPath, e.getMessage());

            } catch (final NoSuchFileException e) {
                log.error("File Not Found: {} - {}", startPath, e.getMessage());
            } catch (final IOException e) {
                log.error("IO Exception: {} - {}", startPath, e.getMessage());
            } finally {
                executorDirectoriesReference = executor;
                fileTraverseSingleExecutorReference = executorOneThread;
                
            }

            return futures;
        }
    });

    return listFuture;
}

    @Override
    public final Future<List<Path>> traverseFilesEasy()
            throws IOException, InterruptedException, ExecutionException {

        ExecutorService executorOneThread = Executors.newSingleThreadExecutor();
        easyFileTraverseSingleExecutorReference = executorOneThread;

        Future<List<Path>> lisFuture2 = executorOneThread.submit(new Callable<List<Path>>() {
            @Override
            public List<Path> call() throws Exception {
                // Get all futures from the original traversal method
                Future<List<Future<List<Path>>>> futures1 = traverseFiles();
                return  futures1.get().get(0).get();
            }
        });

        return lisFuture2;
    }

    @Override
    public final boolean shutdownDirectoryNavThreadPool() {

        Boolean result = null;
        Boolean result2 = null;
        if (executorDirectoriesReference != null) {
            executorDirectoriesReference.shutdownNow();
            try {
                // This will block until all tasks have completed execution
                result = executorDirectoriesReference.awaitTermination(60, TimeUnit.SECONDS);
                if (!result) {
                    log.warn("Executor did not terminate within the expected time frame.");
                }

                if(fileTraverseSingleExecutorReference != null){
                    fileTraverseSingleExecutorReference.shutdownNow();
                   result2 = fileTraverseSingleExecutorReference.awaitTermination(60, TimeUnit.SECONDS);
                     if (!result2) {
                    log.warn("Executor did not terminate within the expected time frame.");
                }
                }

            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }

            finally{
                executorDirectoriesReference.close();
                fileTraverseSingleExecutorReference.close();
                log.debug("Closed executorDirectoriesReference");
                 log.debug("Closed fileTraverseSingleExecutorReference");
            }

        }

        if (result == null)
            return true;
        else
            return result;
    }

    @Override
    public final boolean shutdownEasyFileTraverserThread() {

        Boolean invokedMethodResult = shutdownDirectoryNavThreadPool();

        Boolean result = null;
        if (easyFileTraverseSingleExecutorReference != null) {
            easyFileTraverseSingleExecutorReference.shutdownNow();
          
            try {
                // This will block until all tasks have completed execution
                result = easyFileTraverseSingleExecutorReference.awaitTermination(60, TimeUnit.SECONDS);
                if (!result) {
                    log.warn("Executor did not terminate within the expected time frame.");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
            finally{
                log.debug("Closed easyFileTraverseSingleExecutorReference");
                  easyFileTraverseSingleExecutorReference.close();
            }

        }

        if (result == null && invokedMethodResult == null)
            return true;
        else if (invokedMethodResult == null)
            return false;
        else if (result == null)
            return false;
        else
            return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((log == null) ? 0 : log.hashCode());
        result = prime * result + ((startPath == null) ? 0 : startPath.hashCode());
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
        final FileNavigatorImpl other = (FileNavigatorImpl) obj;
        if (log == null) {
            if (other.log != null)
                return false;
        } else if (!log.equals(other.log))
            return false;
        if (startPath == null) {
            if (other.startPath != null)
                return false;
        } else if (!startPath.equals(other.startPath))
            return false;
        return true;
    }

}
