pipeline {
    agent { label 'master' }

    environment {
        ECR_REPO = "590715976556.dkr.ecr.ap-northeast-2.amazonaws.com/whs/devops"
        IMAGE_TAG = "latest"
        REGION = "ap-northeast-2"
        SBOM_EC2_USER = "ec2-user"
        SBOM_EC2_IP = "172.31.11.127"
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
                    def buildId = env.BUILD_ID
                    def jobDir = "${repoName}_${buildId}"
        
                    echo "📍 REPO URL: ${repoUrl}"
                    echo "📁 Project Name: ${repoName}"
                    echo "🆔 Build ID: ${buildId}"
        
                    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-sbom-key', keyFileVariable: 'SSH_KEY')]) {
                        def remoteCmd = """
                            ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ${env.SBOM_EC2_USER}@${env.SBOM_EC2_IP} '
                                set -e
        
                                echo "[+] 작업 디렉토리 생성 및 클론: /tmp/${jobDir}"
                                rm -rf /tmp/${jobDir}
                                git clone ${repoUrl} /tmp/${jobDir}
        
                                echo "[+] 언어 및 Java 버전 감지"
                                cd /tmp/${jobDir}
                                bash /home/ec2-user/detect-test.sh
        
                                IMAGE_TAG=\$(cat /tmp/${jobDir}/cdxgen_image_tag.txt)
                                echo "[+] 선택된 이미지 태그: \$IMAGE_TAG"
        
                                if [ "\$IMAGE_TAG" = "cli" ]; then
                                    echo "[🚀] CDXGEN(CLI) 도커 실행"
                                    docker run --rm -v \$(pwd):/app ghcr.io/cyclonedx/cdxgen:latest -o sbom.json
                                else
                                    echo "[🚀] CDXGEN(Java) 도커 실행 (\$IMAGE_TAG)"
                                    docker run --rm -v \$(pwd):/app ghcr.io/cyclonedx/cdxgen-\$IMAGE_TAG:latest -o sbom.json
                                fi
        
                                echo "[+] Dependency-Track 업로드 시작"
                                /home/ec2-user/upload-sbom.sh ${jobDir}
        
                                echo "[🧹] 작업 디렉토리 정리"
                                rm -rf /tmp/${jobDir}
                            '
                        """
                        sh remoteCmd
                    }
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

    post {
        always {
            sh 'rm -f sbom.json || true'
        }
    }
}
