#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Compile') {
            steps {
                echo '--| Compile'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean classes'
            }
        }
        stage('Test') {
            steps {
                echo '--| Test'
                sh './gradlew test'
                junit '**/build/test-results/test/*.xml'
            }
        }
        stage('Build WAR') {
            steps {
                echo '--| Build WAR'
                sh './gradlew war'
            }
        }
        stage('Deploy') {
            steps([$class: 'BapSshPromotionPublisherPlugin']) {
                sh 'mv build/libs/aston-hw2.war aston.war'
                sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                        sshPublisherDesc(
                            configName: "tomcat",
                            verbose: true,
                            transfers: [
                                sshTransfer(sourceFiles: "aston.war",)
                            ]
                        )
                    ]
                )
            }
        }
    }
}
