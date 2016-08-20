#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ] || \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Uploading Guavaberry snapshot to OSS..."

  mvn clean deploy --settings="util/deploy-settings.xml" -DskipTests=true -Dgpg.skip=true

  echo "Guavaberry snapshot uploaded to OSS."
fi
