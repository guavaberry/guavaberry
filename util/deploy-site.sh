#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u

mvn clean site-deploy --settings="util/deploy-settings.xml" -DskipTests=true -Dgpg.skip=true

if [ "$TRAVIS_BRANCH" == "dev" ] || \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Publishing Guavaberry site..."


  echo "Guavaberry site published."
fi
