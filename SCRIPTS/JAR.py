#!/usr/bin/env python

# This is a script to build the .jar file.  In order for this to work,
# all the .class files have to be built!  This is useful because it
# only updates those files that need updating (if the .jar file is
# already there), and because it will omit any .java files.  All files
# in the supportDirectories list will be added without question.
# 
# Thomas Finley

import os, string, sys, commands

# What's the name of our manifest file?
manifestFile = "mainFile"
# What's the name of the jar file?
jarFile = "JFLAP.jar"
# Where are non-class files we need?
supportDirectories = ["DOCS", "ICON", "MEDIA"]

# Does a jar file exist?
jarExists = os.access(jarFile, os.F_OK)

# Find those .class files that are newer than the jar, if it exists.
command = "find . -name \"*.class\" "
if jarExists: command += "-newer "+jarFile+" "
files = string.split(commands.getoutput(command))

# Find those support files that are newer than the jar, if it exists.
command = "find "+string.join(supportDirectories)+" -type f "
if jarExists: command += "-newer "+jarFile+" "
files.extend(string.split(commands.getoutput(command)))
# Update the manifest.
print "UPDATING MANIFEST"
if jarExists:
    os.system("jar uvmf "+manifestFile+" "+jarFile)
else:
    os.system("jar cvmf "+manifestFile+" "+jarFile)
# Update the files.
if (len(files)):
    print "UPDATING FILES"
    files = ["'"+file+"'" for file in files]
    os.system("jar uvf "+jarFile+" "+string.join(files))
else:
    print "NO FILES TO ADD"
# Build the index.
print "GENERATING INDEX"
os.system("jar i "+jarFile)
# Yay!
print "JAR BUILT!"
