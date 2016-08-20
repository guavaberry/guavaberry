#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

if [ "$TRAVIS_BRANCH" == "dev" ] || \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Publishing Guavaberry site..."

  mvn clean site-deploy --settings="util/deploy-settings.xml" -DskipTests=true -Dgpg.skip=true

  echo "Guavaberry site published."
fi
