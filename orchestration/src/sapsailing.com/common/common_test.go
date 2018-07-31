package common

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestEventNameToHostname(t *testing.T) {
	assert.Equal(t, "bundesliga2018-1", EventNameToHostname("1. Bundesliga - 2018"))
	assert.Equal(t, "segel-bundesliga2018-travemunde-1", EventNameToHostname("1. Segel-Bundesliga 2018 - Travemünde"))
}
