pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
        DEP_TRACK_URL = "http://43.201.22.52:8081/api/v1/bom"
        DEP_TRACK_API_KEY = credentials('dependency-track-api-key')
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

        stage('📄 Generate SBOM') {
            steps {
                sh 'syft $ECR_REPO:$IMAGE_TAG -o cyclonedx-json > sbom.json'
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

        stage('📤 Upload SBOM to Dependency-Track') {
            steps {
                sh '''
                curl -X POST \
                  -H "X-Api-Key: ${DEP_TRACK_API_KEY}" \
                  -F "sbom=@sbom.json" \
                  ${DEP_TRACK_URL}
                '''
            }
        }
    }

    post {
        success {
            echo "🎉 빌드 + ECR 푸시 + SBOM 업로드 완료!"
        }
        failure {
            echo "❌ 빌드 실패! 로그 확인 필요"
        }
    }
}
