# Path to the Java Card Development Kit
JC_HOME=util/java_card_kit-2_2_1

# Version of JCardSim to use;
JCARDSIM=jcardsim-3.0.4-SNAPSHOT

# Beware that only JCardSim-3.0.4-SNAPSHOT.jar includes the classes
# AIDUtil and CardTerminalSimulator, so some of the code samples on
# https://jcardsim.org/docs do not work with older versions
#    JCARDSIM=jcardsim-2.2.1-all
#    JCARDSIM=jcardsim-2.2.2-all

# Classpath for JavaCard code, ie the smartcard applet; this includes
# way more than is probably needed
JC_CLASSPATH=${JC_HOME}/lib/apdutool.jar:${JC_HOME}/lib/apduio.jar:${JC_HOME}/lib/converter.jar:${JC_HOME}/lib/jcwde.jar:${JC_HOME}/lib/scriptgen.jar:${JC_HOME}/lib/offcardverifier.jar:${JC_HOME}/lib/api.jar:${JC_HOME}/lib/installer.jar:${JC_HOME}/lib/capdump.jar:${JC_HOME}/samples/classes:${CLASSPATH}

# be sure to build the applets before the terminal otherwise the terminal can't run
all:  testapplet testterminal

# applet is the card
testapplet: out/test/test.class

out/test/test.class: src/test/test.java
	javac -d out -cp ${JC_CLASSPATH}:src/test src/test/test.java

testterminal: out/terminal/testTerminal.class

out/terminal/testTerminal.class: src/testTerminal/testTerminal.java
	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:out src/testTerminal/testTerminal.java

runtestterminal:
	# Runs the GUI terminal
	java -cp util/jcardsim/${JCARDSIM}.jar:out testTerminal.testTerminal






clean:
	rm -rfv out/*

