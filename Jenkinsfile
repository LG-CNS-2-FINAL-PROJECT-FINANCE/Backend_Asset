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
                // withKubeConfig-kubectl이 사용할 인증 설정파일
                withKubeConfig([credentialsId: KUBE_CONFIG_ID]) {
                    // sshagent를 사용하여 키로 ssh 연결 수행-비번 필요없어
                    sshagent(credentials: [KUBE_SSH_KEY_ID]) {
                        sh """
                            #!/bin/bash
                            set -e

                            echo "## 1. Connecting to Minikube's Docker/Podman daemon..."
                            eval \$(ssh -o StrictHostKeyChecking=no ${KUBE_USER}@${KUBE_IP} 'minikube -p minikube podman-env')

                            unset CONTAINER_SSHKEY

                            echo "## 2. Building image directly inside Minikube..."
                            podman build -t ${DOCKER_IMAGE_NAME} .

                            echo "## 3. Applying Kubernetes manifests..."
                            kubectl apply -f k8s/

                            echo "## 4. Restarting deployment to apply changes..."
                            kubectl rollout restart deployment/ddiring-backend-asset-deployment
                        """
                    }
                }
            }
        }
    }
}