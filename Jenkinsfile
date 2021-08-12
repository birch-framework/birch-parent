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

node {
   // Configure GraalVM JDK 11
   jdk = tool name: 'GraalVM-JDK11' // Tool name from Jenkins configuration
   env.JAVA_HOME = "${jdk}"

   properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '30', numToKeepStr: '5'))])

   stage ('Clone') {
      checkout scm
   }

   stage ('Configuration') {
      Globals.setVersionInfo(env.BRANCH_NAME as String)
      withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
         bat "mvn clean -version"
      }
   }

   stage ('Test and Install') {
      if (Globals.release) {
         echo "Release branch detected; Test and Install will occur in the Release stage"
      }
      else {
         withCredentials([string(credentialsId: 'GPG-Passphrase', variable: 'PASSPHRASE')]) {
            withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
               bat "mvn install -Dgpg.passphrase=\"${PASSPHRASE}\" -P ci"
            }
         }
      }
   }

   stage ('Quality Analysis') {
      // TODO
//      withSonarQubeEnv ('SonarQube server') {
//         bat 'mvn sonar:sonar'
//      }
   }

   stage ('Release') {
      if (Globals.release) {
         withCredentials([string(credentialsId: 'GPG-Passphrase', variable: 'PASSPHRASE')]) {
            withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
               bat "mvn deploy -Dgpg.passphrase=\"${PASSPHRASE}\" -P ci"
            }
         }
      }
      else {
         echo "${env.BRANCH_NAME} branch does not publish artifacts"
      }
   }

   stage ('Site Deploy') {
      if (Globals.release) {
         withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
            bat "mvn site-deploy"
         }
      }
      else {
         echo "${env.BRANCH_NAME} branch does not deploy site"
      }
   }
}