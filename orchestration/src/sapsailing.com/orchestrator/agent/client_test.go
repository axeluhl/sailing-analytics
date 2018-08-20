package agent

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"os"
	"sapsailing.com/orchestrator/paths"
)

func TestValidSSHConnection(t *testing.T) {
	user := "root"

	privateKeyPath, _ := paths.GetKeyPair()
	auth, err := KeyPairAuthFromPrivateKey(privateKeyPath)

	// Skip this test if the private key does not exist.
	if os.IsNotExist(err) {
		t.SkipNow()
	}

	assert.NoError(t, err, "Loading the keyboard should not fail!")

	client := &AgentConnection{
		Ip:   "sapsailing.com",
		User: user,
		Port: 22,
		Auth: auth,
	}
	out, err := client.RunSimpleCommand("whoami")

	assert.NoError(t, err, "Command should not fail to execute")
	assert.Equal(t, user, strings.TrimSpace(out))
}
