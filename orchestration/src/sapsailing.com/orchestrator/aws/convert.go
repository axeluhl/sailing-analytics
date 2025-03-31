package aws

import (
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ec2"
	"github.com/sirupsen/logrus"
	"strings"
)

func StatusFromAwsName(s *string) InstanceStatus {
	if s == nil {
		logrus.Warn("No status given", *s)
		return InstanceStatusUnknown
	}
	switch strings.ToUpper(*s) {
	case string(InstanceStatusPassed):
		return InstanceStatusPassed
	case string(InstanceStatusFailed):
		return InstanceStatusFailed
	case string(InstanceStatusRunning):
		return InstanceStatusRunning
	case string(InstanceStatusInitializing):
		return InstanceStatusInitializing
	case string(InstanceStatusInsufficientData):
		return InstanceStatusInsufficientData
	case string(InstanceStatusPending):
		return InstanceStatusPending
	case string(InstanceStatusShuttingDown):
		return InstanceStatusShuttingDown
	case string(InstanceStatusTerminated):
		return InstanceStatusTerminated
	case string(InstanceStatusStopping):
		return InstanceStatusStopping
	case string(InstanceStatusStopped):
		return InstanceStatusStopped
	default:
		logrus.Warn("Unknown status: %s", *s)
		return InstanceStatusUnknown
	}
}

func (s Status) EitherStatus() InstanceStatus {
	if s.Instance == InstanceStatusUnknown {
		return s.System
	} else {
		return s.Instance
	}
}

func TagsMapFromAws(tags []*ec2.Tag) map[string]string {
	tagMap := map[string]string{}
	for _, e := range tags {
		tagMap[strings.ToLower(aws.StringValue(e.Key))] = aws.StringValue(e.Value)
	}
	return tagMap
}

func TagsMapToAws(tagMap map[string]string) []*ec2.Tag {
	var tags []*ec2.Tag
	for name, value := range tagMap {
		tags = append(tags, &ec2.Tag{Key: aws.String(name), Value: aws.String(value)})
	}
	return tags
}

func awsInstanceToDescription(instance *ec2.Instance) Description {
	return Description{
		Id:             aws.StringValue(instance.InstanceId),
		VpcId:          aws.StringValue(instance.VpcId),
		SubnetId:       aws.StringValue(instance.SubnetId),
		PrivateIp:      aws.StringValue(instance.PrivateIpAddress),
		PublicIp:       aws.StringValue(instance.PublicIpAddress),
		PublicHostname: aws.StringValue(instance.PublicDnsName),
		Tags:           TagsMapFromAws(instance.Tags),
	}
}

func awsStatusToStatus(status *ec2.InstanceStatus) Status {
	return Status{
		Instance: StatusFromAwsName(status.InstanceStatus.Status),
		System:   StatusFromAwsName(status.SystemStatus.Status),
	}
}

func AllDescriptionsFromReservation(res *ec2.Reservation) []Description {
	if res == nil {
		return []Description{}
	}
	descs := make([]Description, len(res.Instances))
	for i, instance := range res.Instances {
		descs[i] = awsInstanceToDescription(instance)
	}

	return descs
}

func AllStatusesFromInstanceStatus(statuses []*ec2.InstanceStatus) []Status {
	if statuses == nil {
		return []Status{}
	}
	outStatuses := make([]Status, len(statuses))
	for i, status := range statuses {
		outStatuses[i] = awsStatusToStatus(status)
	}
	return outStatuses
}

func InstanceRoleFromString(s string) InstanceRole {
	switch strings.ToLower(s) {
	case string(RoleSingleMaster):
		return RoleSingleMaster
	case string(RoleMultiMaster):
		return RoleMultiMaster
	case string(RoleSingleReplica):
		return RoleSingleReplica
	default:
		return RoleUnknown
	}
}

func TargetGroupRoleFromString(s string) TargetGroupRole {
	switch strings.ToLower(s) {
	case string(RolesMaster):
		return RolesMaster
	case string(RolesDefault):
		return RolesDefault
	default:
		return RolesDefault
	}
}
