package scenario

import (
	log "github.com/sirupsen/logrus"
	"sapsailing.com/common"
	"strings"
)

func Invoke(input *common.AgentInput) bool {
	switch strings.ToLower(input.Scenario) {
	case "master":
		return Master(input)
	default:
		log.Errorf("Unknown scenario: %s", input.Scenario)
		return false
	}
}
