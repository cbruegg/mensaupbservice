#!/bin/bash

DIR=/usr/local/java_apps/mensaupbservice

cd $DIR

sudo -u www-data nohup ./run.sh &> $DIR/server.log &
echo $! > $DIR/server.pid