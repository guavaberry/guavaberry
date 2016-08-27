#!/bin/bash

# see https://coderwall.com/p/9b_lfq

set -e -u


if [ "$TRAVIS_BRANCH" == "master" ]; then

  # If branch master, deploy a release instead of a snapshot
  sed -ie "s/-SNAPSHOT//" pom.xml

fi


if [ "$TRAVIS_BRANCH" == "dev" ] || \
   [ "$TRAVIS_BRANCH" == "master" ]; then

  echo "Publishing Guavaberry snapshot..."
  mvn clean site-deploy --settings="util/deploy-settings.xml" -DskipTests=true -Dgpg.skip=true
  echo "Guavaberry site published."
fi
