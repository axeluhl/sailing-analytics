package primitive

import (
	log "github.com/sirupsen/logrus"
	"os"
	"path"
	"sapsailing.com/agent/paths"
	"sapsailing.com/common"
)

func TestApacheConfig() bool {
	success, output, err := common.RunCommandSimple("sudo", "apachectl", "configtest")
	if err != nil {
		log.Errorf("Error testing apache config: %s\n%s", err, output)
	}
	return success
}

func marcoEventSsl(hostname string, eventID string) string {
	return "Event-SSL " + hostname + " \"" + eventID + "\" 127.0.0.1 8888"
}

func macroPlainSsl(internalIP string) string {
	return "Plain-SSL " + internalIP + " 127.0.0.1 8888"
}

func macroStatus(internalHostname string) string {
	return "Status " + internalHostname + " internal-server-status"
}

func ReloadApacheDaemon() error {
	if !TestApacheConfig() {
		return nil
	}
	_, err := ControlDaemon("apache2", DaemonReload)
	return err
}

func CreateApacheEvent(hostname string, eventID string, internalIP string) (common.UndoFunction, error) {
	macros := []string{
		macroPlainSsl(internalIP),
		marcoEventSsl(hostname, eventID),
	}

	return createApacheMacroFile(hostname, macros)
}

func CreateApacheStatus(internalHostname string) (common.UndoFunction, error) {
	macros := []string{
		macroStatus(internalHostname),
	}

	return createApacheMacroFile("status", macros)
}

func createApacheMacroFile(name string, macros []string) (common.UndoFunction, error) {
	filePath := path.Join(paths.GetApacheConf(), name+".conf")
	f, err := os.OpenFile(filePath, os.O_RDWR|os.O_CREATE|os.O_SYNC|os.O_TRUNC, 0755)
	if err != nil {
		return nil, err
	}

	for _, line := range macros {
		_, err = f.Write([]byte("Use " + line + "\n"))
		if err != nil {
			return nil, err
		}
	}

	err = f.Close()

	return func() {
		log.Warnf("Rolling back the new event config %s...", filePath)
		os.Remove(filePath)
	}, err
}
