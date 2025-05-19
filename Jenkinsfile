pipeline {
  agent any

  tools {
    jdk 'jdk17'
  }

  environment {
    JAVA_HOME = 'tool jdk17'
    IMAGE_NAME = "my-image-${env.BUILD_ID}"
  }

  stages {
    stage('checkout') {
      steps {
        checkout scm
      }
    }

    stage('build') {
      steps {
        sh 'java -version'
        sh './mvnw clean package'
'
      }
    }

    stage('Push image') {
      steps {
        script {
          docker.withRegistry('590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops', 'ecr:ap-northeast-2:1df06cbe-4aa6-412f-aca9-1a4efc3a067f') {
            app = docker.build("590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops")
            app.push("latest_${env.BUILD_ID}")
          }

          sh """docker rmi 166132032896.dkr.ecr.ap-northeast-2.amazonaws.com/new_name_auction:latest_${env.BUILD_ID}"""
        }
      }
    }
  }
}
