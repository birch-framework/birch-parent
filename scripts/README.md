# Overview
This folder houses various support scripts for the Birch Framework.

# `release`
The script used to release the current SNAPSHOT version from `master` to a `release/` branch

**Releases can only be executed by Release admins.**

## Prepare local environment
### Git

Create links to the `post-checkout` script so that your new branches get the correct build status badges.
#### Windows
Run the following in the Windows Command Prompt **from the repository root (`birch-parent`)**:

    mklink .git\hooks\post-checkout %cd%\scripts\post-checkout

`git-bash` is required for this script to function properly.  All Git operations must be executed using `git-bash` for Windows.
#### UNIX
Run the following in a shell prompt **from the repository root (`birch-parent`)**:

    ln -s ${PWD}/scripts/post-checkout -t .git/hooks

## Creating the release
In order to create a release:
1. Ensure all local changes are committed and merged into master before running the release, as not doing so will cause the Maven release to fail.
2. Run the following script from the root of the project (`birch-parent`):
   ```shell
   bash scripts/release
   ```
3. Accept all defaults by pressing `Enter` for each prompt, except if creating a release with a minor and/or major version change, then enter the new version number.

   **Do not deviate from default tag naming convention of `birch-parent-<version>` where `<version>` is in the format `<major>.<minor>.<release>`**

4. Release will be committed to `release/<version>` branch and pushed to the repository.  When CI/CD discovers this new release branch, it will publish it to Maven Central.

# `version`
Used to set the version of the project and all modules within a branch.  This script is meant to be used in special circumstances when the version of the framework
needs to be set to a new value, outside the regular cadance of the release cycle.  Must be run **from the repository root (`birch-parent`)**
```shell
bash scripts/version
```