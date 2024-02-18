#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Compile') {
            steps {
                echo '--| Compile'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean classes --no-daemon'
            }
        }
        stage('Test') {
            steps {
                echo '--| Test'
                sh './gradlew test --no-daemon'
                junit '**/build/test-results/test/*.xml'
            }
        }
        stage('Build WAR') {
            steps {
                echo '--| Build WAR'
                sh './gradlew war --no-daemon'
            }
        }
        stage('Deploy') {
            steps {
                echo '--| Deploy on Tomcat'
            }
        }
    }
}
