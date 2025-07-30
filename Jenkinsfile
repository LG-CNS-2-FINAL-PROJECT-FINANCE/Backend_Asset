#!/usr/bin/env groovy
def APP_NAME
def APP_VERSION
def DOCKER_IMAGE_NAME

pipeline {
    agent any

    environment {
        KUBE_IP = '192.168.56.100'
        KUBE_USER = 'admin'
        KUBE_SSH_KEY_ID = 'local-cluster-key' // 젠킨스 global credential key
        KUBE_CONFIG_ID = 'local-cluster-config' // 젠킨스 global credential configfile
    }

    tools {
        gradle 'Gradle 8.14.2' // 젠킨스 Tools의 Gradle 이름
        jdk 'OpenJDK 17' // 젠킨스 Tools의 JDK 이름
        dockerTool 'Docker' // 젠킨스 Tools의 Docker 이름
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

                    DOCKER_IMAGE_NAME = "${APP_NAME}:${APP_VERSION}"

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
                // Gradlew로 빌드
                sh './gradlew clean build'
            }
        }

        stage('Build Image and Deploy to Minikube') {
            steps {
                withKubeConfig([credentialsId: KUBE_CONFIG_ID]) {
                    sshagent(credentials: [KUBE_SSH_KEY_ID]) {
                        // 문자열 처리 및 여러 sh 명령 실행을 위해 script 블록 사용
                        script {
                            echo "## 1. Getting remote Minikube podman environment..."
                            // 원격에서 실행한 결과를 문자열로 가져옴
                            def remoteEnvString = sh(
                                script: "ssh -o StrictHostKeyChecking=no ${KUBE_USER}@${KUBE_IP} 'minikube -p minikube podman-env --root'",
                                returnStdout: true
                            )

                            echo "## 2. Correcting environment for remote connection..."
                            // ✨ [수정된 부분]
                            // 문자열에서 '127.0.0.1'을 실제 Minikube IP로 교체
                            def correctedEnvString = remoteEnvString.replaceAll('127.0.0.1', KUBE_IP)

                            // 수정된 환경 변수와 함께 후속 명령들을 실행
                            // withEnv 내에서 실행되는 sh 명령어들은 이 환경 변수들의 영향을 받음
                            // CONTAINER_SSHKEY를 빈 값으로 설정하여 이전 오류도 함께 해결
                            withEnv(["${correctedEnvString}", "CONTAINER_SSHKEY="]) {
                                echo "## 3. Building image directly inside Minikube..."
                                sh "sudo podman build -t ${DOCKER_IMAGE_NAME} ."

                                echo "## 4. Applying Kubernetes manifests..."
                                sh "kubectl apply -f k8s/"

                                echo "## 5. Restarting deployment to apply changes..."
                                sh "kubectl rollout restart deployment/my-spring-app-deployment"
                            }
                        }
                    }
                }
            }
        }
    }
}