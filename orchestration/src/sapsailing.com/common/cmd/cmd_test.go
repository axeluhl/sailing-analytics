package cmd

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestParseParams(t *testing.T) {
	args := []string{"a=b", "c", "d=e", "d=f", "with-dash=works"}
	params := ParseParams(args)
	expected := map[string][]string{
		"a":         {"b"},
		"c":         {},
		"d":         {"e", "f"},
		"with-dash": {"works"},
	}

	assert.Equal(t, expected, params, "Params should parse correctly.")
}

type TestCommand struct {
	Name      string
	Age       uint
	WithDash  string `cmd:"with-dash"`
	Different string `cmd:"sooo_different"`
}

func (c *TestCommand) Execute() uint8 {
	return 2
}

func TestRunCommands(t *testing.T) {

	params := []string{"test", "name=test", "age=1", "with-dash=works", "sooo_different=works"}

	commands := CommandMap{
		"test": CommandSpec{
			Help: "Creates a master instance",
			Factory: func() Command {
				return Command(&TestCommand{})
			},
		},
	}
	result := RunCommands(params, commands, func(commandMap CommandMap, err error) uint8 {
		return 1
	})

	assert.Equal(t, uint8(2), result)
}
