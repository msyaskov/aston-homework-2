FROM tomcat:latest
RUN apt update && apt install openssh-server -y
RUN useradd -rm -d /home/deployer -s /bin/bash -g root -u 1000 deployer
RUN  echo 'deployer:deployer' | chpasswd
RUN service ssh start
CMD service ssh start;catalina.sh run