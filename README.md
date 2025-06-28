# Jynx
[Jynx(bird)](https://en.wikipedia.org/wiki/Wryneck)

This is the base module for Jynx that uses the classfile API.
It requires Java V24.

Usage:

```

  --VERSION display version information

  --HELP display help message

 jynx {options} .jx_file
   (produces a class file from a .jx file)


 tojynx {options}  class-name|class_file > .jx_file
   (produces a .jx file from a class)
   (any JYNX options are added to .version directive)


 roundtrip {options}  class-name|class_file|text_file
   (checks that TOJYNX followed by JYNX produces an equivalent class (according to ASM Textifier))


 structure {options}  class-name|class_file
   (prints a skeleton of class structure)

```
