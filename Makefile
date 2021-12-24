SOURCEDIRS = automata file grammar gui regular
SOURCES := `find . -name "*.java"`

all:
	javac *.java
	python SCRIPTS/JAR.py

doc:
	javadoc -d javadoc -overview overview.html -doctitle "JFLAP 4.0" \
		-author \
		-windowtitle "JFLAP 4.0" -breakiterator *.java \
		`find $(SOURCEDIRS) -type d | replace / .`\

clean:
	find . \( -name "*.class" -o -name "*~" -o -name ".DS_Store" \) \
		-a -delete
	rm -rf javadoc/* JFLAP.jar
