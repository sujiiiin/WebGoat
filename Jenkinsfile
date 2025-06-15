pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        DEP_TRACK_URL = "http://<dependency-track-ip>:8081/api/v1/bom"  // 필요 시 수정
        DEP_TRACK_API_KEY = credentials('dependency-track-api-key')     // Jenkins Credentials 등록 필요
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

        stage('📄 Generate SBOM with CycloneDX CLI') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '''
                        echo "[+] Generating SBOM using CycloneDX CLI..."
                        cyclonedx app -i . -o bom.json
                    '''
                }
            }
        }

        stage('📤 Upload SBOM to Dependency-Track') {
            when {
                expression { fileExists('bom.json') }
            }
            steps {
                sh '''
                    echo "[+] Uploading SBOM to Dependency-Track..."
                    curl -X PUT "$DEP_TRACK_URL" \
                         -H "X-Api-Key: $DEP_TRACK_API_KEY" \
                         -H "Content-Type: application/json" \
                         --data @bom.json
                '''
            }
        }
    }

    post {
        always {
            sh 'rm -f bom.json source.zip || true'
        }
    }
}
