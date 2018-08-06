package configuration

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestLoadAWSConfig(t *testing.T) {
	config, err := LoadAWSConfig()

	require.NoError(t, err, "Loading the AWS config should not fail.")
	require.NotNil(t, config, "Config may not be nil")
	require.NotEqual(t, "", config.KeyId)
}
