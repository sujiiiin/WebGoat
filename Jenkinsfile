pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        DEP_TRACK_URL = "http://<dependency-track-ip>:8081/api/v1/bom"
        DEP_TRACK_API_KEY = credentials('dependency-track-api-key')

        CDXGEN_HOST = "ec2-user@172.31.33.68"  // CDXGEN 설치된 EC2의 Private IP
        PROJECT_DIR = "/var/lib/jenkins/workspace/${env.JOB_NAME}"
        SBOM_PATH = "${PROJECT_DIR}/sbom.json"
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

        stage('📄 Generate SBOM (via CDXGEN EC2)') {
            steps {
                sh '''
                    echo "[+] CDXGEN EC2가 Jenkins EC2에 SSH 접속하여 SBOM 생성"
                    ssh $CDXGEN_HOST "
                        ssh ec2-user@$(hostname -I | awk '{print $1}') '
                            cd $PROJECT_DIR && \
                            cdxgen -o sbom.json
                        '
                    "
                '''
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
