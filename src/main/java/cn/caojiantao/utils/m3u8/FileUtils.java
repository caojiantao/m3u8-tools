package cn.caojiantao.utils.m3u8;

import java.io.File;
import java.io.IOException;

/**
 * @author caojiantao
 */
public class FileUtils {

    public static boolean newFile(File file, boolean isDirectory) throws IOException {
        if (file.exists()) {
            return deleteFile(file) && newFile(file, isDirectory);
        } else {
            return isDirectory ? file.mkdirs() : file.createNewFile();
        }
    }

    public static boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File item : files) {
                        deleteFile(item);
                    }
                }
            }
            return file.delete();
        }
        return true;
    }
}
