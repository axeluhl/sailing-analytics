package paths

import (
	"os"
	"os/user"
	"path"
)

func GetHome() string {
	// get the home of the current user if possible
	currentUser, err := user.Current()
	if err == nil {
		return currentUser.HomeDir
	}
	// assume a unixoid environment and return the HOME env value
	homeEnv, homeEnvFound := os.LookupEnv("HOME")
	if homeEnvFound {
		return homeEnv
	}
	// last resort: assume typical setup
	return "/home/sailing"
}

func GetCode() string {
	return path.Join(GetHome(), "code")
}

func GetConfiguration() string {
	return path.Join(GetCode(), "configuration")
}

func GetServers() string {
	return path.Join(GetHome(), "servers")
}

func GetServer(name string) string {
	return path.Join(GetServers(), name)
}

func GetApacheConf() string {
	return path.Join(GetHome(), "apache-events")
}
