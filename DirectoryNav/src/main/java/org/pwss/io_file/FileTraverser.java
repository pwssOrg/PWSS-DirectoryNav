package org.pwss.io_file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The FileTraverser interface provides methods to traverse directories and
 * files asynchronously.
 * It uses Java I/O files for file operations. This interface includes
 * overloaded variants of the
 * traverse method, which ultimately delegate to a single core traversal
 * implementation that takes
 * a File object. Additionally, it provides a method to shut down thread pools
 * and close resources.
 * @apiNote Uses {@link java.io.File} as traversing strategy
 */
public interface FileTraverser {

    /**
     * Asynchronously traverses the directory represented by the given path,
     * returning a list of files within the directory as a Future.
     *
     * @param path The directory path to traverse
     * @return A Future containing a List of File objects representing the contents
     *         of the directory
     */
    Future<List<File>> traverse(final Path path);

    /**
     * Asynchronously traverses the directory represented by the given string path,
     * returning a list of files within the directory as a Future.
     *
     * @param path The directory path (as a String) to traverse
     * @return A Future containing a List of File objects representing the contents
     *         of the directory
     */
    Future<List<File>> traverse(final String path);

    /**
     * Asynchronously traverses the given folder, returning a list of files within
     * the folder as a Future.
     *
     * This is the core method where the traversing logic happens. The other two
     * overloaded variants transform
     * their respective input parameters into a File object and then invoke this
     * method.
     *
     * @param folder The folder to traverse
     * @return A Future containing a List of File objects representing the contents
     *         of the folder
     */
    Future<List<File>> traverse(final File folder);

    /**
     * Shuts down thread pools and closes resources used by this file traverser
     * instance.
     *
     * This method should be called when the traverser is no longer needed to ensure
     * that all threads
     * are properly shut down and resources are released.
     */
    void shutdownThreadPool();

}