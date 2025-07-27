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
    public final List<Future<List<Path>>> traverseFiles() throws IOException, InterruptedException {

        final List<Future<List<Path>>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {

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
                    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
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

                if (executor != null) {
                    executor.shutdown();
                    try {
                        // This will block until all tasks have completed execution
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            log.warn("Executor did not terminate within the expected time frame.");
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                        Thread.currentThread().interrupt(); // Preserve interrupt status
                    }
                }
            }

            return futures;
        }
    }

    public final Future<List<Path>> traverseFilesEasy() throws IOException {
        List<Future<List<Path>>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {

            try {
                FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (Files.isDirectory(dir) && Files.isReadable(dir)) {
                            futures.add(executor.submit(() -> {
                                List<Path> visitedPaths = new ArrayList<>();
                                try (Stream<Path> stream = Files.walk(dir)) {
                                    stream.filter(Files::isRegularFile)
                                            .forEach(visitedPaths::add);
                                } catch (IOException e) {
                                    log.error("Error traversing directory: {} - {}", dir, e.getMessage());
                                }
                                return visitedPaths;
                            }));
                        }
                        return FileVisitResult.SKIP_SIBLINGS;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        log.error("Error accessing file: {} - {}", file, exc.getMessage());
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                };

                Files.walkFileTree(startPath, visitor);

            } catch (AccessDeniedException e) {
                log.error("Access Denied: {} - {}", startPath, e.getMessage());
            } catch (NoSuchFileException e) {
                log.error("File Not Found: {} - {}", startPath, e.getMessage());
            } catch (IOException e) {
                log.error("IO Exception: {} - {}", startPath, e.getMessage());
            }

            Future<List<Path>> singleFuture = null;
            try {

                // Collect all paths from futures into a single list
                singleFuture = executor.submit(() -> {
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
                if (executor != null) {
                    executor.shutdown();

                    try {
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            log.warn("Executor did not terminate within the expected time frame.");
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting for executor to shut down: {}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }

            }

            return singleFuture;

        }

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
