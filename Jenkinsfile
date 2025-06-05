pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
        LAMBDA_SBOM_API = "https://7k76hsq129.execute-api.ap-northeast-2.amazonaws.com/generate-sbom"
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

        stage('📤 Send Code to Lambda for SBOM') {
            steps {
                script {
                    // zip 생성
                   sh '''
                        zip -r source.zip pom.xml src/ .mvn/ settings.xml -x "src/test/**" "src/main/resources/static/**" "target/**"
                        base64 source.zip > encoded.txt
                    '''

                    def encodedZip = readFile('encoded.txt').trim()

                    // JSON payload 구성
                    def payload = [
                        zip_base64: encodedZip,
                        project_name: "WebGoat",
                        project_version: "1.0.0"
                    ]

                    // Lambda API 호출
                    def response = httpRequest(
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        url: env.LAMBDA_SBOM_API,
                        requestBody: groovy.json.JsonOutput.toJson(payload)
                    )

                    echo "Lambda Response: ${response.content}"
                }
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
