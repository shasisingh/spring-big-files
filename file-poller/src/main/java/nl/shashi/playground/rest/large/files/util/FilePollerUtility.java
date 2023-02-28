package nl.shashi.playground.rest.large.files.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static nl.shashi.playground.rest.large.files.util.FileHelper.exists;

@Slf4j
public final class FilePollerUtility {

    private FilePollerUtility() {}


    public static boolean fileExists(final File file) {

        if (!exists(file)) {
            log.warn("[ Memory leakage ] : There is no file present in swiftFileAct folder to check, this event occurred when both files arrived same time and picked up");
            return false;
        }
        return true;
    }




    public static boolean onlyFiles(final File file) {
        return !file.isDirectory() && !file.isHidden() && file.canRead();
    }
}
