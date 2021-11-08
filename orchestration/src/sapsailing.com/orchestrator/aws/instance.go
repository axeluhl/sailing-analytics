package aws

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io/ioutil"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
	"sapsailing.com/orchestrator/configuration"

	log "github.com/sirupsen/logrus"
	"sapsailing.com/common"
)

func ConstructInstanceTagName(name string, role string) string {
	return InstanceNamePrefix + " " + name + " " + role
}

func NewSession() (*session.Session, error) {
	awsConfig, err := configuration.LoadAWSConfig()
	if err != nil {
		return nil, err
	}

	sess, err := session.NewSession(&aws.Config{
		Region: aws.String(awsConfig.DefaultRegion),
	})

	if err != nil {
		log.Warningf("failed to get aws session: %s", err)
		return nil, err
	}

	return sess, nil
}

func LaunchTemplate(template *Template, events []common.EventSpec, role InstanceRole, dryRun bool) ([]Description, error) {
	sess, err := NewSession()

	if err != nil {
		return nil, err
	}

	svc := ec2.New(sess)

	//Build tags

	var groups []*string = nil
	if len(template.SecurityGroups) > 0 {
		groups = aws.StringSlice(template.SecurityGroups)
	}

	tags := template.Tags
	tags[InstanceTagRole] = string(role)

	//use first event as primary server name
	tags[InstanceTagName] = ConstructInstanceTagName(events[0].DisplayName, string(role))
	if len(events) == 1 {
		tags[TagEventName] = events[0].DisplayName
		tags[TagHostName] = events[0].Hostname
	} else {
		for _, event := range events {
			tags[TagEventName] = fmt.Sprintf("%s,%s", tags[TagEventName], event)
			tags[TagHostName] = fmt.Sprintf("%s,%s", tags[TagHostName], event)
		}
	}

	tagSpec := ec2.TagSpecification{
		ResourceType: aws.String("instance"),
		Tags:         TagsMapToAws(tags),
	}

	//Build instance
	runInstanceInput := &ec2.RunInstancesInput{
		DryRun:       aws.Bool(dryRun),
		ImageId:      aws.String(template.Image),
		InstanceType: aws.String(template.InstanceType),
		SubnetId:     aws.String(template.Subnet),

		MinCount:          aws.Int64(template.Count),
		MaxCount:          aws.Int64(template.Count),
		SecurityGroupIds:  groups,
		UserData:          aws.String(base64.StdEncoding.EncodeToString([]byte(template.UserData))),
		TagSpecifications: []*ec2.TagSpecification{&tagSpec},
	}

	log.Infof("launching instance with details: %s", runInstanceInput)
	reservation, err := svc.RunInstances(runInstanceInput)
	if err != nil {
		return nil, err
	}

	return AllDescriptionsFromReservation(reservation), nil
}

func makeDescribeInput(id string, dryRun bool) *ec2.DescribeInstancesInput {
	return &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(id),
		},
		DryRun: aws.Bool(dryRun),
	}
}

func makeDescribeStatusInput(id string, dryRun bool) *ec2.DescribeInstanceStatusInput {
	return &ec2.DescribeInstanceStatusInput{
		InstanceIds: []*string{
			aws.String(id),
		},
		DryRun: aws.Bool(dryRun),
	}
}

func waitUntilInstanceRunning(svc *ec2.EC2, id string, dryRun bool) error {
	input := makeDescribeInput(id, dryRun)
	err := svc.WaitUntilInstanceExists(input)
	if err != nil {
		return err
	}

	return svc.WaitUntilInstanceRunning(input)
}

func waitUntilInstanceStatusOk(svc *ec2.EC2, id string, dryRun bool) error {
	input := makeDescribeStatusInput(id, dryRun)
	err := svc.WaitUntilInstanceStatusOk(input)
	if err != nil {
		return err
	}
	return svc.WaitUntilInstanceStatusOk(input)
}

func WaitUntilInstanceUsable(id string, dryRun bool) error {
	sess, err := NewSession()
	if err != nil {
		return err
	}

	svc := ec2.New(sess)

	err = waitUntilInstanceRunning(svc, id, dryRun)
	if err != nil {
		return err
	}

	err = waitUntilInstanceStatusOk(svc, id, dryRun)
	if err != nil {
		return err
	}

	return nil
}

func DescribeInstance(id string, dryRun bool) (*Description, error) {
	log.Debugf("trying to describe instance %s", id)

	sess, err := NewSession()
	if err != nil {
		return nil, err
	}
	svc := ec2.New(sess)

	//Get Filter criteria
	params := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(id),
		},
		DryRun: aws.Bool(dryRun),
	}
	//Describe instance
	descriptionResult, err := svc.DescribeInstances(params)
	if err != nil {
		log.Warningf("requested instance %s could not be described", id)
		return nil, err
	}

	descriptions := AllDescriptionsFromReservation(descriptionResult.Reservations[0])

	// lookup by ID returns exactly on result
	return &descriptions[0], nil
}

func TemplateFromFile(filename string, userData map[string]string) (*Template, error) {
	data, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	template := &Template{}

	err = json.Unmarshal(data, &template)
	if err != nil {
		return nil, err
	}

	for key, value := range userData {
		template.UserData += key + "=" + value + "\n"
	}

	return template, nil
}

func EditTags(instanceId string, tags map[string]string, dryRun bool) (*ec2.CreateTagsOutput, error) {
	sess, err := NewSession()
	if err != nil {
		return nil, err
	}

	svc := ec2.New(sess)

	awsTags := ec2.CreateTagsInput{
		DryRun:    aws.Bool(dryRun),
		Resources: []*string{&instanceId},
		Tags:      TagsMapToAws(tags),
	}

	return svc.CreateTags(&awsTags)
}
