package main

import (
	log "github.com/sirupsen/logrus"
	"os"
	"sapsailing.com/agent/scenario"
	"sapsailing.com/common"
)

func main() {
	log.SetFormatter(&common.SimpleFormatter{Source: "Agent"})

	log.Info("Started!")

	input := &common.AgentInput{}
	var err error
	if len(os.Args) > 1 {
		confPath := os.Args[1]
		log.Debugf("Loading input from config: %s", confPath)
		err = common.LoadFromFile(confPath, input)
	} else {
		log.Debugf("Loading input from stdin")
		err = common.LoadFromReader(os.Stdin, input)
	}
	if err != nil {
		log.Errorf("Error while reading configuration: %s", err)
		os.Exit(1)
	}

	log.Debugf("Input: %s", *input)
	success := scenario.Invoke(input)

	if !success {
		os.Exit(1)
	}
}
