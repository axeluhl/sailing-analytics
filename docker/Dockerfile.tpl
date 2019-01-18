FROM donaldduck70/sapjvm8:8.1.046
LABEL maintainer=axel.uhl@sap.com
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN wget -O /tmp/RELEASE.tar.gz http://releases.sapsailing.com/RELEASE/RELEASE.tar.gz \
 && tar xzvpf /tmp/RELEASE.tar.gz \
 && rm /tmp/RELEASE.tar.gz
RUN apt-get install -y vim
COPY vimrc /root/.vimrc
RUN apt-get install -y telnet dnsutils net-tools jq
RUN wget -O /tmp/rds.pem https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem \
 && /opt/sapjvm_8/bin/keytool -importcert -alias AWSRDS -file /tmp/rds.pem -keystore /opt/sapjvm_8/jre/lib/security/cacerts -noprompt -storepass changeit \
 && rm /tmp/rds.pem
COPY env.sh .
COPY start .
COPY JavaSE-11.profile .
RUN echo "alias tailf=\"tail -f\"" >>/root/.bashrc
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
