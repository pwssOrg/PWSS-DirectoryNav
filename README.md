# PWSS-DirectoryNav
[![Makefile CI](https://github.com/pwssOrg/PWSS-DirectoryNav/actions/workflows/build.yml/badge.svg)](https://github.com/pwssOrg/PWSS-DirectoryNav/actions/workflows/build.yml)
[![SCA Scan - PWSS-DirectoryNav](https://github.com/pwssOrg/PWSS-DirectoryNav/actions/workflows/snyk-scan.yml/badge.svg)](https://github.com/pwssOrg/PWSS-DirectoryNav/actions/workflows/snyk-scan.yml)

<i>A powerful and efficient library for traversing directories and subdirectories to retrieve file paths, tailored
for applications like file integrity scanning within the PWSS family. </i>



## Overview
PWSS-DirectoryNav is designed to simplify directory navigation by providing an easy-to-use API that allows you to
traverse through files in a given directory. This makes it ideal for use cases such as file integrity checks and
other directory-related operations within the PWSS ecosystem.

## Features
- Efficient traversal of directories and subdirectories
- Retrieval of file paths with ease
- Suitable for file integrity scanning applications

## Getting Started

### Dependencies
To include this library in your project, add the following dependency to your `pom.xml`:
```xml
<dependency>
  <groupId>lib.pwss</groupId>
  <artifactId>directory_nav</artifactId>
  <version>1.2</version>
</dependency>
```

### Basic Usage
Here is a simple example of how to use the library:

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws IOException {
        Path myTestPath = Paths.get("C:\\Users\\PWSS\\Downloads\\ShredChat-master\\ShredChat-master");
        FileNavigator fileNavigator = new FileNavigatorImpl(myTestPath);
        
        try {
            Future<List<Future<List<Path>>>> futures = fileNavigator.traverseFiles();
            while (!futures.isDone()) {
                System.out.println("Future working :) ...");
            }
            System.out.println("Future  has been good :) !");

            Future<List<Path>> futureListOfPaths = futures.get().get(0);

            // Get Future
            List<Path> pathList = futureListOfPaths.get();

            // Print all folders and directories (including the start folder)
            futureListOfPaths.get().stream()
                    .forEach(element -> System.out.println(element.toFile().getAbsolutePath()));

            /***
             * If you want to print out the absolute paths of the files without any directories
             */

            //futureListOfPaths.get().stream()
            // .filter(a -> !Files.isDirectory(a))
            // .forEach(element -> System.out.println(element.toFile()
            // .getAbsolutePath()));

            /**
             * In case you want to have a list of all paths
             */

            //List<Path> listOfPaths = futureListOfPaths.get().stream().filter(a -> !Files.isDirectory(a)).toList();

            System.out.println("Path Size including all folders and files ( +1 initial / selected folder) -> "
                    + pathList.size());
            System.out.println("Path Size including only Files -> " + pathList.stream()
                    .filter(only_files -> !Files.isDirectory(only_files)).toList().size());
            System.out.println("Path Size including all folders and files (except the initial / selected folder) -> "
                    + (pathList.size() - 1));

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // Must be called after all futures have either been retrieved or canceled.
            fileNavigator.shutdownDirectoryNavThreadPool();
        }
    }
}
```


<img src="https://github.com/pwssOrg/PWSS-DirectoryNav/blob/main/.github/assets/images/Jennifer_Burk_a-desert-road-winds-between-red-rock-formations_640x959.jpg" alt="drawing" width="640" height=959/>
<i>Always traverse quickly and gracefully - only then can you achieve full file integrity
</i>

## Discussion Forum

Please visit our discussion forum for project-related documentation and discussions: [Project Discussion
Forum](https://github.com/orgs/pwssOrg/discussions/categories/pwss-directorynav)

---

© 2025 pwssOrg. All rights reserved.
