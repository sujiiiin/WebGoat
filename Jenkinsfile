pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        DEP_TRACK_URL = "http://<dependency-track-ip>:8081/api/v1/bom"
        DEP_TRACK_API_KEY = credentials('dependency-track-api-key')
        SBOM_EC2 = "ec2-user@172.31.11.127"

        
        PROJECT_DIR = "/var/lib/jenkins/workspace/${env.JOB_NAME}"
        
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

       stage('🚀 Generate SBOM via CDXGEN Docker') {
            steps {
                script {
                    def repoUrl = scm.userRemoteConfigs[0].url
                    def repoName = repoUrl.tokenize('/').last().replace('.git', '')

                    echo "📍 REPO URL: ${repoUrl}"
                    echo "📁 Project Name: ${repoName}"

                    sh """
                    ssh -o StrictHostKeyChecking=no \$SBOM_EC2 '
                        echo "[+] 클린 작업: /tmp/${repoName} 제거"
                        rm -rf /tmp/${repoName} && \

                        echo "[+] Git 저장소 클론: ${repoUrl}"
                        git clone ${repoUrl} /tmp/${repoName} && \

                        echo "[+] CDXGEN 실행"
                        cd /tmp/${repoName} && \
                        docker run --rm -v \\$(pwd):/app cdxgen-java17 -o sbom.json
                    '
                    """
                }
            }
        }

        stage('🐳 Docker Build') {
            steps {
                sh 'docker build -t $ECR_REPO:$IMAGE_TAG .'
            }
        }

        stage('🔐 ECR Login') {
            steps {
                sh 'aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPO'
            }
        }

        stage('🚀 Push to ECR') {
            steps {
                sh 'docker push $ECR_REPO:$IMAGE_TAG'
            }
        }
    } // ← stages 블록 닫기

    post {
        always {
            sh 'rm -f sbom.json || true'
        }
    }
}
