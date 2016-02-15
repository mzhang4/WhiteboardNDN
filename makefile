all: bin
	javac -cp "lib/*:." -d bin ndn/whiteboard/*.java

bin:
	mkdir bin

run:
	sudo nfd-start
	java -cp "bin/:lib/*:." ndn.whiteboard.Sync

clean:
	sudo nfd-stop
	rm -rf bin
