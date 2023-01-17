#!/usr/bin/env bash

## $1 = APK filename
## $2 = keystore filename

if [ "$1" == ""]; then
    echo "ERROR: make sure to specify APK and keystore for signing"
    exit 1
fi
if [ "$2" == ""]; then
    echo "ERROR: make sure to specify APK and keystore for signing"
    exit 1
fi

java -jar AndResGuard-cli-1.2.22.jar $1 -config config.xml -out outapk -signatureType v2 -signature $2 testres testres testres

exit 0