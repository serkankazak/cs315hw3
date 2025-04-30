# su / sudo
# apt-get install git make openjdk-7-jdk

all: check clean
	javac hw3.java
	echo "It is possible to run again like -> java hw3"
	java hw3

check: hw3.java
	which javac
	which java

clean:
	rm -f hw3.class
	rm -f searcher.class
