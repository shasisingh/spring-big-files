package nl.shashi.playground.rest.large.files.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class FileHelper {
    private FileHelper() {}

    public static Path createDirectory(String directoryName) throws IOException {
        return Files.createDirectories(Paths.get(directoryName));
    }

    public static boolean exists(final File file) {
        return Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean deleteIfEmpty(File file) {
        if (file.isDirectory() && file.list() != null && Objects.requireNonNull(file.list()).length == 0) {
            return file.delete();
        }
        return false;
    }

}
