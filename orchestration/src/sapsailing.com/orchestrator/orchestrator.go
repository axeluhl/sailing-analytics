package main

import (
	log "github.com/sirupsen/logrus"
	"os"
	"sapsailing.com/common"
	"sapsailing.com/common/cmd"
)

func main() {
	log.SetFormatter(&common.SimpleFormatter{Source: "Orchestrator"})
	commands := cmd.CommandMap{
		"master": cmd.CommandSpec{
			Help:    "Creates a master instance",
			Factory: NewInstanceCommand,
		},
		"agent-only": cmd.CommandSpec{
			Help:    "Executes agent on system",
			Factory: NewAgentOnlyCommand,
		},
		"version": cmd.CommandSpec{
			Help:    "Displays the version",
			Factory: NewVersionCommand,
		},
	}
	os.Exit(int(cmd.DefaultMain(commands)))
}
