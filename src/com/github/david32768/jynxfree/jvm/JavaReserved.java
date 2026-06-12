package com.github.david32768.jynxfree.jvm;

import java.util.Arrays;
import java.util.Optional;

import static com.github.david32768.jynxfree.jvm.JavaReserved.WordType.*;

public enum JavaReserved implements JvmVersioned {
    
    // Java reserved words
    ABSTRACT,
    ASSERT(Feature.assertions),
    BOOLEAN,
    BREAK,
    BYTE,
    CASE,
    CATCH,
    CHAR,
    CLASS,
    CONST,
    CONTINUE,
    DEFAULT,
    DO,
    DOUBLE,
    ELSE,
    ENUM(Feature.enums),
    EXTENDS,
    FINAL,
    FINALLY,
    FLOAT,
    FOR,
    GOTO,
    IF,
    IMPLEMENTS,
    IMPORT,
    INSTANCEOF,
    INT,
    INTERFACE,
    LONG,
    NATIVE,
    NEW,
    PACKAGE,
    PRIVATE,
    PROTECTED,
    PUBLIC,
    RETURN,
    SHORT,
    STATIC,
    STRICTFP(Feature.strictfp_rw),
    SUPER(Feature.superflag),
    SWITCH,
    SYNCHRONISED,
    THIS,
    THROW,
    THROWS,
    TRANSIENT,
    TRY,
    VOID,
    VOLATILE,
    WHILE,
    UNDERLINE("_", Feature.underline),
    
    // contextual keywords
    EXPORTS(Feature.modules, CONTEXTUAL),
    MODULE(Feature.modules, CONTEXTUAL),
    NON_SEALED("non-sealed", Feature.sealed, CONTEXTUAL),
    OPEN(Feature.modules, CONTEXTUAL),
    OPENS(Feature.modules, CONTEXTUAL),
    PERMITS(Feature.sealed, NON_TYPEID),
    PROVIDES(Feature.modules, CONTEXTUAL),
    RECORD(Feature.record, NON_TYPEID),
    REQUIRES(Feature.modules, CONTEXTUAL),
    SEALED(Feature.sealed, NON_TYPEID),
    TO(Feature.modules, CONTEXTUAL),
    TRANSITIVE(Feature.modules, CONTEXTUAL),
    USES(Feature.modules, CONTEXTUAL),
    VALUE(Feature.value, CONTEXTUAL),
    VAR(Feature.var_type, NON_TYPEID),
    WHEN(Feature.switch_pattern, CONTEXTUAL),
    WITH(Feature.modules, CONTEXTUAL),
    YIELD(Feature.switch_expression, NON_TYPEID),

    // literal values
    TRUE(LITERAL),
    FALSE(LITERAL),
    NULL(LITERAL),
    ;
    
    private final String word;
    private final Feature feature;
    private final WordType wordType;

    private JavaReserved() {
        this(Feature.unlimited);
    }

    private JavaReserved(Feature feature) {
        this(feature, RESERVED);
    }

    private JavaReserved(WordType wordtype) {
        this(Feature.unlimited, wordtype);
    }
    
    private JavaReserved(String word, Feature feature) {
        this(word, feature, RESERVED);
    }

    private JavaReserved(Feature feature, WordType wordtype) {
        this(Optional.empty(), feature, wordtype);
    }

    private JavaReserved(String word, Feature feature, WordType wordtype) {
        this(Optional.of(word), feature, wordtype);
    }

    private JavaReserved(Optional<String> word, Feature feature, WordType wordtype) {
        this.word = word.orElse(name().toLowerCase());
        this.feature = feature;
        this.wordType = wordtype;
    }

    public Feature feature() {
        return feature;
    }

    public WordType wordType() {
        return wordType;
    }
    
    @Override
    public JvmVersionRange range() {
            return feature.range();
    }
    
    public static Optional<JavaReserved> of(String str, JvmVersion version) {
        return Arrays.stream(values())
                .filter(jr -> jr.word.equals(str))
                .filter(jr -> version.supports(jr.feature))
                .findAny();
    }

    public enum WordType {
        RESERVED,
        CONTEXTUAL,
        NON_TYPEID,
        LITERAL,
        ;
        
    }
}
