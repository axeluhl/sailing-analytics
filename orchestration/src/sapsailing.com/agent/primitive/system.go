package primitive

import (
	"fmt"
	"github.com/sirupsen/logrus"
	"sapsailing.com/common"
)

type DaemonAction string

const (
	DaemonStart   DaemonAction = "start"
	DaemonStop    DaemonAction = "stop"
	DaemonEnable  DaemonAction = "enable"
	DaemonDisable DaemonAction = "disable"
	DaemonRestart DaemonAction = "restart"
	DaemonReload  DaemonAction = "reload"
	DaemonStatus  DaemonAction = "status"
)

func ControlDaemon(daemon string, action DaemonAction) (common.UndoFunction, error) {
	success, output, err := common.RunCommandSimple("sudo", "systemctl", string(action), daemon)
	if !success {
		return nil, fmt.Errorf("failed to %s daemon %s: err=%s, output=%s", action, daemon, err, output)
	}
	return func() {
		var newAction DaemonAction
		switch action {
		case DaemonStop:
			newAction = DaemonStart
		case DaemonStart:
			newAction = DaemonStop
		case DaemonDisable:
			newAction = DaemonEnable
		case DaemonEnable:
			newAction = DaemonDisable
		}
		if newAction != "" {
			logrus.Warnf("Rolling back service action %s by doing %s...", action, newAction)
			ControlDaemon(daemon, action)
		}
	}, nil
}

func SailingDaemon(name string) string {
	return "sailing@" + name
}
