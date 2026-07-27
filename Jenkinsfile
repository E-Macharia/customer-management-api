pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'Java 21'
    }

    environment {
        IMAGE_NAME = 'emacharia/customer-management-api'
        DOCKER_CREDS = 'docker-hub-credentials'
        // Render deploy webhook URL (stored as a secret text credential in Jenkins)
        RENDER_WEBHOOK = credentials('render-deploy-webhook-url')
    }

    stages {
        stage('Compile') {
            steps {
                echo 'Compiling the code...'
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Running Unit & Integration Tests...'
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging Jar file...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo 'Building Docker image...'
                script {
                    dockerImage = docker.build("${IMAGE_NAME}:${env.BUILD_NUMBER}")
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDS) {
                        echo 'Pushing Docker image to Docker Hub...'
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Deploy to Render') {
            steps {
                echo 'Triggering Render deployment webhook...'
                sh "curl -X POST '${RENDER_WEBHOOK}'"
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution complete.'
        }
        success {
            echo 'Build, Test, and Deployment completed successfully!'
        }
        failure {
            echo 'Build failed. Please check the logs.'
        }
    }
}
