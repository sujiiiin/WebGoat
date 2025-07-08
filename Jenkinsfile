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
                    sh """
                        rm -rf recent-commits && mkdir recent-commits
                        git clone --quiet --branch ${env.BRANCH} ${env.REPO_URL} recent-commits
                    """

                    dir('recent-commits') {
                        def commits = sh(
                            script: "git log ${env.GIT_PREVIOUS_COMMIT}..${env.GIT_COMMIT} --pretty=format:'%H'",
                            returnStdout: true
                        ).trim().split("\n")

                        echo "📌 변경된 커밋 목록:\n${commits.join('\n')}"

                        if (commits.size() == 0 || commits[0] == "") {
                            echo "✅ 변경된 커밋이 없어 SBOM 작업 생략"
                        } else {
                            def jobs = [:]
                            for (int i = 0; i < commits.size(); i++) {
                                def commitId = commits[i]
                                def buildId = "${env.BUILD_NUMBER}-${i}"
                                def repoName = env.REPO_URL.tokenize('/').last().replace('.git', '')

                                jobs["SBOM-${i}"] = {
                                    node('sca') {
                                        sh """
                                            /home/ec2-user/run_sbom_pipeline.sh '${env.REPO_URL}' '${repoName}' '${buildId}' '${commitId}'
                                        """
                                    }
                                }
                            }

                            parallel jobs
                        }
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
