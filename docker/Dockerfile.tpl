FROM buildpack-deps:sid-scm
LABEL maintainer=axel.uhl@sap.com
# Download and extract the SAP JVM 8
ENV PATH=${PATH}:/opt/sapjvm_8/bin
ENV JAVA_HOME=/opt/sapjvm_8
WORKDIR /opt
RUN apt-get update \
 && apt-get install -y wget unzip \
 && curl --cookie eula_3_1_agreed=tools.hana.ondemand.com/developer-license-3_1.txt "https://tools.hana.ondemand.com/additional/sapjvm-8.1.045-linux-x64.zip" --output sapjvm8-linux-x64.zip \
 && unzip sapjvm8-linux-x64.zip \
 && rm sapjvm8-linux-x64.zip \
 && echo "export JAVA_HOME=/opt/sapjvm_8; export PATH=\${PATH}:/opt/sapjvm_8/bin" > /etc/profile.d/javahome.sh
# Download and extract the release
WORKDIR /home/sailing/servers/server
RUN wget -O /tmp/RELEASE.tar.gz http://releases.sapsailing.com/RELEASE/RELEASE.tar.gz \
 && tar xzvpf /tmp/RELEASE.tar.gz \
 && rm /tmp/RELEASE.tar.gz
RUN apt-get install -y vim
COPY vimrc /root/.vimrc
RUN apt-get install -y telnet dnsutils net-tools
COPY env.sh .
COPY start .
COPY JavaSE-11.profile .
RUN echo "alias tailf=\"tail -f\"" >>/root/.bashrc
EXPOSE 8888 14888 8000 7091 6666
CMD [ "/home/sailing/servers/server/start", "fg" ]
