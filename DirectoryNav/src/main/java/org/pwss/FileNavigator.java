package org.pwss;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;


/**
 * The FileNavigator interface provides methods for traversing files in a non-recursive,
 * multithreaded manner. This is particularly useful for handling large directory structures
 * efficiently, preventing stack overflow errors that can occur with recursive traversal.
 */
public interface FileNavigator {

     /**
     * Traverses files in a non-recursive manner using a multithreaded approach.
     * This method is designed to handle large directory structures efficiently,
     * preventing stack overflow errors that can occur with recursive traversal.
     *
     * @return A list of futures, where each future represents the result of
     *         traversing a portion of the file system. Each future contains a list
     *         of file paths (Path objects) found during that portion's traversal.
     * @throws IOException if an I/O error occurs while reading from or writing to
     *                    the file or directory.
     * @throws InterruptedException if the current thread is interrupted while waiting
     *                              for the completion of a subtask.
     */
    List<Future<List<Path>>> traverseFiles() throws IOException, InterruptedException;

}