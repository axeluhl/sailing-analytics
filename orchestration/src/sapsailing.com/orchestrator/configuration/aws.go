package configuration

import (
	log "github.com/sirupsen/logrus"
	"os"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/paths"
)

type ConfigAws struct {
	KeyId         string `json:"AWS_ACCESS_KEY_ID"`
	AccessKey     string `json:"AWS_SECRET_ACCESS_KEY"`
	DefaultRegion string `json:"AWS_DEFAULT_REGION"`
	ElbName       string `json:"AWS_ELB_NAME"`
	SnsTopicARN   string `json:"AWS_SNS_TOPIC_ARN"`
}

func LoadAWSConfig() (*ConfigAws, error) {

	config := &ConfigAws{}
	err := common.LoadFromFile(paths.GetAwsConfig(), config)

	if err != nil {
		log.Infof("error loading conf_aws.json file, %s", err)
		return nil, err
	}

	//make sure AWS keys are set
	os.Setenv("AWS_ACCESS_KEY_ID", config.KeyId)
	os.Setenv("AWS_SECRET_ACCESS_KEY", config.AccessKey)

	return config, nil
}
