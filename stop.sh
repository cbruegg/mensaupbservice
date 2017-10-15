#!/bin/bash

DIR=/usr/local/java_apps/mensaupbservice

kill $(cat $DIR/server.pid)
