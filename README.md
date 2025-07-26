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
  <version>0.5</version>
</dependency>
```

### Basic Usage
Here is a simple example of how to use the library:

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        Path directoryPath = Paths.get("your/directory/path");
        List<Path> foundPaths = new ArrayList<>();

        try {
            // Traverse files in the given directory
            FileNavigator fileNavigator = new FileNavigatorImpl(directoryPath);
            List<Future<List<Path>>> futures = fileNavigator.traverseFiles();

            for (Future<List<Path>> future : futures) {
                List<Path> paths = future.get();
                if (paths != null && !paths.isEmpty()) {
                    foundPaths.addAll(paths);
                    foundPaths.forEach(p -> System.out.println("PWSS Dir -> "+p.getFileName()));
                }
            }
        } catch (Exception e) {
            // Handle exceptions
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

<img src="https://github.com/pwssOrg/PWSS-DirectoryNav/blob/Readme-for-PWSS-DirectoryNav-72/.github/assets/images/Jennifer_Burk_a-desert-road-winds-between-red-rock-formations_640x959.jpg" alt="drawing" width="640" height=959/>


## Discussion Forum

Please visit our discussion forum for project-related documentation and discussions: [Project Discussion
Forum](https://github.com/orgs/pwssOrg/discussions/categories/pwss-directorynav)

---

© 2025 pwssOrg. All rights reserved.
