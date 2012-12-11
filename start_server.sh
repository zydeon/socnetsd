#!/bin/sh
#osascript -e 'tell application "Terminal" to do script with command "cd /Users/Pedro/Documents/FCTUC/3Ano/1semestre/SD/Pratica/Projecto2/socnet; ./start_server.sh 4444"'	

cd src/Project1; java -cp ../rest:bin:../../WEB-INF/classes socnet1.Server 1 $1 localhost $1; cd ../..
