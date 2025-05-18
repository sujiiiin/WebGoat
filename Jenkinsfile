pipeline {
  agent any

  environment {
    IMAGE_NAME = "<AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/webgoat"
    REGION = "ap-northeast-2"
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/sujiiiin/webgoat.git'
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          sh '''
          aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $IMAGE_NAME
          docker build -t webgoat .
          docker tag webgoat:latest $IMAGE_NAME:latest
          docker push $IMAGE_NAME:latest
          '''
        }
      }
    }
  }
}
