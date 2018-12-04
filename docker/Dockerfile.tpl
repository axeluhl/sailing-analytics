FROM openjdk:11
LABEL maintainer=axel.uhl@sap.com
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN apt-get update \
 && apt-get install -y wget \
 && wget -O /tmp/RELEASE.tar.gz http://releases.sapsailing.com/RELEASE/RELEASE.tar.gz \
 && tar xzvpf /tmp/RELEASE.tar.gz \
 && rm /tmp/RELEASE.tar.gz
RUN apt-get install -y vim
COPY vimrc /root/.vimrc
RUN apt-get install -y telnet dnsutils net-tools
COPY env.sh .
COPY start .
COPY JavaSE-11.profile .
# Workaround for https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=910804
RUN mkdir -p /docker-java-home/lib/jfr && \
    wget https://hg.openjdk.java.net/jdk/jdk11/raw-file/76072a077ee1/src/jdk.jfr/share/conf/jfr/default.jfc \
      -O /docker-java-home/lib/jfr/default.jfc && \
    wget https://hg.openjdk.java.net/jdk/jdk11/raw-file/76072a077ee1/src/jdk.jfr/share/conf/jfr/profile.jfc \
      -O /docker-java-home/lib/jfr/profile.jfc
RUN echo "alias tailf=\"tail -f\"" >>/root/.bashrc
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
