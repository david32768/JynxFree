package com.github.david32768.jynxfree.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.my.Message.M116;

import com.github.david32768.jynxfree.jynx.NameDesc;

public class FileIO {

    private FileIO() {}

    public static boolean write(Path dirpath, String classname, byte[] bytes) {
        try {
            Path output = outpath(dirpath, classname, ".class");
            Files.write(output, bytes);
            // "%s created - size %d bytes"
            LOG(M116, output, bytes.length);
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            LOG(ex);
            return false;
        }
   }

    public static boolean write(Path dirpath, String classname, String ext, String str) {
        try {
            Path output = outpath(dirpath, classname, ext);
            Files.writeString(output, str);
            // "%s created - size %d bytes"
            LOG(M116, output, str.length());
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            LOG(ex);
            return false;
        }
   }

    private static Path outpath(Path dirpath, String classname, String ext) throws IOException {
        boolean ok = NameDesc.CLASS_NAME.validate(classname)
                && NameDesc.FILE_EXTENSION.validate(ext);
        if (!ok){
            throw new IllegalArgumentException();
        }
        int lastSlash = classname.lastIndexOf('/');
        if (lastSlash >= 0) {
            String subdirstr = classname.substring(0, lastSlash);
            Path subdir = dirpath.resolve(subdirstr);
            Files.createDirectories(subdir);
        }
        String classfile = classname + ext;
        return dirpath.resolve(classfile);
    }        

}
