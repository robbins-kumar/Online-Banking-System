#!/bin/sh
mkdir -p out
javac -d out $(find src -name "*.java") && java -cp "out:lib/*" bank.Main
