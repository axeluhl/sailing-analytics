FROM donaldduck70/openjdk:1.8.0_242
LABEL maintainer=axel.uhl@gmx.de
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN apt-get update \
 && apt-get install -y vim telnet dnsutils net-tools jq
COPY vimrc /root/.vimrc
RUN wget -O /tmp/rds.pem https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem \
 && keytool -importcert -alias AWSRDS -file /tmp/rds.pem -keystore /usr/lib/jvm/java-8-openjdk-arm64/jre/lib/security/cacerts -noprompt -storepass changeit \
 && rm /tmp/rds.pem
RUN wget -O /tmp/RELEASE.tar.gz http://releases.sapsailing.com/RELEASE/RELEASE.tar.gz \
 && tar xzvpf /tmp/RELEASE.tar.gz \
 && rm /tmp/RELEASE.tar.gz
COPY env.sh .
COPY start .
COPY JavaSE-11.profile .
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
