package aws

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws/awserr"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/configuration"
	"sort"
	"strconv"
	"time"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ec2"
	"github.com/aws/aws-sdk-go/service/elbv2"
	log "github.com/sirupsen/logrus"
)

func ConstructNameForTargetGroup(role TargetGroupRole, event common.EventSpec) string {
	if role == RolesMaster {
		return TargetGroupNamePrefix + "-" + event.Hostname + "-" + string(role)
	} else {
		return TargetGroupNamePrefix + "-" + event.Hostname
	}

}

func ensureTargetGroup(svc *elbv2.ELBV2, vpcId string, event common.EventSpec, role TargetGroupRole) (*elbv2.TargetGroup, error) {
	tgName := ConstructNameForTargetGroup(role, event)
	tg, err := GetTargetGroup(svc, tgName)
	if err != nil {
		log.Warningf("could not get target group: %s", err)
		return nil, err
	}

	if len(tg) == 1 {
		log.Debugf("target group found %s", string(*tg[0].TargetGroupArn))
		return tg[0], nil
	}

	config := &TargetGroupConfig{
		Name:     tgName,
		Port:     443,
		Protocol: "HTTPS",
		VpcId:    vpcId,
		Tags: targetGroupTags{
			HostName: event.Hostname,
			Role:     role,
		},
		HealthCheck: targetGroupHealthCheck{
			Protocol: "HTTPS",
			Path:     "/index.html",
			Port:     443,
			HealthyThresholdCount:   2,
			UnhealthyThresholdCount: 2,
			Timeout:                 4,
			Interval:                5,
			SuccessCode:             "200",
		},
	}

	out, err := createTargetGroup(svc, config)
	if err != nil {
		log.Warningf("could not create new target %s group: %s", config.Name, err)
		return nil, err
	}

	log.Debugf("successfully created new target group %s", config.Name)
	return out, nil
}

func GetTargetGroup(svc *elbv2.ELBV2, name string) ([]*elbv2.TargetGroup, error) {
	describeInput := &elbv2.DescribeTargetGroupsInput{
		Names: []*string{aws.String(name)},
	}

	log.Debugf("searching a target group named %s", name)
	describeOutput, err := svc.DescribeTargetGroups(describeInput)
	if err != nil {
		if aerr, ok := err.(awserr.Error); ok {
			switch aerr.Code() {
			case elbv2.ErrCodeTargetGroupNotFoundException:
				return nil, nil
			default:
				log.Warningf("could not describe target groups: %s", aerr.Error())
				return nil, err
			}
		}

	}

	return describeOutput.TargetGroups, nil
}

func createTargetGroup(svc *elbv2.ELBV2, config *TargetGroupConfig) (*elbv2.TargetGroup, error) {
	createInput := &elbv2.CreateTargetGroupInput{
		Name:     aws.String(config.Name),
		Port:     aws.Int64(int64(config.Port)),
		Protocol: aws.String(config.Protocol),
		VpcId:    aws.String(config.VpcId),
		HealthCheckIntervalSeconds: aws.Int64(config.HealthCheck.Interval),
		HealthCheckPath:            aws.String(config.HealthCheck.Path),
		HealthCheckPort:            aws.String(fmt.Sprintf("%d", config.HealthCheck.Port)),
		HealthCheckProtocol:        aws.String(config.HealthCheck.Protocol),
		HealthCheckTimeoutSeconds:  aws.Int64(config.HealthCheck.Timeout),
		HealthyThresholdCount:      aws.Int64(config.HealthCheck.HealthyThresholdCount),
		UnhealthyThresholdCount:    aws.Int64(config.HealthCheck.UnhealthyThresholdCount),
		Matcher:                    &elbv2.Matcher{HttpCode: aws.String(config.HealthCheck.SuccessCode)},
	}

	//create the new target group
	out, err := svc.CreateTargetGroup(createInput)
	if err != nil {
		log.Warningf("new target group %s could not be created", config.Name)
		return nil, err
	}

	//set tags to target group
	err1 := setTargetGroupTags(svc, out.TargetGroups[0], &config.Tags)
	if err1 != nil {
		log.Warningf("tags for target group %s could not be added", config.Name)
		return nil, err1
	}

	//set attributes to target group
	_, err2 := modifyTargetGroupAttribute(svc, *out.TargetGroups[0].TargetGroupArn)
	if err2 != nil {
		log.Warningf("attributes for target group %s could not be modified", config.Name)
	}

	return out.TargetGroups[0], nil
}

func setTargetGroupTags(svc *elbv2.ELBV2, tg *elbv2.TargetGroup, tags *targetGroupTags) error {
	input := &elbv2.AddTagsInput{
		ResourceArns: []*string{
			aws.String(*tg.TargetGroupArn),
		},
		Tags: []*elbv2.Tag{
			{
				Key:   aws.String(TagHostName),
				Value: aws.String(tags.HostName),
			},
			{
				Key:   aws.String(TargetGroupTagRole),
				Value: aws.String(string(tags.Role)),
			},
		},
	}

	_, err := svc.AddTags(input)
	if err != nil {
		return err
	}

	return nil
}

func getTagsOfTargetGroup(svc *elbv2.ELBV2, tg *elbv2.TargetGroup) (map[string]string, error) {
	input := &elbv2.DescribeTagsInput{
		ResourceArns: aws.StringSlice([]string{*tg.TargetGroupArn}),
	}

	res, err := svc.DescribeTags(input)
	if err != nil {
		return nil, err
	}

	tags := map[string]string{}
	if len(res.TagDescriptions) == 0 {
		return tags, nil
	}
	for _, t := range res.TagDescriptions[0].Tags {
		tags[*t.Key] = *t.Value
	}

	return tags, nil
}

func getEventOfTargetGroup(svc *elbv2.ELBV2, tg *elbv2.TargetGroup) (common.EventSpec, error) {
	var event common.EventSpec

	tags, err := getTagsOfTargetGroup(svc, tg)
	if err != nil {
		log.Warningf("could not get tags of target group %s: %s", string(*tg.TargetGroupArn), err)
		return event, err
	}

	for idx, tag := range tags {
		if idx == TagHostName {
			event.Hostname = tag
		}
	}

	if event.Hostname != "" {
		return event, nil
	} else {
		return event, fmt.Errorf("required tags %s seem to be empty", TagHostName)
	}
}

func getRoleOfTargetGroup(svc *elbv2.ELBV2, tg *elbv2.TargetGroup) (TargetGroupRole, error) {
	var role TargetGroupRole

	tags, err := getTagsOfTargetGroup(svc, tg)
	if err != nil {
		log.Warningf("could not get tags of target group %s: %s", string(*tg.TargetGroupArn), err)
		return "", err
	}

	for idx, tag := range tags {
		if idx == TargetGroupTagRole {
			role = TargetGroupRoleFromString(tag)
		}
	}

	if role != "" {
		return role, nil
	} else {
		return "", fmt.Errorf("required tag %s seem to be empty", TargetGroupTagRole)
	}
}

func modifyTargetGroupAttribute(svc *elbv2.ELBV2, arn string) (*elbv2.ModifyTargetGroupAttributesOutput, error) {
	input := &elbv2.ModifyTargetGroupAttributesInput{
		Attributes: []*elbv2.TargetGroupAttribute{
			{
				Key:   aws.String("deregistration_delay.timeout_seconds"),
				Value: aws.String("30"),
			},
		},
		TargetGroupArn: aws.String(arn),
	}

	return svc.ModifyTargetGroupAttributes(input)
}

func getListenersFromLb(svc *elbv2.ELBV2, lb *elbv2.LoadBalancer) ([]*elbv2.Listener, error) {
	input := &elbv2.DescribeListenersInput{
		LoadBalancerArn: aws.String(*lb.LoadBalancerArn),
	}

	result, err := svc.DescribeListeners(input)
	if err != nil {
		log.Warningf("error with DescribeListeners: %s", err)
		return nil, err
	}

	log.Debugf("found listeners for lb %s", string(*lb.LoadBalancerArn))
	return result.Listeners, nil
}

func GetLbFromName(svc *elbv2.ELBV2, name string) (*elbv2.LoadBalancer, error) {
	input := &elbv2.DescribeLoadBalancersInput{
		Names: []*string{aws.String(name)},
	}

	result, err := svc.DescribeLoadBalancers(input)
	if err != nil {
		log.Warningf("could not describe lb with name %s", name)
		return nil, err
	}

	log.Debugf("found lb with arn %s", string(*result.LoadBalancers[0].LoadBalancerArn))
	return result.LoadBalancers[0], nil
}

func getRulesFromListener(svc *elbv2.ELBV2, listener *elbv2.Listener) ([]*elbv2.Rule, error) {
	input := &elbv2.DescribeRulesInput{
		ListenerArn: aws.String(string(*listener.ListenerArn)),
	}

	result, err := svc.DescribeRules(input)
	if err != nil {
		log.Warningf("could not describe rules for listener %s: err", string(*listener.ListenerArn), err)
		return nil, err
	}
	return result.Rules, nil
}

func getRuleForHostname(svc *elbv2.ELBV2, listener *elbv2.Listener, event common.EventSpec) (*elbv2.Rule, error) {
	rules, err := getRulesFromListener(svc, listener)
	if err != nil {
		return nil, err
	}

	if len(rules) == 0 {
		log.Warningf("no rules for listener %s found", string(*listener.ListenerArn))
		return nil, nil
	}

	domain := common.HostnameToDomain(event.Hostname)

	for _, rule := range rules {
		//default rule does not have values...
		if len(rule.Conditions) > 0 {
			if rule.Conditions[0].Values[0] == &domain {
				return rule, nil
			}
		}
	}

	return nil, fmt.Errorf("could not find rule for hostname %s in listener %s", event.Hostname, *listener.LoadBalancerArn)
}

func GetRuleForEvent(svc *elbv2.ELBV2, listener *elbv2.Listener, event common.EventSpec) (*elbv2.Rule, error) {
	out, err := getRuleForHostname(svc, listener, event)
	if err != nil {
		log.Warningf("could not find rule for event %s", event.Hostname)
		return nil, err
	}
	log.Debugf("found rule %s for event %s", string(*out.RuleArn), event.Hostname)
	return out, nil
}

func DeleteTargetGroup(svc *elbv2.ELBV2, arn string) error {
	input := &elbv2.DeleteTargetGroupInput{
		TargetGroupArn: aws.String(arn),
	}

	_, err := svc.DeleteTargetGroup(input)
	if err != nil {
		log.Warningf("could not delete target group with arn %s", arn)
		return err
	}

	log.Debugf("successfully deleted target group %s", arn)
	return nil
}

func AddTargetToGroup(event common.EventSpec, role TargetGroupRole, target Description) (*elbv2.TargetGroup, error) {
	sess, err := NewSession()
	if err != nil {
		return nil, err
	}

	svc := elbv2.New(sess)

	out, err := ensureTargetGroup(svc, target.VpcId, event, role)
	if err != nil {
		log.Warningf("could not ensure target group for event %s", event.Hostname)
		return nil, err
	}

	input := &elbv2.RegisterTargetsInput{
		TargetGroupArn: aws.String(*out.TargetGroupArn),
		Targets: []*elbv2.TargetDescription{
			{
				Id:   aws.String(target.Id),
				Port: aws.Int64(int64(ApachePort)),
			},
		},
	}

	_, err = svc.RegisterTargets(input)
	if err != nil {
		log.Warningf("could not register target %s to target group %s", target.Id, string(*out.TargetGroupName))
	} else {
		log.Debugf("successfully added target %s to target group %s", target.Id, string(*out.TargetGroupName))
	}

	tg, err := GetTargetGroup(svc, ConstructNameForTargetGroup(role, event))
	if err != nil {
		log.Warningf("could not get the just added target group: %s", err)
	}

	return tg[0], nil
}

func RemoveTargetFromTargetGroup(target *ec2.Instance, tg *elbv2.TargetGroup) (*elbv2.DeregisterTargetsOutput, error) {
	sess, err := NewSession()
	if err != nil {
		return nil, err
	}

	svc := elbv2.New(sess)

	input := &elbv2.DeregisterTargetsInput{
		TargetGroupArn: aws.String(*tg.TargetGroupArn),
		Targets: []*elbv2.TargetDescription{
			{
				Id: aws.String(*target.InstanceId),
			},
		},
	}

	result, err := svc.DeregisterTargets(input)
	if err != nil {
		log.Warningf("could not de-register target %s from lb %s", string(*target.InstanceId), string(*tg.TargetGroupArn))
		return nil, err
	}

	log.Debugf("successfully removed target %s from target group %s", string(*target.InstanceId), string(*tg.TargetGroupArn))
	return result, nil
}

func getHighestPriorityFromListener(svc *elbv2.ELBV2, listener *elbv2.Listener) (int64, error) {
	var priorities []int

	rules, err := getRulesFromListener(svc, listener)
	if err != nil {
		log.Warningf("could not get rules from listener %s", string(*listener.ListenerArn))
		return 0, err
	}

	for _, rule := range rules {
		priority, _ := strconv.Atoi(aws.StringValue(rule.Priority))
		priorities = append(priorities, priority)
	}

	sort.Ints(priorities)

	log.Debugf("found a rule with highest priority")
	return int64(priorities[len(priorities)-1]), nil
}

func createRuleInListener(svc *elbv2.ELBV2, listener *elbv2.Listener, tg *elbv2.TargetGroup, role TargetGroupRole) (common.EventSpec, error) {
	var domain string

	event, err := getEventOfTargetGroup(svc, tg)
	if err != nil {
		log.Warningf("could not get event from target group %s: %s", string(*tg.TargetGroupArn), err)
		return event, err
	}

	highestPriority, err := getHighestPriorityFromListener(svc, listener)
	if err != nil {
		return event, err
	}
	priority := highestPriority + 1

	if role == RolesDefault {
		domain = common.HostnameToDomain(event.Hostname)
	} else if role == RolesMaster {
		domain = common.HostnameToMasterDomain(event.Hostname)
	} else {
		return event, fmt.Errorf("unknown role type %s", role)
	}

	input := &elbv2.CreateRuleInput{
		Actions: []*elbv2.Action{
			{
				TargetGroupArn: aws.String(*tg.TargetGroupArn),
				Type:           aws.String("forward"),
			},
		},
		Conditions: []*elbv2.RuleCondition{
			{
				Field: aws.String("host-header"),
				Values: []*string{
					aws.String(domain),
				},
			},
		},
		ListenerArn: aws.String(*listener.ListenerArn),
		Priority:    aws.Int64(priority),
	}

	_, err = svc.CreateRule(input)

	if err != nil {
		log.Warningf("could not create rule for event %s in listener %s", event.Hostname, string(*listener.ListenerArn))
		return event, err
	}

	log.Debugf("successfully created rule for event %s in listener %s", event.Hostname, string(*listener.ListenerArn))
	return event, nil
}

func CreateRulesInLbForTg(tg *elbv2.TargetGroup, role TargetGroupRole) (common.EventSpec, error) {
	var listener *elbv2.Listener
	var event common.EventSpec

	sess, err := NewSession()
	if err != nil {
		return event, err
	}

	svc := elbv2.New(sess)

	awsConfig, err := configuration.LoadAWSConfig()
	if err != nil {
		return event, err
	}

	lb, err := GetLbFromName(svc, awsConfig.ElbName)
	if err != nil {
		return event, err
	}

	listeners, err := getListenersFromLb(svc, lb)
	if err != nil {
		return event, err
	}

	found := false
	for _, list := range listeners {
		if aws.Int64Value(list.Port) == int64(LbListenerPort) {
			listener = list
			found = true
			log.Debugf("found a matching listener on port %d", *list.Port)
			break
		} else {
			found = false
			log.Debugf("did not find a matching listener in %s, trying next", string(*list.ListenerArn))
		}
	}

	if !found {
		return event, fmt.Errorf("could not find https(443) lb listener for %s", string(*lb.LoadBalancerArn))
	}

	_, err = createRuleInListener(svc, listener, tg, role)
	if err != nil {
		log.Warningf("could not create rule in listener: %s", err)
		return event, err
	}
	log.Debugf("successfully created rule in lb %s", string(*lb.LoadBalancerArn))
	return event, nil
}

func DeleteEventRuleFromListener(listener *elbv2.Listener, event common.EventSpec) error {
	sess, err := NewSession()
	if err != nil {
		return err
	}

	svc := elbv2.New(sess)

	arn, err := GetRuleForEvent(svc, listener, event)
	if err != nil {
		return err
	}

	input := &elbv2.DeleteRuleInput{
		RuleArn: aws.String(*arn.RuleArn),
	}

	_, err = svc.DeleteRule(input)
	if err != nil {
		log.Warningf("could not delete rule for event %s", event)
		return err
	}

	log.Debugf("successfully deleted rule %s from listener %s", string(*arn.RuleArn), string(*listener.ListenerArn))
	return nil
}

func GetMetadataForTg(tg *elbv2.TargetGroup) (string, TargetGroupRole, error) {
	var domain string

	sess, err := NewSession()
	if err != nil {
		return "", "", err
	}

	svc := elbv2.New(sess)

	event, err := getEventOfTargetGroup(svc, tg)
	if err != nil {
		log.Warningf("could not get event of target group: %s", err)
		return "", "", nil
	}

	role, err := getRoleOfTargetGroup(svc, tg)
	if err != nil {
		log.Warningf("could not get role of target group: %s", err)
		return "", "", nil
	}

	if role == RolesMaster {
		domain = common.HostnameToMasterDomain(event.Hostname)
	} else if role == RolesDefault {
		domain = common.HostnameToDomain(event.Hostname)
	} else {
		return "", "", fmt.Errorf("wrong role of target group, got %s", role)
	}

	return domain, role, nil
}

func WaitUntilTargetGroupUsable(tg *elbv2.TargetGroup, retries int) error {
	log.Debugf("waiting for a healthy target in target group %s", string(*tg.TargetGroupArn))
	for i := 1; i <= retries; i++ {
		healthy, err := isTargetGroupHealthy(tg)
		if err != nil {
			return err
		}

		if !healthy {
			time.Sleep(10 * time.Second)
		}

		if (i == retries) && !(healthy) {
			return fmt.Errorf("maximum number of retries reached, no healthy target found")
		}
	}
	return nil
}

func isTargetGroupHealthy(tg *elbv2.TargetGroup) (bool, error) {
	sess, err := NewSession()
	if err != nil {
		return false, err
	}

	svc := elbv2.New(sess)

	input := &elbv2.DescribeTargetHealthInput{
		TargetGroupArn: aws.String(*tg.TargetGroupArn),
	}

	result, err := svc.DescribeTargetHealth(input)
	if err != nil {
		log.Warningf("could not describe health of targets for target group %s: %s", string(*tg.TargetGroupArn), err)
		return false, err
	}

	//at least one target must be healthy to conclude target group is usable
	found := false
	for _, target := range result.TargetHealthDescriptions {
		if found {
			break
		}
		if aws.StringValue(target.TargetHealth.State) == TargetHealthStateHealthy {
			log.Debugf("found healthy target %s in target group %s", string(*target.Target.Id), string(*tg.TargetGroupArn))
			found = true
		} else {
			log.Debugf("found target %s in target group %s with state %s", string(*target.Target.Id), string(*tg.TargetGroupArn), string(*target.TargetHealth.State))
		}
	}
	return found, nil
}

func CreateTargetGroupsWithAlarm(events []common.EventSpec, roles []TargetGroupRole, target Description) []*elbv2.TargetGroup {
	var tgs []*elbv2.TargetGroup

	for _, event := range events {
		for _, role := range roles {
			tg, err := AddTargetToGroup(event, role, target)
			if err != nil {
				log.Errorf("Failed to add target %s to group: %s", target.Id, err)
				//TODO: could remove target group again, if created before
				continue
			}

			_, err = CreateRulesInLbForTg(tg, role)
			if err != nil {
				log.Errorf("Failed to add rule to target groups: %s", err)
				//TODO: could remove target group again, if created before
				continue
			}

			err = CreateDefaultTgAlarm(tg, "")
			if err != nil {
				log.Warningf("Failed to add cloudwatch alarm for target group %s: %s", string(*tg.TargetGroupArn), err)
			}

			tgs = append(tgs, tg)
		}
	}
	return tgs
}
