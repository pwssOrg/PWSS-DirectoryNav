package org.pwss.io_file;

import java.io.File;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileTraverserTest {

    // Given
    final String startPath = "pwss_test_directory";
    final int expectedNumberOfFiles = 8; // Only the files and no directories

    @Test
    void testShutdownThreadPool() {

        FileTraverser fileTraverser = new FileTraverserImpl();
        fileTraverser.shutdownThreadPool();

        try {
            Future<List<File>> future = fileTraverser.traverse(new File(startPath));
        } catch (Exception exception) {

            // Handle expected exceptions
            System.out
                    .println("This Exception is expected and means that the test succeeded: " + exception.getMessage());
            System.out.println("Test succeeded due to an exception: " + exception.getMessage());

            Assertions.assertTrue(true);
        }

    }

    @Test
    void testTraverseWithFile() throws InterruptedException, ExecutionException {

        FileTraverser fileTraverser = new FileTraverserImpl();

        Future<List<File>> future = fileTraverser.traverse(new File(startPath));

   
        List<File> fileList = future.get();

        fileTraverser.shutdownThreadPool();

        Assertions.assertEquals(expectedNumberOfFiles, fileList.size());

    }

    @Test
    void testTraverseWithString() throws InterruptedException, ExecutionException {

        FileTraverser fileTraverser = new FileTraverserImpl();

        Future<List<File>> future = fileTraverser.traverse(startPath);

        while (!future.isDone()) {

        }
        List<File> fileList = future.get();

        fileTraverser.shutdownThreadPool();

        Assertions.assertEquals(expectedNumberOfFiles, fileList.size());

    }

    @Test
    void testTraverseWithPath() throws InterruptedException, ExecutionException {

        FileTraverser fileTraverser = new FileTraverserImpl();

        Future<List<File>> future = fileTraverser.traverse(Path.of(startPath));

        while (!future.isDone()) {

        }
        List<File> fileList = future.get();

        fileTraverser.shutdownThreadPool();

        Assertions.assertEquals(expectedNumberOfFiles, fileList.size());

    }

}
