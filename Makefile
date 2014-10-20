TOMCAT_HOME = /Library/Tomcat/
WEBAPP_NAME = socnetSD

all:
	javac src/Project1/asciiart/*.java -d WEB-INF/classes
	javac -cp src/rest:WEB-INF/classes:${TOMCAT_HOME}lib/* src/webSocket/*.java src/Project1/socnet1/*.java src/rest/*.java -d WEB-INF/classes
	javac -cp WEB-INF/classes:${TOMCAT_HOME}lib/* src/*.java -d WEB-INF/classes

	# first project
	make -C src/Project1
	./start_server.sh 4444
	
	# debug
	tail -f /Library/Tomcat/logs/catalina.out
