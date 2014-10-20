#!/bin/sh

cd src/Project1; java -cp ../rest:bin:../../WEB-INF/classes socnet1.Server 1 $1 localhost $1; cd ../..
