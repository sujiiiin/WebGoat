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
            agent { label 'SCA' }
            steps {
                script {
                    def repoUrl = scm.userRemoteConfigs[0].url
                    def repoName = repoUrl.tokenize('/').last().replace('.git', '')
        
                    // 로그 디렉토리 생성
                    sh 'mkdir -p /home/ec2-user/logs'
        
                    // 백그라운드로 실행 (nohup)
                    sh """
                        nohup /home/ec2-user/run_sbom_pipeline.sh '${repoUrl}' '${repoName}' '${env.BUILD_NUMBER}' > /home/ec2-user/logs/sbom_${env.BUILD_NUMBER}.log 2>&1 &
                    """
        
                    echo "✅ SCA 분석이 백그라운드에서 시작되었습니다. 로그: /home/ec2-user/logs/sbom_${env.BUILD_NUMBER}.log"
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
