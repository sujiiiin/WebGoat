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

       stage('🚀 Generate SBOM for each commit') {
            steps {
                script {
                    def repoName = env.REPO_URL.tokenize('/').last().replace('.git', '')
                    def buildTag = env.BUILD_TAG.replaceAll('[^a-zA-Z0-9]', '_')
        
                    sh """
                        rm -rf recent-commits && mkdir recent-commits
                        git clone --quiet --branch ${env.BRANCH} ${env.REPO_URL} recent-commits
                    """
        
                    def commitList = []
                    dir('recent-commits') {
                        def out = sh(
                            script: "git log ${env.GIT_PREVIOUS_COMMIT}..${env.GIT_COMMIT} --pretty=format:'%H'",
                            returnStdout: true
                        ).trim()
                        commitList = out.split("\n").findAll { it?.trim() }
                    }
        
                    if (commitList.isEmpty()) {
                        echo "✅ 변경된 커밋이 없어 SBOM 작업 생략"
                        return
                    }
        
                    echo "📌 변경된 커밋 목록:\n${commitList.join('\n')}"
        
                    def jobs = [:]
                    for (int i = 0; i < commitList.size(); i++) {
                        def commitId = commitList[i]
                        def jobId = "${buildTag}_${i}"
                        def id = i // 클로저 내 캡처 방지용
        
                        jobs["SBOM-${id}"] = {
                            node('sca') {
                                sh """
                                    /home/ec2-user/run_sbom_pipeline.sh '${env.REPO_URL}' '${repoName}' '${jobId}' '${commitId}'
                                """
                            }
                        }
                    }
        
                    parallel jobs
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
