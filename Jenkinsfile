pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        LAMBDA_FUNC = "TriggerTrivyScan"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
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

        stage('📡 Trigger Trivy Lambda') {
            steps {
                script {
                    def payload = """
                    {
                        "image": "${ECR_REPO}:${IMAGE_TAG}",
                        "repo": "${REPO_URL}",
                        "scan_id": "build-${env.BUILD_ID}"
                    }
                    """

                    writeFile file: 'lambda-payload.json', text: payload

                    sh '''
                    aws lambda invoke \
                        --function-name $LAMBDA_FUNC \
                        --region $REGION \
                        --payload file://lambda-payload.json \
                        lambda-response.json

                    cat lambda-response.json
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "✅ 이미지 빌드, ECR 푸시 및 Trivy Lambda 트리거 성공!"
        }
        failure {
            echo "❌ 빌드 실패! 로그 확인 필요"
        }
    }
}
