package scenario

import (
	log "github.com/sirupsen/logrus"
	"sapsailing.com/agent/primitive"
	"sapsailing.com/common"
	"strconv"
)

func Master(input *common.AgentInput) bool {
	log.Info("Master scenario started")

	undoStack := &common.UndoStack{}

	log.Info("Updating the code repository...")
	undo, err := primitive.UpdateCode()
	if err != nil {
		log.Errorf("Failed to update the code: %s", err)
		return false
	}
	undoStack.Push(undo)

	sailingDaemon := primitive.SailingDaemon(input.ServerName)

	log.Infof("Setting up new server instance %s...", input.ServerName)
	undo, err = primitive.SetupNewServer(input.ServerName, input.AnalyticsRelease, input.AnalyticsEnvironment)
	if err != nil {
		log.Errorf("Failed to create analytics server: %s", err)
		undoStack.UndoAll()
		return false
	}
	undoStack.Push(undo)

	serverPortFrom := uint16(8888)
	serverPortTo := uint16(8950)
	serverPort := primitive.FindNextFreePortFromTo(serverPortFrom, serverPortTo)
	if serverPort == 0 {
		log.Errorf("Failed to find a free server port in the range %d-%d", serverPortFrom, serverPortTo)
		undoStack.UndoAll()
		return false
	}

	log.Debugf("Calculating memory for java instance...")
	memory, err := primitive.GetMemoryForJavaInstanceInMB(1, false)
	if err != nil {
		log.Errorf("Failed to calculate memory for java instance: %s", err)
		return false
	}

	log.Infof("Updating env.sh for server instance %s...", input.ServerName)
	undo, err = primitive.SetEnvShValues(input.ServerName, map[string]string{
		"DEPLOY_TO":              input.ServerName,
		"MEMORY":                 strconv.FormatUint(memory, 10) + "m",
		"USE_ENVIRONMENT":        input.AnalyticsEnvironment,
		"MONGODB_NAME":           common.HostnameToMasterDbName(input.ServerName),
		"SERVER_NAME":            input.ServerName,
		"REPLICATION_CHANNEL":    input.ServerName,
		"TELNET_PORT":            strconv.FormatUint(uint64(primitive.FindNextFreePortFrom(14888)), 10),
		"SERVER_PORT":            strconv.FormatUint(uint64(serverPort), 10),
		"INSTANCE_NAME":          input.AwsInstanceName,
		"INSTANCE_IPV4":          input.ExternalIp,
		"INSTANCE_INTERNAL_IPV4": input.InternalIp,
		"INSTANCE_DNS":           input.InternalHostname,
		"BUILD_COMPLETE_NOTIFY":  input.Notify,
		"SERVER_STARTUP_NOTIFY":  input.Notify,
	})
	if err != nil {
		log.Errorf("Failed to set env.sh values: %s", err)
		undoStack.UndoAll()
		return false
	}
	undoStack.Push(undo)

	log.Infof("Starting the systemd daemon %s...", sailingDaemon)
	undo, err = primitive.ControlDaemon(sailingDaemon, primitive.DaemonStart)
	if err != nil {
		log.Errorf("Failed start the sailing daemon: %s", err)
		undoStack.UndoAll()
		return false
	}
	undoStack.Push(undo)

	log.Infof("Checking for systemd daemon %s...", sailingDaemon)
	_, err = primitive.ControlDaemon(sailingDaemon, primitive.DaemonStatus)
	if err != nil {
		log.Errorf("The daemon did not start properly: %s", err)
		undoStack.UndoAll()
		return false
	}

	log.Info("Creating the events...")
	events, err := primitive.CreateEvents(serverPort, input.Events)
	if err != nil {
		log.Errorf("Failed create events: %s", err)
		undoStack.UndoAll()
		return false
	}

	log.Info("Creating apache internal-server-status config...")
	undo, err = primitive.CreateApacheStatus(input.InternalHostname)
	if err != nil {
		log.Errorf("Failed to create the apache internal-server-status config: %s", err)
		undoStack.UndoAll()
		return false
	}
	undoStack.Push(undo)

	for i, event := range events {
		domain := common.HostnameToDomain(input.Events[i].Hostname)

		log.Infof("Creating apache event config for event %s (%s) on domain %s...", event.Id, event.DisplayName, domain)
		undo, err = primitive.CreateApacheEvent(domain, event.Id, input.InternalIp)
		if err != nil {
			log.Errorf("Failed to create the apache event config: %s", err)
			undoStack.UndoAll()
			return false
		}
		undoStack.Push(undo)
	}

	log.Infof("Enabling the systemd daemon %s...", sailingDaemon)
	undo, err = primitive.ControlDaemon(sailingDaemon, primitive.DaemonEnable)
	if err != nil {
		log.Errorf("Failed enable the sailing daemon: %s", err)
		undoStack.UndoAll()
		return false
	}
	undoStack.Push(undo)

	log.Info("Reloading the apache2 daemon...")
	err = primitive.ReloadApacheDaemon()
	if err != nil {
		log.Errorf("Failed to reload apache: %s", err)
		undoStack.UndoAll()
		return false
	}

	return true
}
