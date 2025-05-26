pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
    }

    stages {
        stage('📦 Checkout') {
            steps {
                checkout scm
            }
        }

        stage('🔨 Build JAR') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('🐳 Docker Build') {
            steps {
                sh '''
                docker build -t $ECR_REPO:$IMAGE_TAG .
                '''
            }
        }

        stage('🔍 Trivy Scan') {
            steps {
                sh '''
                echo "▶️ Running Trivy vulnerability scan with secret scan enabled..."

                # 임시 저장소와 캐시 경로 설정
                export TMPDIR=/var/lib/jenkins/trivy-tmp
                export TRIVY_CACHE_DIR=/var/lib/jenkins/trivy-cache
                mkdir -p $TMPDIR $TRIVY_CACHE_DIR

                # 트리비 실행 - secret 스캔 유지 + 타임아웃 연장
                trivy image \
                  --cache-dir $TRIVY_CACHE_DIR \
                  --timeout 15m \
                  --exit-code 0 \
                  --severity HIGH,CRITICAL \
                  --format table \
                  $ECR_REPO:$IMAGE_TAG
                '''
            }
        }




        stage('🔐 ECR Login') {
            steps {
                sh '''
                aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPO
                '''
            }
        }

        stage('🚀 Push to ECR') {
            steps {
                sh 'docker push $ECR_REPO:$IMAGE_TAG'
            }
        }
    }

    post {
        success {
            echo "✅ 이미지 빌드 & Trivy 검사 & ECR 푸시 성공!"
        }
        failure {
            echo "❌ 실패! 로그 확인 필요"
        }
    }
}
