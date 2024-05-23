FROM eclipse-temurin:8-jdk
# This Dockerfile assumes that the release to use is provided as
# ${RELEASE}.tar.gz in the current Docker workspace. Use, e.g.,
# the configuration/github-download-release-assets.sh script to
# obtain the tar.gz file for a specific or the latest "main" release.
# To build with an SAP JVM 8 base image built using Dockerfile_sapjvm, use something like:
#FROM sapjvm8:8.1.098
ARG RELEASE
LABEL maintainer=axel.uhl@sap.com
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN apt-get update \
 && apt-get install -y vim telnet dnsutils net-tools jq
COPY vimrc /root/.vimrc
COPY ${RELEASE}.tar.gz /tmp
RUN tar xzvpf /tmp/${RELEASE}.tar.gz \
 && rm /tmp/${RELEASE}.tar.gz
COPY env.sh .
RUN cat env-default-rules.sh >>env.sh
COPY start .
COPY JavaSE-11.profile .
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
