# Using Autossh in a tunnel environment

Autossh is a tool which enables users to monitor their current ssh connection and is able to restart connections in specific cases.

## Installation
To install autossh in CentOS 7 (current EC2-environment in AWS), you need to compile it from source.  
The following commands can help with the installation:

+ `sudo yum install wget gcc make`
+ `wget http://www.harding.motd.ca/autossh/autossh-1.4g.tgz`
+ `tar -xf autossh-1.4g.tgz`
+ `cd autossh-1.4g`
+ `./configure`
+ `make`
+ `sudo make install`
+ `sudo ln -s /usr/local/bin/autossh /usr/local/sbin/autossh`

The last command is useful when you want to access autossh with the root user.

The only problem with this approach is that the source compilation in this case doesn't provide an uninstall operation.  
It can therefore be useful to use the tool `checkinstall` instead of `sudo make install`.

## Configuration
It is benefitial to use tmux for the usage of autossh to create a terminal session, in which autossh can run without blocking the whole terminal session. Use `yum install tmux` for the installation.

Autossh itself relies on ssh and passes its terminal arguments to ssh. There are a few exeptions, for example the -M argument, which specifies the monitoring and echo port needed for the operation of autossh (uses the specified port and the port + 1). Autossh can be configured via environment variables, refer to the autossh manual for further information (`man autossh`).  
It can be useful to tweak the ssh_config and sshd_config to assure a quick recovery from failovers (see [Olympic Setup](/wiki/info/landscape/olympic-setup.md#tunnels) for the configuration). A typical command for creating a ssh tunnel is the following:

```
autossh -M 20000 -N -L *:5672:localhost:5672 -i /root/.ssh/id_rsa <ip-address>
```

+ -N specifies to not execute remote commands
+ -L specifies the connection to be forwarded
+ -i specifies the identity file