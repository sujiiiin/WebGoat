pipeline {
  agent any

  environment {
    AWS_ACCOUNT_ID = '590715976556'   // ← 본인 계정으로 변경
    REGION = 'ap-northeast-2'
    IMAGE_NAME = "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/webgoat"
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/your-username/webgoat.git', branch: 'develop'
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          sh """
            echo '🔐 ECR 로그인'
            aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $IMAGE_NAME

            echo '🔧 Docker 이미지 빌드'
            docker build -t webgoat .

            echo '📦 이미지 태깅 및 푸시'
            docker tag webgoat:latest $IMAGE_NAME:latest
            docker push $IMAGE_NAME:latest
          """
        }
      }
    }

    // 선택 사항
    stage('Notify or Deploy') {
      steps {
        echo '🎯 ECR 푸시 완료 후 추가 작업 (예: CodeDeploy 배포 트리거)'
      }
    }
  }
}
