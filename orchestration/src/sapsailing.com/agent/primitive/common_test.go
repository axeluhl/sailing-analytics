package primitive

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGetHostMemoryInMB(t *testing.T) {
	assert.NotEmpty(t, GetHostMemoryInMB())
}

func TestGetMemoryForJavaInstanceInMB(t *testing.T) {
	memory, err := GetMemoryForJavaInstanceInMB(1, true)
	assert.NoError(t, err)
	assert.NotEmpty(t, memory)
}
