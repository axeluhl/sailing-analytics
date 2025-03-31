package aws

import (
	"testing"

	"sapsailing.com/orchestrator/configuration"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/paths"
)

func createData() (map[string]string, *Template, map[string]string) {
	tags := map[string]string{
		"k": "v",
	}

	awsConfig, _ := configuration.LoadAWSConfig()

	//Prepare request to get SAPSailing imageId
	sess := session.Must(session.NewSession(&aws.Config{
		Region: aws.String(awsConfig.DefaultRegion),
	}))

	svc := ec2.New(sess)

	filter := &ec2.Filter{
		Name:   aws.String("name"),
		Values: []*string{aws.String("SAP Sailing*")}, //TODO get latest via tag Version and Role
	}

	imageInput := &ec2.DescribeImagesInput{
		Filters: []*ec2.Filter{filter},
	}

	images, _ := svc.DescribeImages(imageInput)

	imageId := images.Images[0].ImageId

	instance := Template{
		Image:        *imageId,
		InstanceType: "t2.micro",
		Count:        1,
		Tags:         tags,
	}

	userData := map[string]string{
		"Key1": "Value1",
		"Key2": "Value2",
	}

	return tags, &instance, userData
}

func TestInstanceLaunch(t *testing.T) {
	_, template, _ := createData()
	_, err := LaunchTemplate(template, []common.EventSpec{{Hostname: "test", DisplayName: "test"}}, RoleSingleMaster, true)

	assert.Contains(t, err.Error(), "Request would have succeeded", "Should not fail in dry run")
}

func TestInstanceEditTags(t *testing.T) {
	tags, _, _ := createData()
	_, err := EditTags("", tags, true)

	assert.Contains(t, err.Error(), "Request would have succeeded", "Should not fail in dry run")
}

func TestInstanceConstruction(t *testing.T) {
	_, _, userData := createData()
	instance, err := TemplateFromFile(paths.GetInstanceTemplate("master"), userData)
	assert.NoError(t, err, "Reading the config should not fail.")

	assert.Equal(t, int64(1), instance.Count)
}

func TestDescribeInstance(t *testing.T) {
	desc, err := DescribeInstance("123", true)
	require.Error(t, err, "Describe dryrun must return an error")
	assert.Contains(t, err.Error(), "Request would have succeeded", "Should not fail in dry run")

	assert.Nil(t, desc)
}

//TODO: more extensive testing of Describe()
