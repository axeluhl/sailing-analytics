package agent

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"path"
	"strings"
	"time"

	"github.com/pkg/sftp"
	log "github.com/sirupsen/logrus"
	"golang.org/x/crypto/ssh"
)

const (
	DefaultTimeout = 3 // second
)

type AgentConnection struct {
	Ip   string
	User string
	Auth ssh.AuthMethod
	Port int
}

func KeyPairAuthFromPrivateKey(file string) (ssh.AuthMethod, error) {
	buffer, err := ioutil.ReadFile(file)
	if err != nil {
		return nil, err
	}

	key, err := ssh.ParsePrivateKey(buffer)
	if err != nil {
		return nil, err
	}
	return ssh.PublicKeys(key), nil
}

func PassphraseAuthFromString(passphrase string) ssh.AuthMethod {
	return ssh.Password(passphrase)
}

func (c *AgentConnection) WaitForConnectivity(delay time.Duration) error {
	try := 0
	for {
		err := c.ProbeConnectivity()
		if err == nil {
			return nil
		}
		try++
		if try >= 10 {
			return err
		}
		time.Sleep(delay)
	}
}

func (c *AgentConnection) ProbeConnectivity() error {
	out, err := c.RunSimpleCommand("whoami")
	if err != nil {
		return err
	}
	user := strings.TrimSpace(out)
	if user != c.User {
		return fmt.Errorf("command succeeded, but user mismatched: %s(local) != %s(remote)", c.User, user)
	}

	return nil
}

func (c *AgentConnection) newClient() (*ssh.Client, error) {

	sshConfig := &ssh.ClientConfig{
		User:            c.User,
		Auth:            []ssh.AuthMethod{c.Auth},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
		Timeout:         time.Second * DefaultTimeout,
	}

	port := c.Port
	if port == 0 {
		port = 22
	}

	client, err := ssh.Dial("tcp", fmt.Sprintf("%s:%d", c.Ip, port), sshConfig)
	if err != nil {
		return nil, err
	}

	return client, nil
}

func (c *AgentConnection) RunCommand(cmd string, env map[string]string, stdin io.Reader, stdout io.Writer, stderr io.Writer) (string, error) {
	client, err := c.newClient()
	if err != nil {
		return "", err
	}
	defer client.Close()

	sess, err := client.NewSession()
	if err != nil {
		return "", err
	}
	defer sess.Close()

	for key, value := range env {
		log.Debugf("forwarding env: %s=%s", key, value)
		err = sess.Setenv(key, value)
		if err != nil {
			// TODO currently all env request fail. In the future propagate the error
			log.Errorf("Setting env %s=%s failed: %s", key, value, err)
			//return "", err
		}
	}
	var output []byte
	sess.Stdin = stdin
	if stdout != nil && stderr != nil {
		log.Debug("setup forwarding of stdout and stderr")
		sess.Stdout = stdout
		sess.Stderr = stderr
		log.Debugf("running the command with forwarded output: %s", cmd)
		err = sess.Run(cmd)
	} else {
		log.Debugf("running the command and return the output: %s", cmd)
		output, err = sess.CombinedOutput(cmd)
	}

	return string(output), err
}

func (c *AgentConnection) RunSimpleCommand(cmd string) (string, error) {
	out, err := c.RunCommand(cmd, map[string]string{}, bytes.NewReader(nil), nil, nil)
	return out, err
}

func (c *AgentConnection) newSftpClient() (*sftp.Client, error) {
	sshClient, err := c.newClient()
	if err != nil {
		return nil, err
	}

	client, err := sftp.NewClient(sshClient)
	if err != nil {
		return nil, err
	}

	return client, nil
}

func (c *AgentConnection) Upload(reader io.Reader, to string, mode os.FileMode) error {
	sftpClient, err := c.newSftpClient()
	if err != nil {
		return err
	}
	defer sftpClient.Close()

	directory := path.Dir(to)
	err = sftpClient.MkdirAll(directory)
	if err != nil {
		return err
	}

	f, err := sftpClient.Create(to)
	if err != nil {
		return err
	}

	defer f.Close()

	_, err = io.Copy(f, reader)
	if err != nil {
		return err
	}

	err = f.Chmod(mode)
	if err != nil {
		return err
	}

	return nil
}
