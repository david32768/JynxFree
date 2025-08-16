package com.github.david32768.jynxfree.jynx;

import java.util.EnumSet;
import java.util.function.BiConsumer;

import static com.github.david32768.jynxfree.my.Message.M165;

public enum State {
    
    // if one state refers to another, the method associated with other must support a null directive
    
    END_METHOD(DirectiveConsumer::endMethod),
    CODE(DirectiveConsumer::setCode,END_METHOD),

    END_CLASS(DirectiveConsumer::endClass),
    END_CLASSHDR(DirectiveConsumer::endHeader),
    END_COMPONENT(DirectiveConsumer::endComponent),
    END_FIELD(DirectiveConsumer::endField),
    END_MODULEHDR(DirectiveConsumer::endHeader),
    END_MODULE(DirectiveConsumer::endModule),
    END_START(DirectiveConsumer::defaultVersion), 
    END_PACKAGEHDR(DirectiveConsumer::endHeader),

    COMPONENT_BLOCK(null,END_COMPONENT),
    FIELD_BLOCK(null,END_FIELD),
    METHOD_BLOCK(DirectiveConsumer::setMethod,END_METHOD),
    START_BLOCK(DirectiveConsumer::setStart),

    START(null,END_START),
    CLASSHDR(DirectiveConsumer::setClass, END_CLASSHDR),
    COMPONENT(DirectiveConsumer::setComponent,COMPONENT_BLOCK),
    FIELD(DirectiveConsumer::setField,FIELD_BLOCK),
    MODULEHDR(DirectiveConsumer::setClass, END_MODULEHDR),
    MODULE(DirectiveConsumer::setModule),
    PACKAGEHDR(DirectiveConsumer::setClass, END_PACKAGEHDR),
    COMMON(DirectiveConsumer::setCommon), // state unchanged
    HEADER(DirectiveConsumer::setHeader), // state unchanged
    READ_END(null),    // END directive is read by previous directive in stream
    ;

    private final BiConsumer<DirectiveConsumer,Directive> dirfn;
    private final State next;

    private State(BiConsumer<DirectiveConsumer, Directive> dirfn, State next) {
        this.dirfn = dirfn;
        this.next = next;
    }

    private State(BiConsumer<DirectiveConsumer, Directive> dirfn) {
        this(dirfn,null);
    }

    
    private void changeStateTo(DirectiveConsumer jc, Directive dir) {
        if (dirfn != null) {
            dirfn.accept(jc,dir);
        }
    }

    public State changeToValidState(DirectiveConsumer jc, EnumSet<State> before) {
        if (next == null) {
            // "Directive in wrong place; Current state = %s%n  Expected state was one of %s"
            throw new LogIllegalStateException(M165,this,before);
        }
        // as enum always goes towards top so cannot loop
        assert next.ordinal() < this.ordinal();
        next.changeStateTo(jc,null);
        if (before.contains(next)) {
            return next;
        }
        return next.changeToValidState(jc, before); // recurse up chain of next
    }
    
    public boolean changeToThisState(DirectiveConsumer jc,Directive dir) {
        changeStateTo(jc, dir);
        return switch (this) {
            case HEADER, COMMON -> false;
            default -> true;
        };
    }

    public static State getState(ClassType classtype) {
        return switch (classtype) {
            case MODULE_CLASS -> MODULEHDR;
            case PACKAGE -> PACKAGEHDR;
            default -> CLASSHDR;
        };
    }
    
}
