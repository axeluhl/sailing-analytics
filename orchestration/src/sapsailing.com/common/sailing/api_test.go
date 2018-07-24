package sailing

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestBaseUrl(t *testing.T) {
	assert.Equal(t, BaseUrl("test", 80), "http://test")
	assert.Equal(t, BaseUrl("test", 443), "https://test")
	assert.Equal(t, BaseUrl("test", 8888), "http://test:8888")
}

func TestApplicationClient_BaseUrl(t *testing.T) {
	assert.Equal(t, (&ApplicationClient{Host: "test", Port: 80}).BaseUrl(), "http://test")
	assert.Equal(t, (&ApplicationClient{Host: "test", Port: 443}).BaseUrl(), "https://test")
	assert.Equal(t, (&ApplicationClient{Host: "test", Port: 8888}).BaseUrl(), "http://test:8888")
}

func TestSecurityApi(t *testing.T) {
	assert.Equal(t, SecurityApi("test", 80, "test"), "http://test/security/api/restsecurity/test")
}

func TestApplicationClient_SecurityApi(t *testing.T) {
	assert.Equal(t, (&ApplicationClient{Host: "test", Port: 80}).SecurityApi("test"), "http://test/security/api/restsecurity/test")
}

func TestApplicationClient_Api(t *testing.T) {
	assert.Equal(t, (&ApplicationClient{Host: "test", Port: 80, ApiVersion: 2}).Api("test"), "http://test/sailingserver/api/v2/test")
}

func TestApplicationClient_AuthenticatedRequest(t *testing.T) {
	tok := AccessToken{Username: "admin", AccessToken: "token"}
	req := (&ApplicationClient{Host: "test", Port: 80, ApiVersion: 2, Token: tok}).authenticatedRequest()

	assert.Equal(t, req.Token, tok.AccessToken)
}
