package com.github.david32768.jynxfree.jynx;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.ServiceLoader;

public interface MainOptionService {

    MainOption main();

    default boolean call(PrintWriter pw, String[] args) {
        return switch (args.length) {
            case 0 -> call(pw);
            case 1 -> call(pw, args[0]);
            case 2 -> call(pw, args[0], args[1]);
            default -> {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    default boolean call(PrintWriter pw) {
        throw new UnsupportedOperationException();
    }
    
    default boolean call(PrintWriter pw, String arg) {
        throw new UnsupportedOperationException();
    }
    
    default boolean call(PrintWriter pw, String arg1, String arg2) {
        throw new UnsupportedOperationException();
    }
    
    default String callToString(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    default byte[] callFromString(String classname, String code) {
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
