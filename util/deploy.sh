#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ]; then
  echo "Uploading Guavaberry snapshot to OSS..."

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true

  echo "Guavaberry snapshot uploaded to OSS."

elif [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Uploading Guavaberry release to OSS..."

  openssl aes-256-cbc -K $encrypted_bbc983d8127a_key -iv $encrypted_bbc983d8127a_iv -in util/pubring.gpg.enc -out util/pubring.gpg -d
  openssl aes-256-cbc -K $encrypted_bbc983d8127a_key -iv $encrypted_bbc983d8127a_iv -in util/secring.gpg.enc -out util/secring.gpg -d

  ls -la util/

  # If branch master deploy a release instead of a snapshot
  sed -ie "s/-SNAPSHOT//" pom.xml

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true -DperformRelease=true -Dgpg.passphrase=${GPG_PASSPHRASE} -Dgpg.keyname=${GPG_KEYNAME}

  echo "Guavaberry release uploaded to OSS."
fi
