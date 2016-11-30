#!/bin/bash

 mkdir bin

 javac -d bin *.java

 java -cp ./bin restaurant.Restaurant $1 $2