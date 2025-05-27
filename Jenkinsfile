pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        FUNCTION_NAME = "TriggerTrivyScan"
        PAYLOAD_FILE = "lambda-payload.json"
        RESPONSE_FILE = "lambda-response.json"
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
                    def scanId = "build-${env.BUILD_NUMBER}"

                    writeFile file: "${PAYLOAD_FILE}", text: """
                    {
                        "image": "${ECR_REPO}:${IMAGE_TAG}",
                        "repo": "${REPO_URL}",
                        "scan_id": "${scanId}",
                        "generate_sbom": true
                    }
                    """.stripIndent().trim()

                    echo '▶️ Lambda 함수 호출 중...'

                    def result = sh(
                        script: """
                            aws lambda invoke \
                              --function-name ${FUNCTION_NAME} \
                              --region ${REGION} \
                              --cli-binary-format raw-in-base64-out \
                              --payload file://${PAYLOAD_FILE} \
                              ${RESPONSE_FILE}
                        """,
                        returnStatus: true
                    )

                    if (result != 0) {
                        error("❌ Lambda 호출 실패! 종료 코드: ${result}")
                    } else {
                        echo "✅ Lambda 호출 성공!"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "🎉 전체 빌드 및 Trivy Lambda 호출 성공!"
        }
        failure {
            echo "❌ 빌드 실패! 로그 확인 필요"
        }
    }
}
