pipeline {
    agent any

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        LAMBDA_SBOM_API = "https://7k76hsq129.execute-api.ap-northeast-2.amazonaws.com/default/generate-sbom"
        PROJECT_UUID = "c5edd688-1d38-4826-9ebf-c86eabee0ffe"
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

        stage('📤 Upload to S3 and Trigger Lambda') {
            steps {
                script {
                    def zipFile = "source.zip"
                    def s3Key = "sbom/${env.BUILD_ID}/${zipFile}"

                    // zip 만들고 S3 업로드
                    sh """
                        zip -r ${zipFile} pom.xml src/ .mvn/ settings.xml -x 'src/test/**' 'target/**'
                        aws s3 cp ${zipFile} s3://jenkins-sbom-source/${s3Key} --region $REGION
                    """

                    def payload = [
                        s3_bucket: "jenkins-sbom-source",
                        s3_key: s3Key,
                        project_uuid: env.PROJECT_UUID
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
                sh 'aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPO'
            }
        }

        stage('🚀 Push to ECR') {
            steps {
                sh 'docker push $ECR_REPO:$IMAGE_TAG'
            }
        }
    }

    post {
        always {
            sh 'rm -f source.zip'
        }
    }
}
