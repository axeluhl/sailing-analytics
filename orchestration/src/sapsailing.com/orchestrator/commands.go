package main

import (
	"fmt"
	log "github.com/sirupsen/logrus"
	"path"
	"sapsailing.com/common"
	"sapsailing.com/common/cmd"
	"sapsailing.com/common/sailing"
	"sapsailing.com/orchestrator/agent"
	"sapsailing.com/orchestrator/aws"
	"sapsailing.com/orchestrator/paths"
	"time"
)

type InstanceCommand struct {
	Events        []string `cmd:"event" cmd_help:"Name of the event to create and a hostname for it (format: hostname:eventname)"`
	AdminPassword string   `cmd:"admin-password" cmd_help:"New admin password to set"`
	Username      string   `cmd:"user-name" cmd_help:"New user to create"`
	Password      string   `cmd:"user-password" cmd_help:"Password for the new user"`
	Template      string   `cmd:"template-file" cmd_help:"template" cmd_help:"Path to Template" cmd_optional:"!"`
	Release       string   `cmd:"release" cmd_help:"Version (e.g.: build-201806181113)" cmd_optional:"!"`
	Environment   string   `cmd:"env" cmd_help:"Environment (e.g.: live-master-server)" cmd_optional:"!"`
	Notify        string   `cmd:"notify" cmd_help:"Email to notify on startup" cmd_optional:"!"`
}

func NewInstanceCommand() cmd.Command {
	return cmd.Command(&InstanceCommand{
		Template:    "master",
		Environment: "live-master-server",
	})
}

func (c *InstanceCommand) Execute() uint8 {
	release := c.Release
	if release == "" {
		latest, err := sailing.FindLatestBuild()
		if err != nil {
			log.Errorf("Failed to get latest release: %s", err)
			return 1
		}
		release = latest
	}

	events := common.ParseEvents(c.Events)
	primaryHostname := events[0].Hostname

	userData := map[string]string{
		"INSTALL_FROM_RELEASE": c.Release,
		"USE_ENVIRONMENT":      c.Environment,
		"SERVER_NAME":          primaryHostname,
	}

	if c.Notify != "" {
		userData["SERVER_STARTUP_NOTIFY"] = c.Notify
	}

	instance, err := aws.TemplateFromFile(path.Join(paths.GetInstanceTemplate(c.Template)), userData)
	if err != nil {
		log.Errorf("Failed to launch instance: %s", err)
		return 1
	}

	descriptions, err := aws.LaunchTemplate(instance, events, aws.RoleSingleMaster, false)
	if err != nil {
		log.Errorf("There is an error during the instance launch: %s", err)
		return 1
	}

	description := descriptions[0]

	log.Info("Waiting for the instance to come up...")
	err = aws.WaitUntilInstanceUsable(description.Id, false)
	if err != nil {
		log.Errorf("Failed to wait for instance", err)
		return 1
	}

	desc, err := aws.DescribeInstance(description.Id, false)
	if err != nil {
		log.Errorf("Failed to describe instance: %s", err)
		return 1
	}
	description = *desc

	privateKeyPath, _ := paths.GetKeyPair()
	keyPair, err := agent.KeyPairAuthFromPrivateKey(privateKeyPath)
	if err != nil {
		log.Errorf("Failed to load keypair: %s", err)
		return 1
	}
	sshConnection := &agent.AgentConnection{
		Ip:   description.PublicIp,
		Auth: keyPair,
		User: common.SshUser,
	}

	err = sshConnection.WaitForConnectivity(5 * time.Second)
	if err != nil {
		log.Errorf("Unable to connect via SSH: %s", err)
		return 1
	}

	err = agent.DeployAgent(sshConnection)
	if err != nil {
		log.Errorf("Failed to deploy the agent binary: %s", err)
		return 1
	}

	agentInput := &common.AgentInput{
		AnalyticsRelease:     release,
		AnalyticsEnvironment: c.Environment,
		ServerName:           primaryHostname,
		AwsInstanceName:      description.Id,
		InternalHostname:     description.PublicHostname,
		ExternalIp:           description.PublicIp,
		InternalIp:           description.PrivateIp,
		Events:               events,
		Notify:               c.Notify,
		Scenario:             "master",
	}

	err = agent.InvokeAgent(sshConnection, agentInput)
	if err != nil {
		log.Errorf("Failed to execute agent stage master: %s", err)
		return 1
	}
	log.Info("Agent completed without error.")

	var masterDomain string
	var roles []aws.TargetGroupRole
	roles = append(roles, aws.RolesMaster)
	roles = append(roles, aws.RolesDefault)

	tgs := aws.CreateTargetGroupsWithAlarm(events, roles, description)
	for _, tg := range tgs {
		err = aws.WaitUntilTargetGroupUsable(tg, 6)
		if err != nil {
			log.Errorf("Failed to wait for succeeding health check: %s", err)
			return 1
		}

		domain, role, err := aws.GetMetadataForTg(tg)
		if err != nil {
			log.Warningf("Could not get metadata for target group %s: %s", string(*tg.TargetGroupArn), err)
		} else {
			if role == aws.RolesMaster {
				masterDomain = domain
			}
			log.Infof("Target group with role %s can be reached via https://%s", role, domain)
		}
	}

	//configure app against target group with master role

	appConfig := &sailing.ApplicationConfig{
		Host:          masterDomain,
		Port:          aws.LbListenerPort,
		AdminUsername: "admin",
		AdminPassword: "admin",
		ApiVersion:    1,
	}

	appClient, err := sailing.CreateAuthenticatedClient(appConfig)
	if err != nil {
		log.Warningf("Failed connect to application for post init steps: %s", err)
		log.Warningf("This might indicate a startup problem!")
	} else {
		err := appClient.ChangePassword("admin", c.AdminPassword)
		if err != nil {
			log.Warningf("Failed to change admin password: %s", err)
		}
		err = appClient.CreateUser(c.Username, c.Password)
		if err != nil {
			log.Warningf("Failed to create new user %s: %s", c.Username, err)
		}
		log.Infof("You can log-in to the AdminConsole with the new user %s", c.Username)
	}

	log.Infof("Scenario completed without error.")
	return 0
}

// will be overwritten by compiler flags
var (
	BuildRevision = "unknown"
	BuildVersion  = "unknown"
	BuildBranch   = "unknown"
)

type VersionCommand struct{}

func NewVersionCommand() cmd.Command {
	return cmd.Command(&VersionCommand{})
}

func (c *VersionCommand) Execute() uint8 {
	fmt.Printf("Build version: %s\n", BuildVersion)
	fmt.Printf("Build revision: %s\n", BuildRevision)
	fmt.Printf("Build branch: %s\n", BuildBranch)
	return 0
}

type AgentOnlyCommand struct {
	BasePath             string `cmd_optional:"!"`
	AnalyticsRelease     string `cmd_optional:"!"`
	AnalyticsEnvironment string
	ServerName           string
	InternalHostname     string
	ExternalIp           string
	InternalIp           string
	Event                []string
	Scenario             string
	DryRun               bool `cmd_optional:"!"`
}

func NewAgentOnlyCommand() cmd.Command {
	return cmd.Command(&AgentOnlyCommand{
		DryRun: false,
	})
}

func (c *AgentOnlyCommand) Execute() uint8 {

	if c.AnalyticsRelease == "" {
		build, err := sailing.FindLatestBuild()
		if err == nil {
			c.AnalyticsRelease = build
		} else {
			log.Errorf("Failed to get latest build: %s\n", err)
			return 1
		}
	}

	privateKeyPath, _ := paths.GetKeyPair()
	keyPair, err := agent.KeyPairAuthFromPrivateKey(privateKeyPath)
	if err != nil {
		log.Errorf("Failed to load keypair: %s", err)
		return 1
	}

	sshConnection := &agent.AgentConnection{
		Ip:   c.ExternalIp,
		Auth: keyPair,
		User: common.SshUser,
	}

	log.Info("Deploying agent...")
	err = agent.DeployAgent(sshConnection)
	if err != nil {
		log.Errorf("Failed to deploy the agent binary: %s", err)
		return 1
	}

	agentInput := &common.AgentInput{
		BasePath:             c.BasePath,
		AnalyticsRelease:     c.AnalyticsRelease,
		AnalyticsEnvironment: c.AnalyticsEnvironment,
		ServerName:           c.ServerName,
		InternalHostname:     c.InternalHostname,
		ExternalIp:           c.ExternalIp,
		InternalIp:           c.InternalIp,
		Events:               common.ParseEvents(c.Event),
		Scenario:             c.Scenario,
		DryRun:               c.DryRun,
	}

	log.Infof("Invoking agent with scenario %s...", agentInput.Scenario)
	err = agent.InvokeAgent(sshConnection, agentInput)
	if err != nil {
		log.Errorf("Failed to execute agent: %s", err)
		return 1
	}

	log.Info("Agent execution completed.")

	return 0
}
