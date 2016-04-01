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
changeParentVersionCommand="mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${version}"
mvnInstallCommand="mvn clean install"

(cd maven && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven\" folder" >&2; exit 3; fi
ant ready
if [ $? -ne 0 ]; then echo "Failed to maven install \"maven\" projects" >&2; exit 3; fi
$changeVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi

(cd features  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"features\" folder" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-configuration-settings-ws-v2/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-configuration-settings-ws-v2/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-data-bundle/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-data-bundle/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-feature-inventory/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-feature-inventory/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-help/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-help/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-html5/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-html5/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-html5-login-anon/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-html5-login-anon/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-html5-login-token/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-html5-login-token/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/mtwilson-core-version/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/mtwilson-core-version/feature.xml\"" >&2; exit 3; fi

(cd features-deprecated  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"features-deprecated\" folder" >&2; exit 3; fi
(cd features-linux  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"features-linux\" folder" >&2; exit 3; fi
(cd integration  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"integration\" folder" >&2; exit 3; fi
(cd util  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"util\" folder" >&2; exit 3; fi

(cd packages  && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions in \"packages\" folder" >&2; exit 3; fi
(cd packages  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"packages\" folder" >&2; exit 3; fi

sed -i 's/\-[0-9\.]*\(\-SNAPSHOT\|\(\-\|\.zip$\|\.bin$\|\.jar$\)\)/-'${version}'\2/g' build.targets
if [ $? -ne 0 ]; then echo "Failed to change versions in \"build.targets\" file" >&2; exit 3; fi
