pipeline {
    agent none

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        SBOM_EC2_USER = "ec2-user"
        SBOM_EC2_IP = "172.31.11.127"
        REPO_URL = "https://github.com/sujiiiin/WebGoat.git"
    }

    stages {
        stage('📦 Checkout') {
            agent { label 'master' }
            steps {
                checkout scm
            }
        }

        stage('🔨 Build JAR') {
            agent { label 'master' }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

         stage('🧪 병렬 실행 제거: SBOM 생성 nohup') {
            agent { label 'SCA' }
            steps {
                script {
                    def repoUrl = scm.userRemoteConfigs[0].url
                    def repoName = repoUrl.tokenize('/').last().replace('.git', '')
                    def buildId = env.BUILD_NUMBER
                 
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh """
                            echo "[+] SBOM 생성 시작 (nohup)"
                            nohup /home/ec2-user/run_sbom_pipeline.sh '${repoUrl}' '${repoName}' '${buildId}' > /tmp/sbom_${repoName}_${buildId}.log 2>&1 &
                        """
                    }
                }
            }
        }

        stage('🐳 Docker Build & Push') {
            agent { label 'master' }
            stages {
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
    }
}
