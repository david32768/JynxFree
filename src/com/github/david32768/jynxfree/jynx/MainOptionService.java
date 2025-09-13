package com.github.david32768.jynxfree.jynx;

import java.io.PrintWriter;
import java.lang.classfile.ClassModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.github.david32768.jynxfree.my.Message.M219;

public interface MainOptionService {

    MainOption main();

    default boolean call(PrintWriter pw, String[] args) {
        return switch (args.length) {
            case 0 -> call(pw);
            case 1 -> call(pw, args[0]);
            case 2 -> call(pw, args[0], args[1]);
            default -> {
                // "wrong number of parameters after options %s"
                throw new LogUnsupportedOperationException(M219, Arrays.asList(args));
            }
        };
    }
    
    default boolean call(PrintWriter pw) {
        // "wrong number of parameters after options %s"
        throw new LogUnsupportedOperationException(M219, Collections.emptyList());
    }
    
    default boolean call(PrintWriter pw, String arg) {
        // "wrong number of parameters after options %s"
        throw new LogUnsupportedOperationException(M219,List.of(arg));
    }
    
    default boolean call(PrintWriter pw, String arg1, String arg2) {
        var list = List.of(arg1, arg2);
        // "wrong number of parameters after options %s"
        throw new LogUnsupportedOperationException(M219, list);
    }
    
    default String callToString(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    default byte[] callFromString(String classname, String code) {
        throw new UnsupportedOperationException();        
    }
    
    default byte[] callToBytes(ClassModel cm) {
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
