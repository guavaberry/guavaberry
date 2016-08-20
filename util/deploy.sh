#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ]; then
  echo "Uploading Guavaberry snapshot to OSS..."

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true

  echo "Guavaberry snapshot uploaded to OSS."

elif [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Uploading Guavaberry release to OSS..."

  openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in util/pubring.gpg.enc -out util/pubring.gpg -d
  openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in util/secring.gpg.enc -out util/secring.gpg -d

  # If branch master deploy a release instead of a snapshot
  sed -ie "s/-SNAPSHOT//" pom.xml

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true -DperformRelease=true

  echo "Guavaberry release uploaded to OSS."
fi
