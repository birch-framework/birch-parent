/****************************************************************
 * Copyright (c) 2021 Birch Framework
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ***************************************************************/
class Globals {

   static String version
   static boolean release = false

   static setVersionInfo(String theBranchName) {
      if (theBranchName != null && !theBranchName.isEmpty ()) {
         if (theBranchName =~ 'release/*') {
            def splitVersion = theBranchName.split("/")
            if (splitVersion != null && splitVersion.size () > 0) {
               version = splitVersion[1]
               release = true
            }
            else {
               throw new RuntimeException("Could not infer version number from branch name")
            }
         }
         else {
            version = theBranchName
            release = false
         }
      }
   }
}

node('ubuntu-docker-agents') {
   // Configure GraalVM JDK 11
   env.JAVA_HOME = tool name: 'GraalVM-JDK11' // Tool name from Jenkins configuration

   properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', numToKeepStr: '5'))])

   stage ('Clone') {
      checkout scm
   }

   stage ('Configuration') {
      Globals.setVersionInfo(env.BRANCH_NAME as String)
      withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
         sh "mvn clean -version"
      }
   }

   stage ('Test and Install') {
      withCredentials([string(credentialsId: 'GPG-Passphrase',     variable: 'GPG_PASSPHRASE'),
                       string(credentialsId: 'OAuth2-Test-Secret', variable: 'OAUTH2_SECRET')]) {
         env.OAUTH2_TEST_SECRET = OAUTH2_SECRET
         withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
            sh "mvn install -P bci,ossrh -Dmaven.javadoc.skip=true -Dgpg.passphrase=\"${GPG_PASSPHRASE}\""
         }
      }
   }

   stage ('Quality Analysis') {
      withSonarQubeEnv(installationName: 'SonarCloud (Birch Framework)', envOnly: true) {
         // This expands the evironment variables SONAR_CONFIG_NAME, SONAR_HOST_URL, SONAR_AUTH_TOKEN that can be used by any script.
         withMaven (mavenSettingsConfig: 'Birch-Maven-Settings') {
            sh "mvn sonar:sonar -P bci -Dsonar.branch.name=${env.BRANCH_NAME}"
         }
      }
   }

   stage ('Release') {
      if (Globals.release) {
         withCredentials([string(credentialsId: 'GPG-Passphrase', variable: 'PASSPHRASE')]) {
            withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
               sh "mvn -P bci,ossrh deploy -Dmaven.main.skip=true -Dpmd.skip=true -Dcpd.skip=true -Dfindbugs.skip=true -DskipTests=true -Djacoco.skip=true -Dsonar.skip=true -Dgpg.passphrase=\"${PASSPHRASE}\""
            }
         }
      }
      else {
         echo "${env.BRANCH_NAME} branch does not publish/release artifacts"
      }
   }

   stage ('Site Deploy') {
      if (Globals.release) {
         withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
            sh "mvn -P bci site site:stage scm-publish:publish-scm"
         }
      }
      else {
         echo "${env.BRANCH_NAME} branch does not deploy site"
      }
   }
}