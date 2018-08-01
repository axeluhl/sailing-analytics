package common

type UndoFunction func()

type UndoStack []UndoFunction

func (s *UndoStack) Push(function UndoFunction) {
	*s = append(*s, function)
}

func (s *UndoStack) UndoAll() {
	for i := len(*s) - 1; i >= 0; i-- {
		(*s)[i]()
	}
	*s = UndoStack{}
}

type AgentInput struct {
	BasePath             string
	InternalIp           string
	ExternalIp           string
	ServerName           string
	InternalHostname     string
	Events               []EventSpec
	AnalyticsRelease     string
	AnalyticsEnvironment string
	AwsInstanceName      string
	Notify               string

	Scenario string
	DryRun   bool
}
