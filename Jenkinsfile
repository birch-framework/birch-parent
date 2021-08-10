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

   static final String RELEASE_REPO_LOCAL = 'libs-release-local'
   static final String SNAPSHOT_REPO_LOCAL = 'libs-snapshot-local'

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

   stage ('Site Deploy') {
      withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
         bat "mvn site:site site:deploy"
      }
//      if (Globals.release) {
//         // TODO publish to Maven Central
//      }
//      else {
//         echo "${env.BRANCH_NAME} branch does not deploy site"
//      }
   }

   stage ('Test and Install') {
      withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
         bat "mvn install"
      }
   }

   stage ('Quality Analysis') {
//      withSonarQubeEnv ('SonarQube server') {
//         sh 'mvn sonar:sonar'
//      }
   }

   stage ('Sources and Javadocs') {
      withMaven(mavenSettingsConfig: 'Birch-Maven-Settings') {
         bat "mvn source:jar javadoc:jar -pl :birch-common,:birch-rest-jaxrs,:birch-bridge-jms-kafka,:birch-security-oauth-spring,:birch-spring-kafka,:birch-kafka-utils,:birch-starter"
      }
   }

   stage ('Publish') {
      if (Globals.release) {
         // TODO publish to Maven Central
      }
      else {
         echo "${env.BRANCH_NAME} branch does not publish artifacts"
      }
   }
}