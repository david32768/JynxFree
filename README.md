# Jynx
[Jynx(bird)](https://en.wikipedia.org/wiki/Wryneck)


# JynxFree

This is the base module for Jynx that uses the classfile API.
It requires Java V25.

Usage:

```

  --VERSION display version information

  --HELP display help message

jynx {options} .jx_file
   (produces a class file from a .jx file)

 tojynx {options}  class-name|class_file > .jx_file
   (produces a .jx file from a class)
   (any JYNX options are added to .version directive)

 roundtrip {options}  class-name|class_file|txt-file
   (checks that TOJYNX followed by JYNX produces an equivalent class)
   (according to ASM Textifier)
   (txt-file is a .txt file containing [ {options} [class-name|class-file] ]*)

 structure {options}  class-name|class_file
   (prints a skeleton of class structure)

 compare {options}  class-name|class_file class-name|class_file
   (checks that classes are the same according to ASM Textifier)

 verify {options}  class-name|class_file [.hints-file]?
   (verifies using classfile verifier)
   (if present .hints file only contains a Jynx .hints directive)
   (adds temporary stack map to verify pre Java V1_7 unless contains jsr,ret)

upgrade {options}  class-name|class_file|zip-file|jar-file directory-for-upgraded-class(es)
   (upgrades class to Java Version 7)

```
