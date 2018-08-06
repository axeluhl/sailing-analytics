package primitive

import (
	"fmt"
	"github.com/go-resty/resty"
	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"math"
	"net"
	"os"
	"path"
	"sapsailing.com/agent/paths"
	"sapsailing.com/common"
	"sapsailing.com/common/sailing"
	"strings"
	"time"
)

func SetupNewServer(name string, release string, environment string) (common.UndoFunction, error) {
	serverPath := paths.GetServer(name)

	err := os.MkdirAll(serverPath, 0755)
	if err != nil {
		return nil, err
	}

	refreshInstanceScript := path.Join(paths.GetCode(), "java", "target", "refreshInstance.sh")
	installReleaseCmd := []string{refreshInstanceScript, "install-release", release}
	env := []string{"DEPLOY_TO=" + name}
	success, output, err := common.RunCommand(serverPath, env, "bash", installReleaseCmd...)
	if err != nil {
		return nil, errors.Wrapf(err, "command failed: %s", output)
	}
	if !success {
		return nil, fmt.Errorf("command failed: %s", output)
	}

	rollback := func() {
		log.Warnf("Rolling back the new instance %s...", name)
		os.RemoveAll(serverPath)
	}

	log.Infof("Installing environment: %s...", environment)
	err = InstallEnvironment(environment, path.Join(serverPath, "env.sh"))
	if err != nil {
		rollback()
		return nil, err
	}

	return rollback, nil
}

func InstallEnvironment(env string, to string) error {
	response, err := resty.R().Get("http://releases.sapsailing.com/environments/" + env)
	if err != nil {
		return err
	}

	data := response.String()
	f, err := os.OpenFile(to, os.O_APPEND|os.O_RDWR|os.O_SYNC, 0755)
	if err != nil {
		return err
	}

	f.WriteString("\n\n# Environment: " + env + "\n")
	f.WriteString(data)
	f.WriteString("\n")

	err = f.Close()
	if err != nil {
		return err
	}

	return nil
}

func UpdateCode() (common.UndoFunction, error) {
	var env []string
	codeDir := paths.GetCode()
	success, output, err := common.RunCommand(codeDir, env, "git", "rev-parse", "HEAD")
	if !success {
		return nil, fmt.Errorf("failed to parse HEAD")
	}
	if err != nil {
		return nil, err
	}
	oldHead := strings.TrimSpace(output)
	rollback := func() {
		log.Warnf("Rolling back the git pull by resetting to %s...", oldHead)
		common.RunCommand(codeDir, env, "git", "reset", "--hard", oldHead)
	}
	success, _, err = common.RunCommand(codeDir, env, "git", "pull")
	if err != nil {
		rollback()
		return nil, err
	}
	if !success {
		rollback()
		return nil, fmt.Errorf("git pull was unsuccessful")
	}
	if err != nil {
		return nil, err
	}

	return rollback, nil
}

func CreateEvents(appPort uint16, specs []common.EventSpec) ([]sailing.SailingEvent, error) {
	appConf := &sailing.ApplicationConfig{
		Host:          "localhost",
		Port:          appPort,
		ApiVersion:    1,
		AdminPassword: "admin",
		AdminUsername: "admin",
	}

	appClient, err := sailing.WaitForAuthenticatedClient(appConf, 5*time.Second)
	if err != nil {
		return nil, err
	}

	events := make([]sailing.SailingEvent, len(specs))

	for i, spec := range specs {
		log.Infof("Creating event %s (%s)", spec.DisplayName, spec.Hostname)
		event, err := retryCreateEvent(appClient, spec.DisplayName, 5*time.Second)
		if err != nil {
			return nil, err
		}
		events[i] = *event
	}

	return events, nil
}

func retryCreateEvent(client *sailing.ApplicationClient, displayName string, delay time.Duration) (*sailing.SailingEvent, error) {
	try := 0
	for {
		event, err := client.CreateEvent(displayName, false)
		if err == nil {
			return event, nil
		}
		switch err.(type) {
		case *sailing.ApiError:
			apiErr := err.(*sailing.ApiError)
			if !(apiErr.ResponseStatus == 404 || apiErr.ResponseStatus >= 500) {
				return nil, err
			}
		default:
			return nil, err
		}
		try++
		if try >= 10 {
			return event, err
		}
		time.Sleep(delay)
	}
}

func SetEnvShValues(serverName string, values map[string]string) (common.UndoFunction, error) {
	envsh := path.Join(paths.GetServer(serverName), "env.sh")
	return PatchBashConf(envsh, values)
}

func FindNextFreePortFrom(from uint16) uint16 {
	return FindNextFreePortFromTo(from, math.MaxUint16)
}

func FindNextFreePortFromTo(from uint16, to uint16) uint16 {
	i := from
	for i <= to {
		_, err := net.Dial("tcp", "localhost:"+string(from))
		if err != nil {
			return i
		}
		i++
	}
	return 0
}
