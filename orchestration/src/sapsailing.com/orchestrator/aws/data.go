package aws

type InstanceStatus string

const (
	InstanceStatusCreating         InstanceStatus = "CREATING"
	InstanceStatusConfiguring      InstanceStatus = "CONFIGURING"
	InstanceStatusPending          InstanceStatus = "PENDING"
	InstanceStatusRunning          InstanceStatus = "RUNNING"
	InstanceStatusShuttingDown     InstanceStatus = "SHUTTING-DOWN"
	InstanceStatusTerminated       InstanceStatus = "TERMINATED"
	InstanceStatusStopping         InstanceStatus = "STOPPING"
	InstanceStatusStopped          InstanceStatus = "STOPPED"
	InstanceStatusNotFound         InstanceStatus = "NOTFOUND"
	InstanceStatusNotReachable     InstanceStatus = "NOTREACHABLE"
	InstanceStatusPassed           InstanceStatus = "PASSED"
	InstanceStatusFailed           InstanceStatus = "FAILED"
	InstanceStatusInitializing     InstanceStatus = "INITIALIZING"
	InstanceStatusInsufficientData InstanceStatus = "INSUFFICIENTDATA"
	InstanceStatusUnknown          InstanceStatus = "-"
)

type InstanceRole string

const (
	RoleSingleMaster  InstanceRole = "single-master"
	RoleMultiMaster   InstanceRole = "multi-master"
	RoleSingleReplica InstanceRole = "single-replica"
	RoleUnknown       InstanceRole = "-"
)

type TargetGroupRole string

const (
	RolesDefault TargetGroupRole = "default"
	RolesMaster  TargetGroupRole = "master"
)

const (
	TargetGroupNamePrefix = "S"
	InstanceNamePrefix    = "S"
)

const (
	TargetHealthStateInitial     = "initial"
	TargetHealthStateHealthy     = "healthy"
	TargetHealthStateUnhealthy   = "unhealthy"
	TargetHealthStateUnused      = "unused"
	TargetHealthStateDraining    = "draining"
	TargetHealthStateUnavailable = "unavailable"
)

const (
	InstanceTagRole    = "instance-role"
	InstanceTagName    = "Name"
	TargetGroupTagRole = "targetgroup-role"
	TagHostName        = "hostname"
	TagEventName       = "eventname"
)

type Template struct {
	Name string

	Vpc    string
	Subnet string

	Image          string
	InstanceType   string `json:"instance-type"`
	Count          int64
	SecurityGroups []string `json:"security-groups"`
	Tags           map[string]string
	UserData       string `json:"user-data"`
}

type Status struct {
	Instance InstanceStatus
	System   InstanceStatus
}

type Description struct {
	Id               string
	VpcId            string
	SubnetId         string
	AvailibilityZone string
	PublicIp         string
	PrivateIp        string
	Tags             map[string]string
	PublicHostname   string
}

const (
	ApachePort                 uint16 = 443
	TargetGroupHealthCheckPort uint16 = 443
	TargetGroupPort            uint16 = 443
	LbListenerPort             uint16 = 443
)

const (
	RemoteAgentPath = "/tmp/sailing-agent"
)

type targetGroupTags struct {
	HostName string
	Role     TargetGroupRole
}

type targetGroupHealthCheck struct {
	Protocol                string
	Path                    string
	Port                    uint16
	HealthyThresholdCount   int64
	UnhealthyThresholdCount int64
	Timeout                 int64
	Interval                int64
	SuccessCode             string
}

type TargetGroupConfig struct {
	Name        string
	Port        uint16
	Protocol    string
	VpcId       string
	Tags        targetGroupTags
	HealthCheck targetGroupHealthCheck
}

const (
	MetricUnHealthyHostCount string = "UnHealthyHostCount"
)
