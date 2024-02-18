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
        // stage('Test') {
            // steps {
                // echo '--| Test'
                // sh './gradlew test --no-daemon'
                // junit '**/build/test-results/test/*.xml'
            // }
        // }
        stage('Build WAR') {
            steps {
                echo '--| Build WAR'
                sh './gradlew war --no-daemon'
            }
        }
        stage('Deploy') {
            steps([$class: 'BapSshPromotionPublisherPlugin']) {
            sshPublisher(
                continueOnError: false, failOnError: true,
                publishers: [
                    sshPublisherDesc(
                        configName: "kubernetes_master",
                        verbose: true,
                        transfers: [
                            sshTransfer(execCommand: "/bin/rm -f /usr/local/tomcat/webapps/aston-hw2.war"),
                            sshTransfer(sourceFiles: "./build/libs/aston-hw2.war",)
                        ]
                    )
                ]
            )
        }
        }
    }
}
