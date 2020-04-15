package paths

import (
	"os"
	"path"
)

const PrefixEnvName = "PREFIX"

func GetPrefix() string {
	prefix, found := os.LookupEnv(PrefixEnvName)
	if found {
		return prefix
	}
	// prefer pwd as its easier to debug when things to wrong
	workDir, err := os.Getwd()
	if err == nil {
		return workDir
	}
	return "."
}

func GetConfigs() string {
	return path.Join(GetPrefix(), "configs")
}

func GetInstanceTemplates() string {
	return path.Join(GetConfigs(), "instance_template")
}

func GetInstanceTemplate(name string) string {
	return path.Join(GetInstanceTemplates(), name+".json")
}

func GetSecrets() string {
	return path.Join(GetConfigs(), "secrets")
}

func GetAwsConfig() string {
	return path.Join(GetSecrets(), "conf_aws.json")
}

func GetKeyPair() (string, string) {
	private := path.Join(GetSecrets(), "id_rsa")
	return private, private + ".pub"
}

func GetBin() string {
	return path.Join(GetPrefix(), "bin")
}

func GetAgent() string {
	return path.Join(GetBin(), "agent-linux-amd64")
}
