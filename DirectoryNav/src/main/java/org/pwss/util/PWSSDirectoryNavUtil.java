package org.pwss.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The PWSSDirectoryNavUtil class provides utility methods to work with
 * directories.
 * This class is designed to operate in a stateless manner and only provides
 * static methods for directory
 * navigation.
 */
public record PWSSDirectoryNavUtil() {

    /**
     * Retrieves the contents of the selected folder without including subfolders.
     *
     * @param selectedFolder The folder whose contents need to be retrieved
     * @return A list of File objects representing the immediate contents of the
     *         selected folder
     * @throws NullPointerException if the selected folder is null or its contents
     *                              cannot be listed
     */
    public static List<File> GetSelectedFolderWithoutSubFolders(File selectedFolder) {
        return Arrays.stream(Objects.requireNonNull(selectedFolder.listFiles())).toList();
    }

    /**
     * Processes files in a given directory and returns them as a list.
     *
     * This method iterates through the contents of the specified folder, adding
     * each file or subfolder to a list.
     * If any exception occurs while processing an entry (e.g., due to permission
     * issues), it is caught and
     * ignored.
     * The resulting list includes only those entries that were successfully
     * processed.
     *
     * @param selectedFolder The directory whose contents need to be processed
     * @return A list of File objects representing the immediate contents of the
     *         specified folder
     */
    private static List<File> processFiles(File selectedFolder) {
        ArrayList<File> listOfFiles = new ArrayList<>();
        final File[] entries = selectedFolder.listFiles();
        if (entries != null) {
            for (File entry : entries) {

                try {
                    listOfFiles.add(entry);
                } catch (Exception e) {

                }
            }
        }
        return listOfFiles;
    }
}