#!/bin/bash

DIR=/usr/local/java_apps/mensaupbservice

cd $DIR
./gradlew shadowJar
sudo -u www-data nohup "java -jar build/libs/mensaupbservice-1.0-all.jar" &> $DIR/server.log &
echo $! > $DIR/server.pid