# Jynx
[Jynx(bird)](https://en.wikipedia.org/wiki/Wryneck)


# JynxFree

This is the base module for Jynx that uses the classfile API.
It requires Java V24.

Usage:

```

  --VERSION display version information

  --HELP display help message

 jynx {options} .jx_file
   (produces a class file from a .jx file)
   (requires module JynxFor)


 tojynx {options}  class-name|class_file > .jx_file
   (produces a .jx file from a class)
   (any JYNX options are added to .version directive)
   (requires module JynxTo)


 roundtrip {options}  class-name|class_file|text_file
   (checks that TOJYNX followed by JYNX produces an equivalent class (according to ASM Textifier))
   (requires module JynxRound)


 structure {options}  class-name|class_file
   (prints a skeleton of class structure)
   (requires module JynxStructure)

```
