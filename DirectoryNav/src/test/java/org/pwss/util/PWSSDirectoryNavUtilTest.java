package org.pwss.util;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PWSSDirectoryNavUtilTest {
    
    // Given
    final int expectedNumberOfFiles = 3 ; // The files and the directories
    final String startPath = "pwss_test_directory";

    @Test
    public void  testStaticRetrievalOfFiles(){

        final int actual = PWSSDirectoryNavUtil.GetSelectedFolderWithoutSubFolders(new File(startPath)).size();
        Assertions.assertEquals(expectedNumberOfFiles,actual);

    }



    
    
}
