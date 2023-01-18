#!/usr/bin/env bash

## $1 = Input APK filename
## $2 = Output APK filename
## $3 = keystore filename

if [ "$1" == ""]; then
    echo "ERROR: make sure to specify input APK and output directory and keystore for signing"
    exit 1
fi
if [ "$2" == ""]; then
    echo "ERROR: make sure to specify input APK and output directory and keystore for signing"
    exit 1
fi
if [ "$3" == ""]; then
    echo "ERROR: make sure to specify input APK and output directory and keystore for signing"
    exit 1
fi

java -jar AndResGuard-cli-1.2.22.jar $1 -config config.xml -out $2 -signatureType v2 -signature $3 testres testres testres

exit 0