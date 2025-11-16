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
  <version>1.5.2</version>
</dependency>
```

## Basic Usage

### java.io.File

#### Retrieving a Future List of File

```java
import org.pwss.io_file.FileTraverser;
import org.pwss.io_file.FileTraverserImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws IOException,
            ExecutionException,
            InterruptedException {
        FileTraverser fileTraverser = new FileTraverserImpl();

        Future<List<File>> future = fileTraverser
                .traverse("C:\\Users\\PWSS\\" +
                        "Downloads\\ShredChat-master\\ShredChat-master");
        while (!future.isDone()) {

        }
        List<File> fileList = future.get();
        fileList.forEach(f -> System.out.println(f.getAbsolutePath()));
        fileTraverser.shutdownThreadPool();
    }
}

``` 

#### Retrieving a List of files with no subfolders to the selected folder (static)

```java
import org.pwss.util.PWSSDirectoryNavUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws IOException,
            ExecutionException,
            InterruptedException {
        File selectedFolder =
                new File("C:\\Program Files (x86)\\Battle.net");

        List<File> noSubfolderList = PWSSDirectoryNavUtil
                .GetSelectedFolderWithoutSubFolders(selectedFolder);
        noSubfolderList
                .forEach(file -> System.out.println(file.getAbsolutePath()));
    }
}
```
### API Documentation

For detailed information about our classes, methods, and their usage, please visit the Javadoc:

[Link to API Docs](https://pwssorg.github.io/PWSS-DirectoryNav-JavaDocs/)

The Javadocs provide comprehensive documentation for all public APIs in this project, including:
- Class descriptions
- Method details with parameters and return types
- Example usages when available


<img src="https://github.com/pwssOrg/PWSS-DirectoryNav/blob/master/.github/assets/images/Jennifer_Burk_a-desert-road-winds-between-red-rock-formations_640x959.jpg" alt="drawing" width="640" height=959/>
<i>Always traverse quickly and gracefully - only then can you achieve full file integrity
</i>

## Discussion Forum

Please visit our discussion forum for project-related documentation and discussions: [Project Discussion
Forum](https://github.com/orgs/pwssOrg/discussions/categories/pwss-directorynav)

---

© 2025 pwssOrg. All rights reserved.
