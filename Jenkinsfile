#!/usr/bin/env groovy
def APP_NAME
def APP_VERSION
def DOCKER_IMAGE_NAME

pipeline {
    agent any

    environment {
        //KUBE_IP = '192.168.56.100'
        //KUBE_USER = 'admin'
        //KUBE_SSH_KEY_ID = 'local-cluster-key' // 젠킨스 global credential key
        //KUBE_CONFIG_ID = 'local-cluster-config' // 젠킨스 global credential configfile
        REGISTRY_HOST = "192.168.56.200:5000"
        MANIFEST_REPO = 'git@github.com:LG-CNS-2-FINAL-PROJECT-FINANCE/Backend_Manifests.git'
        USER_EMAIL = 'ssassaium@gmail.com'
        USER_ID = 'kaebalsaebal'
        SERVICE_NAME = 'asset'

    }

    tools {
        gradle 'Gradle 8.14.2' // 젠킨스 Tools의 Gradle 이름
        jdk 'OpenJDK 17' // 젠킨스 Tools의 JDK 이름
        //dockerTool 'Docker' // 젠킨스 Tools의 Docker 이름
    }

    stages {
        stage('Set Version') {
            steps {
                script {
                    APP_NAME = sh (
                            script: "gradle -q getAppName",
                            returnStdout: true
                    ).trim()
                    APP_VERSION = sh (
                            script: "gradle -q getAppVersion",
                            returnStdout: true
                    ).trim()

                    DOCKER_IMAGE_NAME = "${REGISTRY_HOST}/${APP_NAME}:${APP_VERSION}"

                    sh "echo IMAGE_NAME is ${APP_NAME}"
                    sh "echo IMAGE_VERSION is ${APP_VERSION}"
                    sh "echo DOCKER_IMAGE_NAME is ${DOCKER_IMAGE_NAME}"
                }
            }
        }

        stage('Checkout Dev Branch') {
            steps {
                // Git에서 dev 브랜치의 코드를 가져옵니다.
                checkout scm
            }
        }

        stage('Build Spring Boot App') {
            steps {
                // gradlew 권한부여
                sh 'chmod +x gradlew'
                // 디버그
                sh 'ls -al src/main/resources/'
                sh 'cat src/main/resources/application.yaml'
                // Gradlew로 빌드
                sh './gradlew clean build'
            }
        }

        stage('Image Build and Push to Registry') {
            steps {
                // 컨테이너 빌드
                sh "echo Image building..."
                sh "sudo podman build -t ${DOCKER_IMAGE_NAME} ."
                // 레지스트리 푸쉬
                sh "echo Image pushing to local registry..."
                sh "sudo podman push ${DOCKER_IMAGE_NAME}"
            }
        }

        stage('Update Helm Values') {
            steps{
                script{
                    withCredentials([
                        credentialsId:'github-credential',
                        keyFileVariable:'GIT_SSH_KEY'
                    ]) {
                        def imageRepo = "${REGISTRY_HOST}/${APP_NAME}"
                        def imageTag = "${APP_VERSION}"

                        sh """
                             # Git 사용자 정보 설정(커밋 사용자 명시땜에)
                            #git config --global user.email "${USER_EMAIL}"
                            #git config --global user.name "${USER_ID}"
                            
                            # SSH Agent 설정
                            eval \$(ssh-agent -s)
                            ssh-add \${GIT_SSH_KEY}
                            
                            # 매니페스트 레포 클론
                            git clone ${MANIFEST_REPO}
                            cd manifest-repo

                            # yq를 사용하여 개발 환경의 values 파일 업데이트
                            yq -i '.image.repository = "${imageRepo}"' helm-chart/${SERVICE_NAME}/values-dev.yaml
                            yq -i '.image.tag = "${imageTag}"' helm-chart/${SERVICE_NAME}/values-dev.yaml
                            
                            # 변경 사항 커밋 및 푸시
                            git add helm-chart/${SERVICE_NAME}/values-dev.yaml
                            git commit -m "Update image tag for dev to ${DOCKER_IMAGE_NAME} [skip ci]"
                            git push origin master
                        """
                    }
                }
            }
        }
        /*
        stage('Copy Artifacts to Minikube') {
            steps {
                sshagent(credentials: [KUBE_SSH_KEY_ID]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${KUBE_USER}@${KUBE_IP} 'mkdir -p ~/app'
                        rsync -avz --delete --exclude '.git/' ./ ${KUBE_USER}@${KUBE_IP}:~/app/
                    """
                }
            }
        }

        stage('Remote Podman Build & K8s Deploy') {
            steps {
                sshagent(credentials: [KUBE_SSH_KEY_ID]) {
                    withKubeConfig([credentialsId: KUBE_CONFIG_ID]) {
                        script {
                            def remoteBuildScript = """
                                cd ~/app
                                sudo podman build -t ${DOCKER_IMAGE_NAME} .
                                kubectl apply -f k8s/
                                kubectl rollout restart deployment/ddiring-backend-asset-deployment
                            """

                            sh """
                                ssh -o StrictHostKeyChecking=no ${KUBE_USER}@${KUBE_IP} '${remoteBuildScript}'
                            """
                        }
                    }
                }
            }
        }
        */
    }
}