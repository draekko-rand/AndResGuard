#!/usr/bin/env bash

java -jar AndResGuard-cli-1.2.22.jar input.apk -config config.xml -out outapk -signatureType v2 -signature release.keystore testres testres testres
