package aws

import (
	"math/rand"
	"testing"
	"time"

	"github.com/aws/aws-sdk-go/service/elbv2"
	"github.com/stretchr/testify/require"
	"sapsailing.com/common"
)

func init() {
	rand.Seed(time.Now().UnixNano())
}

func TestCreateDeleteAlarm(t *testing.T) {
	var event common.EventSpec
	event.Hostname = common.RandStringRunes(5)
	event.DisplayName = common.RandStringRunes(5)
	var alarmRandom = common.RandStringRunes(5)

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
		VpcId:    "vpc-14b3477f",
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

	//create alarm
	err = CreateDefaultTgAlarm(out, "testAlarm"+alarmRandom)
	require.NoError(t, err)

	//delete alarm
	err = DeleteDefaultLbAlarm(out, "testAlarm"+alarmRandom)
	require.NoError(t, err)

	//delete tg
	require.NoError(t, DeleteTargetGroup(svc, *out.TargetGroupArn))

}
