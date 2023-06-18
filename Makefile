# Path to the Java Card Development Kit
JC_HOME=util/java_card_kit-2_2_1

# Version of JCardSim to use
JCARDSIM=jcardsim-3.0.4-SNAPSHOT

# Classpath for JavaCard code
JC_CLASSPATH=${JC_HOME}/lib/apdutool.jar:${JC_HOME}/lib/apduio.jar:${JC_HOME}/lib/converter.jar:${JC_HOME}/lib/jcwde.jar:${JC_HOME}/lib/scriptgen.jar:${JC_HOME}/lib/offcardverifier.jar:${JC_HOME}/lib/api.jar:${JC_HOME}/lib/installer.jar:${JC_HOME}/lib/capdump.jar:${JC_HOME}/samples/classes:${CLASSPATH}

# be sure to build the applets before the terminal otherwise the terminal can't run
all:  testapplet testterminal personalizationterminal keyutils runkeyutils posterminal carterminal

# applet is the card
testapplet: out/test/test.class

out/test/test.class: src/test/test.java
	javac -d out -cp ${JC_CLASSPATH}:src/test src/test/test.java

testterminal: out/terminal/testTerminal.class

out/terminal/testTerminal.class: src/testTerminal/testTerminal.java
	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:out src/testTerminal/testTerminal.java

runtestterminal:
	java -cp util/jcardsim/${JCARDSIM}.jar:out testTerminal.testTerminal


personalizationterminal: out/terminal/personalizationTerminal.class

out/terminal/personalizationTerminal.class: src/personalizationTerminal/personalizationTerminal.java
	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:out src/personalizationTerminal/personalizationTerminal.java

runpersonalizationterminal:
	java -cp util/jcardsim/${JCARDSIM}.jar:/home/sara/Desktop/TestJC/lib/BouncyCastle/bcprov-jdk15to18-174.jar:out personalizationTerminal.personalizationTerminal


posterminal: out/terminal/POSTerminal.class

out/terminal/POSTerminal.class: src/POSTerminal/POSTerminal.java
	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:out src/POSTerminal/POSTerminal.java

runposterminal:
	java -cp util/jcardsim/${JCARDSIM}.jar:out POSTerminal.POSTerminal


carterminal: out/terminal/CarTerminal.class

out/terminal/CarTerminal.class: src/CarTerminal/CarTerminal.java
	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:out src/CarTerminal/CarTerminal.java

runcarterminal:
	java -cp util/jcardsim/${JCARDSIM}.jar:out CarTerminal.CarTerminal

keyutils: out/terminal/keyutils.class

out/terminal/keyutils.class: src/KeyUtils/KeyUtils.java

	javac -d out -cp ${JC_HOME}:util/jcardsim/${JCARDSIM}.jar:/home/sara/Desktop/TestJC/lib/BouncyCastle/bcprov-jdk15to18-174.jar:out src/KeyUtils/KeyUtils.java

runkeyutils:
	java -cp util/jcardsim/${JCARDSIM}.jar:/home/sara/Desktop/TestJC/lib/BouncyCastle/bcprov-jdk15to18-174.jar:out KeyUtils.KeyUtils

clean:
	rm -rfv out/*
