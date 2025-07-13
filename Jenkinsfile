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

        stage('🧪 병렬 실행 시작') {
            parallel {
                stage('🚀 Generate SBOM') {
                    agent { label 'sca' }
                    steps {
                        script {
                            def repoUrl = scm.userRemoteConfigs[0].url
                            def repoName = repoUrl.tokenize('/').last().replace('.git', '')
                            def buildId = env.BUILD_NUMBER
                            def repoDir = "/tmp/${repoName}_${buildId}"

                            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                                sh """
                                    /home/ec2-user/run_sbom_pipeline.sh '${repoUrl}' '${repoName}' '${buildId}' '${repoDir}' > /home/ec2-user/logs/sbom_${buildId}.log 2>&1
                                """
                            }

                            echo "✅ SBOM 로그: /home/ec2-user/logs/sbom_${buildId}.log"
                        }
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
