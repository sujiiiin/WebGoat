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
                echo "▶️ Running Trivy vulnerability scan..."

                # 1. 디스크 기반 TMPDIR 설정
                export TMPDIR=/var/lib/jenkins/trivy-tmp
                mkdir -p $TMPDIR

                # 2. Trivy 캐시 디렉토리도 변경하면 더 좋음
                export TRIVY_CACHE_DIR=/var/lib/jenkins/trivy-cache
                mkdir -p $TRIVY_CACHE_DIR

                # 3. Trivy 실행
                trivy image --cache-dir $TRIVY_CACHE_DIR --exit-code 0 --severity HIGH,CRITICAL --format table $ECR_REPO:$IMAGE_TAG
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
