package nl.shashi.playground.rest.large.files.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class AtomicFileMover {

    public Path fileMove(File source, File destination) throws IOException {
        return Files.move(source.toPath(), destination.toPath(), ATOMIC_MOVE);
    }

    public boolean createFolder(File fileName) {
        return fileName.mkdirs();
    }
}
