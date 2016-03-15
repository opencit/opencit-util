#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DnewVersion=${version}"

if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi
(cd maven/mtwilson-maven-bom-coreutil && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/mtwilson-maven-bom-coreutil\" folder" >&2; exit 3; fi
(cd maven/mtwilson-maven-bom-external && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/mtwilson-maven-bom-external\" folder" >&2; exit 3; fi
(cd maven/mtwilson-core-application-zip && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/mtwilson-core-application-zip\" folder" >&2; exit 3; fi
(cd maven/mtwilson-core-feature-zip && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/mtwilson-core-feature-zip\" folder" >&2; exit 3; fi
(cd maven/mtwilson-maven-java && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/mtwilson-maven-java\" folder" >&2; exit 3; fi
