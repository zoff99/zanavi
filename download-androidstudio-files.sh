#!/bin/sh

# get values from git commandline ------------
BRANCH=$(git rev-parse --abbrev-ref HEAD|sed -e 's# #%20#g'|sed -e 'sx#x%23xg'|sed -e 's#/#%2F#g'| grep -v HEAD || git name-rev --name-only HEAD|sed -e 's#^remotes/origin/##'|sed -e 's#^origin/##'|sed -e 's# #%20#g'|sed -e 'sx#x%23xg'|sed -e 's#/#%2F#g')
REPO="zanavi"
REPOUSER=$(git config --get remote.origin.url|cut -d'/' -f 4)
# get values from git commandline ------------

echo $BRANCH
echo $REPO
echo $REPOUSER

wget 'https://circleci.com/api/v1/project/'"$REPOUSER"'/'"$REPO"'/latest/artifacts/0/$CIRCLE_ARTIFACTS/android-studio-project.zip?filter=successful&branch='"$BRANCH" -O ./android-studio-project.zip
unzip -o ./android-studio-project.zip
ls -al ./android-studio-project.zip
rm -f ./android-studio-project.zip
