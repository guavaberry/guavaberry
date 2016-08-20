#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ] || \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  # If branch master deploy a release instead of a snapshot
  [ "$TRAVIS_BRANCH" == "master" ] && sed -ie "s/-SNAPSHOT//" pom.xml
  echo "Uploading Guavaberry snapshot to OSS..."

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true -Dgpg.skip=true

  echo "Guavaberry snapshot uploaded to OSS."
fi
