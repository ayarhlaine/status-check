void setBuildStatus(String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/ayarhlaine/status-check"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/ayarhlaine/unit-tests"],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}

pipeline {
   agent {
        docker { 
            image 'node:12-alpine' 
            args '-v node-modules:/.npm'
        }
    }
   stages {
      stage('Checkout') {
         steps {
            checkout([$class: 'GitSCM', branches: [[name: '*/feature**']], userRemoteConfigs: [[url: 'https://github.com/ayarhlaine/status-check']]])
         }
      }
      stage('Unit Tests Pending => github') {
         steps {
            setBuildStatus("Unit Tests", "PENDING");
         }
      }
      stage('Install Depandencies') {
         steps {
             sh 'npm install'
         }
      }
      stage('Unit Test') {
         steps {
             sh 'CI=true npm test'
         }
      }
   }
   post {
    success {
        setBuildStatus("Unit Tests succeeded", "SUCCESS");
    }
    failure {
        setBuildStatus("Unit Tests failed", "FAILURE");
    }
  }
}