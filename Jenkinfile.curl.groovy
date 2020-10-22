void setBuildStatus(String DESCRIPTION, String STATE, String GITHUB_TOKEN, String COMMIT_SHA) {
  script{
      sh(script: 'chmod +x ./sendStatus.sh')
      sh(
          script: "GITHUB_TOKEN=\"$GITHUB_TOKEN\" COMMIT_SHA=\"$COMMIT_SHA\" STATE=$STATE DESCRIPTION=$DESCRIPTION TARGET_URL=http://localhost sh ./sendStatus.sh"
        )
  }
}

pipeline {
   agent {
        docker { 
            image 'my-node-worker' 
            args '-v node-modules:/.npm'
        }
    }
    environment {
        COMMIT_SHA = ''
        REPO_NAME = 'https://github.com/ayarhlaine/status-check'
        GITHUB_ACCESS_TOKEN = credentials('repostatusToken')
    }
   stages {
      stage('Checkout') {
         steps {
            checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://github.com/ayarhlaine/status-check']]])
         }
      }
      stage('GET COMMIT SHA') {
         steps {
            script {
                COMMIT_SHA =  sh(script: 'git rev-parse --verify HEAD', returnStdout: true)
            }
         }
      }
      stage('Send Pending => github') {
         steps {
            setBuildStatus("unit-test-pending", "pending", GITHUB_ACCESS_TOKEN, COMMIT_SHA);
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
        setBuildStatus("unit-tests-succeeded", "success", GITHUB_ACCESS_TOKEN, COMMIT_SHA);
    }
    failure {
        setBuildStatus("unit-tests-failed", "failure", GITHUB_ACCESS_TOKEN,COMMIT_SHA);
    }
  }
}