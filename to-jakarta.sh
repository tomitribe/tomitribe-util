#!/usr/bin/env bash

# java sources - brutal but we only use JAXB annotations so no need to deal with exceptions
find . -type f -name '*.java' -exec sed -i'' -e 's/javax./jakarta./g' {} +
# service loader files
find . -path "*/src/main/resources/META-INF/services/javax*" | sed -e 'p;s/javax/jakarta/g' | xargs -n2 git mv
# docs
find documentation -type f -name '*.md' -exec sed -i'' -e 's/javax./jakarta./g' {} +

mvn -ntp build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0-SNAPSHOT

# Update javax dependencies to use their jakarta equivalent
mvn -ntp versions:update-property -Dproperty=jakarta.xml.bind-api.version -DnewVersion=4.0.0
mvn -ntp versions:update-property -Dproperty=jaxb-runtime.version -DnewVersion=4.0.2