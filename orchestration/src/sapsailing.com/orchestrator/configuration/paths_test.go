package configuration

import (
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
	"sapsailing.com/orchestrator/paths"
)

func TestGetPrefix(t *testing.T) {
	workDir, err := os.Getwd()
	if err != nil {
		workDir = "."
	}
	oldPrefix, found := os.LookupEnv(paths.PrefixEnvName)
	prefix := "test"
	os.Setenv(paths.PrefixEnvName, prefix)
	assert.Equal(t, prefix, paths.GetPrefix())
	os.Unsetenv(paths.PrefixEnvName)
	assert.Equal(t, workDir, paths.GetPrefix())
	if found {
		os.Setenv(paths.PrefixEnvName, oldPrefix)
	} else {
		os.Unsetenv(paths.PrefixEnvName)
	}
}
