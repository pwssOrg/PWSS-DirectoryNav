package org.pwss;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The FileNavigator interface provides methods for traversing files in a
 * non-recursive (Java method),
 * multithreaded manner. This is particularly useful for handling large
 * directory structures
 * efficiently, preventing stack overflow errors that can occur with recursive
 * traversal.
 */
public interface FileNavigator {

    /**
     * Traverses files in a non-recursive (Java method) manner using a multithreaded
     * approach.
     * This method is designed to handle large directory structures efficiently,
     * preventing stack overflow errors that can occur with recursive traversal.
     *
     * The method divides the file system traversal into multiple tasks, each
     * handled by a separate thread. Each task returns its results as a Future
     * object.
     * Clients need to process these futures to obtain the complete list of files.
     *
     * This approach allows clients to handle different directories individually,
     * potentially improving performance and responsiveness for large file systems.
     *
     * @return A Future list of futures, where each future represents the result of
     *         traversing a portion of the file system. Each future contains a list
     *         of file paths (Path objects) found during that portion's traversal.
     * @throws IOException          if an I/O error occurs while reading from or
     *                              writing to
     *                              the file or directory.
     * @throws InterruptedException if the current thread is interrupted while
     *                              waiting
     *                              for the completion of a subtask.
     */
    Future<List<Future<List<Path>>>> traverseFiles() throws InterruptedException,IOException;

    /**
     * Traverses files in a non-recursive (Java method) manner using an executor
     * service and a
     * thread pool. This method provides a simpler usage pattern by returning just
     * one future representing the completion of all traversal tasks.
     *
     * While this approach also uses multiple threads via the executor service,
     * it differs from {@link #traverseFiles()} in that clients must wait for the
     * single future to complete before accessing any results. This means the client
     * cannot handle different directories individually but must wait until all
     * directories within the chosen directory have been traversed.
     *
     * @return A future that represents the result of traversing the file system.
     *         The future contains a list of file paths (Path
     *         objects) found during
     *         the traversal.
     * @throws IOException          if an I/O error occurs while reading from or
     *                              writing to
     *                              the file or directory.
     * @throws InterruptedException if the current thread is interrupted while
     *                              waiting
     *                              for the completion of the task.
     */
    Future<List<Path>> traverseFilesEasy() throws IOException, InterruptedException, ExecutionException;

    /**
     * This method should be invoked after all futures have been retrieved from the
     * client
     * following a method invocation of {@link #traverseFiles()}. It is responsible
     * for shutting
     * down the fixed-size thread pool (size 5) used during traversal.
     *
     * @return A boolean that represents the success of shutting down the fixed-size
     *         to 5 thread pool.
     */
    boolean shutdownDirectoryNavThreadPool();

    /**
     * This method should be invoked after all futures have been retrieved from the
     * client
     * following a method invocation of {@link #traverseFilesEasy()}. It is
     * responsible for shutting
     * down both the single thread pool and the fixed-size to 5 thread pool used
     * during traversal.
     *
     * @return A boolean that represents the success of shutting down the single
     *         thread pool and the fixed-size to
     *         5 thread pool.
     */
    boolean shutdownEasyFileTraverserThread();

}