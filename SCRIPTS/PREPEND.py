#!/usr/bin/env python

# This will prepend a file specified on the command line to every
# .java file.

# Thomas Finley

import os, string, sys, commands, tempfile

# Get the file.
if (len(sys.argv) < 2):
    print "I need a file name."
    sys.exit(0)
prependFile = sys.argv[1]

# Get a list of all .java files.
command = "find . -name \"*.java\""
files = string.split(commands.getoutput(command))

# What about a temp file?
temp = tempfile.mktemp()

# Find those support files that are newer than the jar, if it exists.
print "I am going to preprend",prependFile

for file in files:
    os.system("cat "+prependFile+" "+file+" > "+temp)
    os.system("cp "+temp+" "+file)
    print "Did",file
