#!/bin/bash

DIR=/home/root/chris/mensaupbservice

cd $DIR

sudo -u www-data nohup ./run.sh &> $DIR/server.log &
echo $! > $DIR/server.pid