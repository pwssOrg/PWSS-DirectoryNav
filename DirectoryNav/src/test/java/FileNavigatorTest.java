import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pwss.FileNavigator;
import org.pwss.FileNavigatorImpl;

public class FileNavigatorTest {

    @Test
     void testFileTraversingThroughADirectoryWith13FilesAndDirectories() throws IOException {
        // Given
        final Path myTestPath = Paths.get("pwss_test_directory");
        final int expectedNumberOfFiles = 13;

        List<Path> foundPaths = new ArrayList<>();
        FileNavigator fileNavigator = new FileNavigatorImpl(myTestPath);
        try {
            // When
            Future<List<Future<List<Path>>>> futures = fileNavigator.traverseFiles();

            while (!futures.isDone()) {
                System.out.println("Future 1 is working...");
                Thread.sleep(200);

            }

            System.out.println("Future for test 1 is complete!");
            for (Future<List<Path>> future : futures.get()) {

                List<Path> paths = future.get();
                if (paths != null && !paths.isEmpty()) {
                    foundPaths.addAll(paths);

                    foundPaths.stream().filter(a -> !Files.isDirectory(a)).toList().forEach(p -> System.out.println("PWSS Dir -> " + p.getFileName()));
                    
                }
            }

          

        } catch (InterruptedException e) {
            // Handle expected exceptions
            System.err.println("Error: " + e.getMessage());
            Assertions.fail("Test execution failed due to an exception: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle unexpected exceptions
            System.err.println("Execution error: " + e.getCause().getMessage());
            Assertions.fail("Test execution failed due to an exception: " + e.getCause().getMessage());
        }

        finally {

            // Must be called after all futures have either been retrieved or canceled.
            fileNavigator.shutdownDirectoryNavThreadPool();

        }

        // Then
        int actualNumberOfFiles = foundPaths.size();
        Assertions.assertEquals(expectedNumberOfFiles, actualNumberOfFiles);
    }

    @Test
     void testEasyFileTraversingThroughADirectoryWith13FilesAndDirectories() {
        // Given
        final Path myTestPath = Paths.get("pwss_test_directory");
        final int expectedNumberOfFiles = 13;

        List<Path> foundPaths = new ArrayList<>();
        FileNavigator fileNavigator = new FileNavigatorImpl(myTestPath);

        try {
            // When

            Future<List<Path>> future = fileNavigator.traverseFilesEasy();

            while (!future.isDone()) {

                System.out.println("Future 2 is working...");
                Thread.sleep(200);
            }

            System.out.println("Future for test 2 is complete!");

            future.get();

            List<Path> paths = future.get();
            if (paths != null && !paths.isEmpty()) {
                foundPaths.addAll(paths);
            }

            // Must be called after all futures have either been retrieved or canceled.
            fileNavigator.shutdownEasyFileTraverserThread();

        } catch (IOException | InterruptedException e) {
            // Handle expected exceptions
            System.err.println("Error: " + e.getMessage());
            Assertions.fail("Test execution failed due to an exception: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle unexpected exceptions
            System.err.println("Execution error: " + e.getCause().getMessage());
            Assertions.fail("Test execution failed due to an exception: " + e.getCause().getMessage());
        }

        // Then
        int actualNumberOfFiles = foundPaths.size();
        Assertions.assertEquals(expectedNumberOfFiles, actualNumberOfFiles);
    }

   

    @Test
    void testEquals_ObjectsEqual_Success() {
        // Arrange
        String path = "/some/path";
        FileNavigator fileNav1 = new FileNavigatorImpl(path);
        FileNavigator fileNav2 = new FileNavigatorImpl(path);

        // Act
        boolean result = fileNav1.equals(fileNav2);

        // Assert
        assertTrue(result, "The two objects should be equal.");
    }

    @Test
    void testEquals_ObjectsNotEqual_Success() {
        // Arrange
        String path1 = "/some/path";
        String path2 = "/another/path";
        FileNavigator fileNav1 = new FileNavigatorImpl(path1);
        FileNavigator fileNav2 = new FileNavigatorImpl(path2);

        // Act
        boolean result = fileNav1.equals(fileNav2);

        // Assert
        assertFalse(result, "The two objects should not be equal.");
    }

    @Test
    void testEquals_SameObject_Success() {
        // Arrange
        String path = "/some/path";
        FileNavigator fileNav = new FileNavigatorImpl(path);

        // Act
        boolean result = fileNav.equals(fileNav);

        // Assert
        assertTrue(result, "The object should be equal to itself.");
    }

    
    void testEquals_NullObject_Success() {
        // Arrange
        String path = "/some/path";
        FileNavigator fileNav = new FileNavigatorImpl(path);

        // Act & Assert
        assertFalse(fileNav.equals(null), "An object should not be equal to null.");
    }

    @Test
    void testHashCode_EqualObjects_Success() {
        // Arrange
        String path = "/some/path";
        FileNavigator fileNav1 = new FileNavigatorImpl(path);
        FileNavigator fileNav2 = new FileNavigatorImpl(path);

        // Act & Assert
        assertEquals(fileNav1.hashCode(), fileNav2.hashCode(),
                "The hash codes of equal objects should be the same.");
    }

    @Test
    void testHashCode_DifferentObjects_Success() {
        // Arrange
        String path1 = "/some/path";
        String path2 = "/another/path";
        FileNavigator fileNav1 = new FileNavigatorImpl(path1);
        FileNavigator fileNav2 = new FileNavigatorImpl(path2);

        // Act & Assert
        assertNotEquals(fileNav1.hashCode(), fileNav2.hashCode(),
                "The hash codes of different objects should not be the same.");
    }
}