# archive-extract

A project to extract a compressed archive.

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=melahn_test-zip)](https://sonarcloud.io/dashboard?id=melahn_test-zip)

## Overview

Extracting a compressed archive file is well understand but I wanted a project that would test specifically for the
the Zip Slip vulnerability described [here](https://github.com/snyk/zip-slip-vulnerability). It is possible for a naive archive extracter to
unintentionally extract a potentially malicious file without explictly checking for this vulnerability. I also wanted the extractor to protect the user from extracting archives with unusual nesting patterns,  And I wanted it to have 100% Sonar code coverage with no code smells, maintainability issues, security hotspots, etc.

## Usage

### Command Line

To extract a compressed archive, run this command.

     java -jar <compressed archive file name>

### Command Line Examples

#### Safe archives

Benign archives that don't contain any known vulnerabilities are included in the project.  To try one of them out, run this command.

     java -jar target/archive-extract-1.0.0-SNAPSHOT.jar src/test/resources/test.tgz

#### Unsafe Archives

##### Zip Slip Vulnerability

Consider the case where an archive contains a file which, when extracted, would be placed in a directory outside the target directory. This would be
a way for malicious files to be delivered. An example of such an archive can be found [here](./src/test/resources/test-chart-file-with-zip-slip-vulnerability.tgz). This archive contains a file named *evil.txt* waiting to be extracted to the *parent* directory of the extract directory.

This project contains code that extracts a tgz archive, checks for this vulnerabilty and throws an exception when found.

To try it out, run this command.

    java -jar target/archive-extract-1.0.0-SNAPSHOT.jar src/test/resources/test-chart-file-with-zip-slip-vulnerability.tgz

##### Nested Archive

Another example of a potentially unsafe archive is one that contains many levels of nested zip files. An extreme example of that is a [quine](https://research.swtch.com/zip) which would expand infinitely. The extractor
in this project stops extracting when it finds itself more than five levels deep in a nested archive. An example of such an archive can be found [here](./src/test/resources/test-with-depth-six.tgz).

To try it out, run this command.

    java -jar target/archive-extract-1.0.0-SNAPSHOT.jar src/test/resources/test-with-depth-six.tgz

### Java API

     public void extract(String archiveName, Path targetDirectory) throws IOException, IllegalArgumentException

### Java API Example

     public class ArchiveExtractExample {
          public static void main (String args[]) throws java.io.IOException {
               String archive = "src/test/resources/test.tgz";
               java.nio.file.Path targetDirectory = java.nio.file.Files.createTempDirectory("ArchiveExtractExample");
               new com.melahn.util.extract.ArchiveExtract().extract(archive, targetDirectory);
          }
     }
