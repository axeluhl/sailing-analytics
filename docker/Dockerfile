FROM eclipse-temurin:8-jdk
# To build with an SAP JVM 8 base image built using Dockerfile_sapjvm, use something like:
#FROM docker.sapsailing.com/sapjvm8:8.1.098
ARG RELEASE
LABEL maintainer=axel.uhl@sap.com
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN apt-get update \
 && apt-get install -y vim telnet dnsutils net-tools jq
COPY vimrc /root/.vimrc
RUN wget -O /tmp/${RELEASE}.tar.gz https://releases.sapsailing.com/${RELEASE}/${RELEASE}.tar.gz \
 && tar xzvpf /tmp/${RELEASE}.tar.gz \
 && rm /tmp/${RELEASE}.tar.gz
COPY env.sh .
RUN cat env-default-rules.sh >>env.sh
COPY start .
COPY JavaSE-11.profile .
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
