package com.github.david32768.jynxfree.jynx;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.ServiceLoader;

public interface MainOptionService {

    MainOption main();

    default boolean call(Optional<String> optfname)  {
        PrintWriter pw = new PrintWriter(System.out);
        boolean result = call(optfname.get(), pw);
        pw.flush();
        return result;
    }

    default boolean call(String fname, PrintWriter pw) {
        throw new UnsupportedOperationException();
    }

    default byte[] assembleString(String classname, String code) {
        throw new UnsupportedOperationException();        
    }
    
    public static Optional<MainOptionService> find(MainOption main) {
        var loader = ServiceLoader.load(MainOptionService.class);
        for (var mainx : loader) {
            if (mainx.main() == main) {
                return Optional.of(mainx);
            }
        }
        return Optional.empty();
    }
}
