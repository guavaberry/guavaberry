#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ]; then
  echo "Uploading Guavaberry snapshot to OSS..."

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true

  echo "Guavaberry snapshot uploaded to OSS."

elif [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Uploading Guavaberry release to OSS..."

  # Check the key files
  ls -la util/

  # If branch master deploy a release instead of a snapshot
  sed -ie "s/-SNAPSHOT//" pom.xml

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true -DperformRelease=true -Dgpg.passphrase=${GPG_PASSPHRASE} -Dgpg.keyname=${GPG_KEYNAME}

  echo "Guavaberry release uploaded to OSS."
fi
