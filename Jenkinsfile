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

        stage('📤 Upload Zip to S3 and Trigger Lambda') {
            steps {
                script {
                    def zipFile = "source.zip"
                    def s3Key = "sbom/${env.BUILD_ID}/${zipFile}"
        
                    // zip 생성
                    sh "zip -r ${zipFile} pom.xml src/ .mvn/ settings.xml -x 'src/test/**' 'target/**'"
        
                    // S3 업로드
                    sh """
                        aws s3 cp ${zipFile} s3://jenkins-sbom-source/${s3Key} --region $REGION
                    """
        
                    // Lambda 호출
                    def payload = [
                        s3_bucket: "jenkins-sbom-source",
                        s3_key: s3Key,
                        project_name: "WebGoat",
                        project_version: "1.0.0"
                    ]
        
                    def response = httpRequest(
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        url: env.LAMBDA_SBOM_API,
                        requestBody: groovy.json.JsonOutput.toJson(payload),
                        validResponseCodes: '200:299'
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

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    post {
        always {
            sh 'rm -f source.zip'  // ✅ encoded.txt 제거!
        }
    }
}
