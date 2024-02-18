#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean test --no-daemon' //run a gradle task
                junit '**/build/test-results/test/*.xml' //make the junit test results available in any case (success & failure)
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