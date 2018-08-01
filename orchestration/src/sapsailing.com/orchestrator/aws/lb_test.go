package aws

import (
	"math/rand"
	"testing"
	"time"

	"github.com/aws/aws-sdk-go/service/elbv2"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/configuration"
)

func init() {
	rand.Seed(time.Now().UnixNano())
}

var vpcId = "vpc-14b3477f"

func TestCreateDeleteTargetGroup(t *testing.T) {
	var event common.EventSpec
	event.Hostname = common.RandStringRunes(5)
	event.DisplayName = common.RandStringRunes(5)

	//integration test, as the elbv2 api does not provide a dryRun option
	tgRole := RolesMaster
	tgName := ConstructNameForTargetGroup(tgRole, event)

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	config := &TargetGroupConfig{
		Name:     tgName,
		Port:     TargetGroupPort,
		Protocol: "HTTPS",
		VpcId:    vpcId,
		Tags: targetGroupTags{
			HostName: event.Hostname,
			Role:     tgRole,
		},
		HealthCheck: targetGroupHealthCheck{
			Protocol: "HTTPS",
			Path:     "/index.html",
			Port:     TargetGroupHealthCheckPort,
			HealthyThresholdCount:   2,
			UnhealthyThresholdCount: 2,
			Timeout:                 4,
			Interval:                5,
			SuccessCode:             "200",
		},
	}

	//create
	out, err := createTargetGroup(svc, config)
	require.NoError(t, err)

	//describe
	desc, err := GetTargetGroup(svc, tgName)
	require.NoError(t, err)
	assert.Equal(t, out.TargetGroupArn, desc[0].TargetGroupArn)

	//delete
	require.NoError(t, DeleteTargetGroup(svc, *out.TargetGroupArn))
}

func TestEnsureCreateDeleteTargetGroup(t *testing.T) {
	var event common.EventSpec
	event.Hostname = common.RandStringRunes(5)
	event.DisplayName = common.RandStringRunes(5)

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	//ensure created
	out, err := ensureTargetGroup(svc, vpcId, event, RolesMaster)
	assert.NoError(t, err)

	//delete
	require.NoError(t, DeleteTargetGroup(svc, *out.TargetGroupArn))
}

func TestGetLbFromName(t *testing.T) {
	awsConfig, err := configuration.LoadAWSConfig()
	require.NoError(t, err)

	if awsConfig.ElbName == "" {
		t.SkipNow()
	}

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	name, err := GetLbFromName(svc, awsConfig.ElbName)

	require.NotEmpty(t, name)
}

func TestFailGetLbFromName(t *testing.T) {
	name := "NOTEXISTING"

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	_, err = GetLbFromName(svc, name)

	assert.Error(t, err)
}

func TestGetListenersFromLb(t *testing.T) {
	awsConfig, err := configuration.LoadAWSConfig()
	require.NoError(t, err)

	if awsConfig.ElbName == "" {
		t.SkipNow()
	}

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	lb, err := GetLbFromName(svc, awsConfig.ElbName)

	listeners, err := getListenersFromLb(svc, lb)

	require.NotEmpty(t, listeners)
}

func TestFailDeleteTargetGroup(t *testing.T) {
	arn := "NOTEXISTING"
	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	err = DeleteTargetGroup(svc, arn)

	assert.Error(t, err)
}

func TestCreateRuleInLb(t *testing.T) {
	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	//just use a target group that should be always there
	tg, err := GetTargetGroup(svc, "HttpToHttpsForwarder")

	//assume that some random tg doesnt have the eventName tag set
	_, err = CreateRulesInLbForTg(tg[0], RolesDefault)
	assert.Error(t, err)
}

func TestFailDeleteEventRuleFromListener(t *testing.T) {
	awsConfig, err := configuration.LoadAWSConfig()
	require.NoError(t, err)

	if awsConfig.ElbName == "" {
		t.SkipNow()
	}

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	lb, err := GetLbFromName(svc, awsConfig.ElbName)

	listeners, err := getListenersFromLb(svc, lb)
	require.NotEmpty(t, listeners)

	var event common.EventSpec
	event.Hostname = common.RandStringRunes(5)
	event.DisplayName = common.RandStringRunes(5)

	//just take the first listener
	listener := listeners[0]
	err = DeleteEventRuleFromListener(listener, event)
	assert.Error(t, err)
}

func TestGetRulesFromListener(t *testing.T) {
	awsConfig, err := configuration.LoadAWSConfig()
	require.NoError(t, err)

	if awsConfig.ElbName == "" {
		t.SkipNow()
	}

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	lb, err := GetLbFromName(svc, awsConfig.ElbName)

	listeners, err := getListenersFromLb(svc, lb)
	require.NotEmpty(t, listeners)

	_, err = getRulesFromListener(svc, listeners[0])
	require.NoError(t, err)
}

func TestGetHighestPriorityFromListener(t *testing.T) {
	awsConfig, err := configuration.LoadAWSConfig()
	require.NoError(t, err)

	if awsConfig.ElbName == "" {
		t.SkipNow()
	}

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	lb, err := GetLbFromName(svc, awsConfig.ElbName)

	listeners, err := getListenersFromLb(svc, lb)
	require.NotEmpty(t, listeners)

	number, err := getHighestPriorityFromListener(svc, listeners[0])
	require.NoError(t, err)

	//there should be at least one rule with prio 1 in it
	assert.True(t, number > 0)
}

func TestGetMetadataForTg(t *testing.T) {
	var event common.EventSpec
	event.Hostname = common.RandStringRunes(5)
	event.DisplayName = common.RandStringRunes(5)

	sess, err := NewSession()
	require.NoError(t, err)
	svc := elbv2.New(sess)

	//ensure created
	out, err := ensureTargetGroup(svc, vpcId, event, RolesMaster)
	assert.NoError(t, err)

	//get metadata and ensure domain and role are correct
	domain, role, err := GetMetadataForTg(out)
	assert.Equal(t, domain, common.HostnameToMasterDomain(event.Hostname))
	assert.Equal(t, role, RolesMaster)

	//delete
	require.NoError(t, DeleteTargetGroup(svc, *out.TargetGroupArn))
}
