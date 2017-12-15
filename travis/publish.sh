#
# Copyright 2016-2017 The OpenTracing Authors
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.
#

set -euo pipefail
set -x

build_started_by_tag() {
  if [ "${TRAVIS_TAG}" == "" ]; then
    echo "[Publishing] This build was not started by a tag, publishing snapshot"
    return 1
  else
    echo "[Publishing] This build was started by the tag ${TRAVIS_TAG}, publishing release"
    return 0
  fi
}

is_pull_request() {
  if [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then
    echo "[Not Publishing] This is a Pull Request"
    return 0
  else
    echo "[Publishing] This is not a Pull Request"
    return 1
  fi
}

is_travis_branch_master_or_release() {
  if [[ "${TRAVIS_BRANCH}" == "master" || "${TRAVIS_BRANCH}" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "[Publishing] Travis branch is ${TRAVIS_BRANCH}"
    return 0
  else
    echo "[Not Publishing] Travis branch is not master or v0.0.0"
    return 1
  fi
}

check_travis_branch_equals_travis_tag() {
  #Weird comparison comparing branch to tag because when you 'git push --tags'
  #the branch somehow becomes the tag value
  #github issue: https://github.com/travis-ci/travis-ci/issues/1675
  if [ "${TRAVIS_BRANCH}" != "${TRAVIS_TAG}" ]; then
    echo "Travis branch does not equal Travis tag, which it should, bailing out."
    echo "  github issue: https://github.com/travis-ci/travis-ci/issues/1675"
    exit 1
  else
    echo "[Publishing] Branch (${TRAVIS_BRANCH}) same as Tag (${TRAVIS_TAG})"
  fi
}

check_release_tag() {
    tag="${TRAVIS_TAG}"
    if [[ "$tag" =~ ^[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+(\-RC[[:digit:]]+)?$ ]]; then
        echo "Build started by version tag $tag. During the release process tags like this"
        echo "are created by the 'release' Maven plugin. Nothing to do here."
        exit 0
    elif [[ ! "$tag" =~ ^release-[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+(\-RC[[:digit:]]+)?$ ]]; then
        echo "You must specify a tag of the format 'release-0.0.0' or 'release-0.0.0-RC0' to release this project."
        echo "The provided tag ${tag} doesn't match that. Aborting."
        exit 1
    fi
}

is_release_commit() {
  project_version=$(./mvnw help:evaluate -N -Dexpression=project.version|sed -n '/^[0-9]/p')
  if [[ "$project_version" =~ ^[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+(\-RC[[:digit:]]+)?$ ]]; then
    echo "Build started by release commit $project_version. Will synchronize to maven central."
    return 0
  else
    return 1
  fi
}

release_version() {
    echo "${TRAVIS_TAG}" | sed 's/^release-//'
}

safe_checkout_remote_branch() {
  # We need to be on a branch for release:perform to be able to create commits,
  # and we want that branch to be master or v0.0.0 (for RCs). which has been checked before.
  # But we also want to make sure that we build and release exactly the tagged version,
  # so we verify that the remote branch is where our tag is.
  checkoutBranch=master
  if [[ "${TRAVIS_BRANCH}" =~ ^release-[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+\-RC[[:digit:]]+$ ]]; then
    checkoutBranch=v`release_version | sed 's/-RC[[:digit:]]\+//'`
  fi
  git checkout -B "${checkoutBranch}"
  git fetch origin "${checkoutBranch}":origin/"${checkoutBranch}"
  commit_local="$(git show --pretty='format:%H' ${checkoutBranch})"
  commit_remote="$(git show --pretty='format:%H' origin/${checkoutBranch})"
  if [ "$commit_local" != "$commit_remote" ]; then
    echo "${checkoutBranch} on remote 'origin' has commits since the version under release, aborting"
    exit 1
  fi
}

#----------------------
# MAIN
#----------------------

if ! is_pull_request && build_started_by_tag; then
  check_travis_branch_equals_travis_tag
  check_release_tag
fi

./mvnw install -nsu

# If we are on a pull request, our only job is to run tests, which happened above via ./mvnw install
if is_pull_request; then
  true
# If we are on master, we will deploy the latest snapshot or release version
#   - If a release commit fails to deploy for a transient reason, delete the broken version from bintray and click rebuild
elif is_travis_branch_master_or_release; then
  ./mvnw --batch-mode -s ./.settings.xml -Prelease -nsu -DskipTests deploy

  # If the deployment succeeded, sync it to Maven Central. Note: this needs to be done once per project, not module, hence -N
  if is_release_commit; then
    ./mvnw --batch-mode -s ./.settings.xml -nsu -N io.zipkin.centralsync-maven-plugin:centralsync-maven-plugin:sync
  fi

# If we are on a release tag, the following will update any version references and push a version tag for deployment.
elif build_started_by_tag; then
  safe_checkout_remote_branch
  ./mvnw --batch-mode -s ./.settings.xml -Prelease -nsu -DreleaseVersion="$(release_version)" -Darguments="-DskipTests" release:prepare
fi

