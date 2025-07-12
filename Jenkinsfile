pipeline {
    agent { label 'master' }

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        SBOM_EC2_USER = "ec2-user"
        SBOM_EC2_IP = "172.31.11.127"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
        BRANCH = "main"
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
            agent { label 'sca' }
            steps {
                script {
                    def repoUrl = scm.userRemoteConfigs[0].url
                    def repoName = repoUrl.tokenize('/').last().replace('.git', '')
        
                    // 백그라운드로 실행 (nohup)
                    sh """
                         setsid /home/ec2-user/run_sbom_pipeline.sh ... > log 2>&1 < /dev/null &        
                }
            }
        }




        stage('🐳 Docker Build') {
            steps {
                sh "docker build -t ${env.ECR_REPO}:${env.IMAGE_TAG} ."
            }
        }

        stage('🔐 ECR Login') {
            steps {
                sh "aws ecr get-login-password --region ${env.REGION} | docker login --username AWS --password-stdin ${env.ECR_REPO}"
            }
        }

        stage('🚀 Push to ECR') {
            steps {
                sh "docker push ${env.ECR_REPO}:${env.IMAGE_TAG}"
            }
        }
    }

}
