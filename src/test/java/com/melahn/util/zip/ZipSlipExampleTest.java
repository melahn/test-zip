package com.melahn.util.zip;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ZipSlipExampleTest {

    private final PrintStream initialOut = System.out;
    private final ByteArrayOutputStream testOut = new ByteArrayOutputStream();
    private final static int UNZIP_OUT_LENGTH = 2048;
    private final static String DEFAULT_TEST_FILENAME = "test.tgz";
    private final static String DIVIDER = "---------------------------";
    @BeforeAll
    public static void init() {
        /**
         * copy the test.tgz file to the project root to prep for the no
         * parms case
         */
        System.out.println(DIVIDER.concat(" TESTS START ").concat(DIVIDER));
        try {
            Files.copy(Paths.get("src/test/resources/test.tgz"), Paths.get(DEFAULT_TEST_FILENAME),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Could not initialize test cases: ".concat(e.getMessage()));
        }
    }

    @AfterAll
    public static void cleanup() {
        /**
         * if the test.tgz file exists in the project directory, then remove it
         */
        try {
            Path testFile = Paths.get(DEFAULT_TEST_FILENAME);
            if (Files.exists(testFile)) {
                Files.delete(testFile);
            }
        } catch (IOException e) {
            System.out.println("Could not cleanup after test cases: ".concat(e.getMessage()));
        }
        System.out.println(DIVIDER.concat(" TESTS END ").concat(DIVIDER));
    }

    /**
     * Test an archive with the Zip Slip Vulnerabiluty
     */
    @Test
    public void unzipTgzWithZipSlipVuln() {
        String[] args = prepForUnzip("src/test/resources/test-chart-file-with-zip-slip-vulnerability.tgz");
        assertThrows(ZipSlipException.class, () -> {
            ZipSlipExample.main(args);
        });
        System.setOut(initialOut);
        System.out.println(String
                .format("SUCCESS: The archive %s was found to have the Zip Slip vulnerability as expected", args[0]));
    }

    /**
     * Test an archive that does not exist
     */
    @Test
    public void unzipTgzNoExist() {
        String[] args = prepForUnzip("notexist.tgz");
        assertThrows(NoSuchFileException.class, () -> {
            ZipSlipExample.main(args);
        });
        System.setOut(initialOut);
        System.out.println(String
                .format(String.format("SUCCESS: A NoSuchFileException was thrown as expected for the non-existent file %s", args[0])));
    }

    /**
     * Test archives that don't have the Zip Slip Vulnerability
     */
    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/test-chart-file-without-embedded-tgz-files.tgz",
            "src/test/resources/test-chart-file-with-embedded-tgz-files.tgz","" })
    void unzipVariant(String archiveFilename) throws IOException {
        Path unzipDir = unzipToPath(archiveFilename);
        System.out.println(String.format("SUCCESS: Parameterized test with %s. Archive was unzipped to %s",
                archiveFilename.isEmpty() ? "src/test/resources/".concat(ZipSlipExample.DEFAULT_TGZ_FILENAME)
                        : archiveFilename,
                unzipDir));
    }

    /**
     * Parses the name of the unzip directory from the input string which contains
     * redirected output from the archive extraction
     * 
     * @param s the outout from the archive extraction
     * @return the Path of the unzip directory
     */
    private Path getUnzipDirectory(String s) {
        String unzipDir = s.substring(s.indexOf("\n") + 1 + "Unzip Target Directory: ".length(),
                s.indexOf("\n", s.indexOf("\n") + 1));
        return Paths.get(unzipDir);
    }

    /**
     * Unzips an archive
     * 
     * @oaram a the name of the archive file to unzip
     * @return a Path to which the archive was unzipped
     */
    private Path unzipToPath(String a) throws IOException {
        Path unzipDir = getUnzipDirectory(unzip(prepForUnzip(a)));
        assertTrue(Files.exists(unzipDir));
        return unzipDir;
    }

    /**
     * Unzip the archive
     * 
     * @param a the archive
     * @return a string containing the first part of the redirected output (enough
     *         to parse the name of the directory to which the archive was unzipped)
     */
    private String unzip(String[] a) throws IOException {
        ZipSlipExample.main(a);
        System.setOut(initialOut);
        return new String(testOut.toByteArray(), 0,
                testOut.size() < UNZIP_OUT_LENGTH ? testOut.size() : UNZIP_OUT_LENGTH);
    }

    /**
     * Redirect standard out to another stream so it cam be inspected to gather
     * information like the name of the directory to which the archive was unzipped
     * and then constructs the arguments to be passed to main
     * 
     * @param a The name of the archive
     * @return a atring array of zero or one element, namely a Path to the archive
     */
    private String[] prepForUnzip(String a) {
        System.setOut(new PrintStream(testOut));
        if (!a.isEmpty()) {
            Path tgzFile = Paths.get(a).toAbsolutePath();
            return new String[] { tgzFile.toString() };
        }
        return new String[] {};
    }
}
