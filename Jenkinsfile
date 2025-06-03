pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
        DEP_TRACK_URL = "http://13.125.17.184:8081/api/v1/bom"
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
                sh '''
                    echo "[+] Generating CycloneDX SBOM..."
                    mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom -Dcyclonedx.outputFormat=json
                '''
                archiveArtifacts artifacts: 'target/bom.json', fingerprint: true
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

}
