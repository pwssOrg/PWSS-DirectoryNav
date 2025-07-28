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

    private ExecutorService executorReference;

    private ExecutorService singleExecutorReference;

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
        final List<Future<List<Path>>> futures = new ArrayList<>();

        Future<List<Future<List<Path>>>> lisFuture = executor.submit(new Callable<List<Future<List<Path>>>>() {

            @Override
            public List<Future<List<Path>>> call() throws Exception {

                try {
                    final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                            if (Files.isDirectory(dir) && Files.isReadable(dir)) {
                                futures.add(executor.submit(() -> {
                                    final List<Path> visitedPaths = new ArrayList<>();
                                    try (Stream<Path> stream = Files.walk(dir)) {
                                        stream.filter(Files::isRegularFile)
                                                .forEach(visitedPaths::add);
                                    } catch (final IOException e) {
                                        log.error("Error traversing directory: {} - {}", dir, e.getMessage());
                                    }
                                    return visitedPaths;
                                }));
                            }
                            return FileVisitResult.SKIP_SIBLINGS;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(final Path file, final IOException exc)
                                throws IOException {
                            log.error("Error accessing file: {} - {}", file, exc.getMessage());
                            return FileVisitResult.SKIP_SUBTREE;
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

                    executorReference = executor;
                }

                return futures;
            }

        });

        return lisFuture;

    }

    @Override
    public final Future<Future<List<Path>>> traverseFilesEasy()
            throws IOException, InterruptedException, ExecutionException {

        ExecutorService executorOneThread = Executors.newSingleThreadExecutor();

        Future<Future<List<Path>>> lisFuture2 = executorOneThread.submit(new Callable<Future<List<Path>>>() {

            @Override
            public Future<List<Path>> call() throws Exception {

                Future<List<Future<List<Path>>>> futures1 = traverseFiles();

                List<Future<List<Path>>> futures = futures1.get();

                Future<List<Path>> singleFuture = null;
                try {

                    // Collect all paths from futures into a single list
                    singleFuture = executorOneThread.submit(() -> {
                        List<Path> allPaths = new ArrayList<>();
                        for (Future<List<Path>> future : futures) {
                            try {
                                allPaths.addAll(future.get());
                            } catch (InterruptedException | ExecutionException e) {
                                log.error("Error retrieving paths: {}", e.getMessage());
                            }
                        }
                        return allPaths;
                    });
                } catch (Exception e) {

                    log.error("Error: {}", e.getMessage());
                }

                finally {
                    singleExecutorReference = executorOneThread;
                }

                return singleFuture;

            }
        });
        return lisFuture2;
    }

    @Override
    public final boolean shutdownDirectoryNavThreadPool() {

        Boolean result = null;
        if (executorReference != null) {
            executorReference.shutdown();
            try {
                // This will block until all tasks have completed execution
                result = executorReference.awaitTermination(60, TimeUnit.SECONDS);
                if (!result) {
                    log.warn("Executor did not terminate within the expected time frame.");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                Thread.currentThread().interrupt(); // Preserve interrupt status
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
        if (singleExecutorReference != null) {
            singleExecutorReference.shutdown();
            try {
                // This will block until all tasks have completed execution
                result = singleExecutorReference.awaitTermination(60, TimeUnit.SECONDS);
                if (!result) {
                    log.warn("Executor did not terminate within the expected time frame.");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                Thread.currentThread().interrupt(); // Preserve interrupt status
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
