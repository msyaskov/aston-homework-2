#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Test') {
            script {
                try {
                    sh './gradlew clean test --no-daemon' //run a gradle task
                } finally {
                    junit '**/build/test-results/test/*.xml' //make the junit test results available in any case (success & failure)
                }
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}