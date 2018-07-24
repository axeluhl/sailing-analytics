package agent

import (
	"bytes"
	"encoding/json"
	log "github.com/sirupsen/logrus"
	"golang.org/x/crypto/ssh"
	"os"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/aws"
	"sapsailing.com/orchestrator/paths"
)

func DeployAgent(sshConnection *AgentConnection) error {
	agentFile, err := os.Open(paths.GetAgent())
	if err != nil {
		return err
	}
	defer agentFile.Close()

	err = sshConnection.Upload(agentFile, aws.RemoteAgentPath, 0755)
	if err != nil {
		return err
	}

	return nil
}

func InvokeAgent(sshConnection *AgentConnection, input *common.AgentInput) error {
	serialData, err := json.Marshal(input)
	if err != nil {
		return err
	}

	inputReader := bytes.NewReader(serialData)
	env := map[string]string{
		common.LogEnvVar: log.GetLevel().String(),
	}
	log.Debugf("Env to forward: %s", env)
	_, err = sshConnection.RunCommand(aws.RemoteAgentPath, env, inputReader, common.LogOutput, common.LogOutput)

	switch err.(type) {
	case *ssh.ExitError:
		exitErr := err.(*ssh.ExitError)
		log.Debugf("Agent exited with code %d!", exitErr.ExitStatus())
	}
	return err
}
