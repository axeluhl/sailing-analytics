package aws

import (
	"fmt"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/cloudwatch"
	"github.com/aws/aws-sdk-go/service/elbv2"
	log "github.com/sirupsen/logrus"
	"sapsailing.com/orchestrator/configuration"
)

func ConstructNameForAlarm(metricName string, targetGroupName string, nameSuffix string) string {
	if nameSuffix != "" {
		return metricName + "_" + targetGroupName + "_" + nameSuffix
	} else {
		return metricName + "_" + targetGroupName
	}
}

func newDefaultElbUnhealthyHostsAlarm(svc *cloudwatch.CloudWatch, tg *elbv2.TargetGroup, nameSuffix string) (string, error) {
	awsConfig, err := configuration.LoadAWSConfig()
	if err != nil {
		return "", err
	}
	if awsConfig.SnsTopicARN == "" {
		return "", fmt.Errorf("error, SnsTopicArn was not found, maybe check config")
	}

	sess, err := NewSession()
	if err != nil {
		return "", err
	}

	elbSvc := elbv2.New(sess)

	//get lb
	lb, err := GetLbFromName(elbSvc, awsConfig.ElbName)

	//concat alarm name
	alarmName := ConstructNameForAlarm(MetricUnHealthyHostCount, *tg.TargetGroupName, nameSuffix)

	if err != nil {
		log.Warningf("could not get lb with name %s", awsConfig.ElbName)
		return "", err
	}
	_, err = svc.PutMetricAlarm(&cloudwatch.PutMetricAlarmInput{
		AlarmName:          aws.String(alarmName),
		ComparisonOperator: aws.String(cloudwatch.ComparisonOperatorGreaterThanOrEqualToThreshold),
		EvaluationPeriods:  aws.Int64(3),
		MetricName:         aws.String(MetricUnHealthyHostCount),
		Namespace:          aws.String("AWS/ApplicationELB"),
		Period:             aws.Int64(60),
		Statistic:          aws.String(cloudwatch.StatisticAverage),
		Threshold:          aws.Float64(1.0),
		ActionsEnabled:     aws.Bool(true),
		AlarmDescription:   aws.String("Alarm when UnHealthyHostCount exceeds one for three data points"),
		Unit:               aws.String(cloudwatch.StandardUnitSeconds),
		TreatMissingData:   aws.String("missing"),

		// Send out a notification through SNS
		AlarmActions: []*string{
			aws.String(awsConfig.SnsTopicARN),
		},
		OKActions: []*string{
			aws.String(awsConfig.SnsTopicARN),
		},

		Dimensions: []*cloudwatch.Dimension{
			{
				Name:  aws.String("Load­Balancer"),
				Value: aws.String(*lb.LoadBalancerArn),
			},
			{
				Name:  aws.String("Target­Group"),
				Value: aws.String(*tg.TargetGroupArn),
			},
		},
	})
	if err != nil {
		log.Warningf("failed PutMetricAlarm with name %s: %s", alarmName, err)
		return "", err
	}
	return alarmName, nil
}

func enableAlarm(svc *cloudwatch.CloudWatch, tg *elbv2.TargetGroup, nameSuffix string) error {
	name, err := newDefaultElbUnhealthyHostsAlarm(svc, tg, nameSuffix)
	if err != nil {
		log.Warningf("could not create default alarm: %s", err)
		return err
	}
	_, err = svc.EnableAlarmActions(&cloudwatch.EnableAlarmActionsInput{
		AlarmNames: []*string{
			aws.String(name),
		},
	})
	if err != nil {
		log.Warningf("could not enable alarm: %s", err)
		return err
	}
	return nil
}

func deleteAlarm(svc *cloudwatch.CloudWatch, name string) error {
	_, err := svc.DeleteAlarms(&cloudwatch.DeleteAlarmsInput{
		AlarmNames: []*string{
			aws.String(name),
		},
	})
	if err != nil {
		log.Warningf("could not delete alarm with name %s", name)
		return err
	}
	log.Debugf("successfully deleted alarm with name %s", name)
	return nil
}

func CreateDefaultTgAlarm(tg *elbv2.TargetGroup, nameSuffix string) error {
	sess, err := NewSession()
	if err != nil {
		return err
	}
	svc := cloudwatch.New(sess)
	err = enableAlarm(svc, tg, nameSuffix)
	if err != nil {
		log.Warningf("could not enable alarm for target group %s: %s", string(*tg.TargetGroupArn), err)
		return err
	}

	log.Debugf("successfully added alarm for target group %s", string(*tg.TargetGroupName))
	return nil
}

func DeleteDefaultLbAlarm(tg *elbv2.TargetGroup, nameSuffix string) error {
	sess, err := NewSession()
	if err != nil {
		return err
	}

	svc := cloudwatch.New(sess)

	alarmName := ConstructNameForAlarm(MetricUnHealthyHostCount, *tg.TargetGroupName, nameSuffix)
	err = deleteAlarm(svc, alarmName)
	if err != nil {
		return err
	}
	return nil
}
