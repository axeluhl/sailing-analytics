package sailing

import (
	"fmt"
	"regexp"
	"strconv"

	"github.com/go-resty/resty"
	"time"
)

func init() {
	resty.SetDisableWarn(true)
}

const ReleasesEndpoint = "http://releases.sapsailing.com/?C=M;O=D"

// API doc: https://www.sapsailing.com/sailingserver/webservices/api/v1/index.html

func isOk(resp *resty.Response) bool {
	status := resp.StatusCode()
	return status >= 200 && status < 300
}

func FindLatestBuild() (string, error) {
	r, err := resty.R().Get(ReleasesEndpoint)

	if err != nil {
		return "", err
	}

	if !isOk(r) {
		return "", fmt.Errorf("release index returned an error: %d", r.StatusCode())
	}

	releasePattern := regexp.MustCompile("([^<>]+)/</a>")
	matches := releasePattern.FindAllStringSubmatch(r.String(), -1)

	if len(matches) == 0 {
		return "", fmt.Errorf("no builds found")
	}

	return matches[0][1], nil
}

type AccessToken struct {
	Username    string
	AccessToken string `json:"access_token"`
}

type SailingEvent struct {
	Id          string `json:"eventid"`
	DisplayName string `json:"eventname"`
}

type ApplicationConfig struct {
	Host          string
	Port          uint16
	ApiVersion    uint
	AdminUsername string
	AdminPassword string
}

type ApplicationClient struct {
	Host       string
	Token      AccessToken
	ApiVersion uint
	Port       uint16
}

type ApiError struct {
	Method         string
	Url            string
	Response       string
	ResponseStatus int
}

func (e *ApiError) Error() string {
	return fmt.Sprintf("request `%s %s` failed: %d %s", e.Method, e.Url, e.ResponseStatus, e.Response)
}

func (c *ApplicationClient) authenticatedRequest() *resty.Request {
	r := resty.R()
	r.SetAuthToken(c.Token.AccessToken)
	return r
}

func BaseUrl(host string, port uint16) string {
	var address string
	var proto string
	if port == 80 {
		address = host
		proto = "http"
	} else if port == 443 {
		address = host
		proto = "https"
	} else {
		address = fmt.Sprintf("%s:%d", host, port)
		proto = "http"
	}
	return fmt.Sprintf("%s://%s", proto, address)
}

func (c *ApplicationClient) BaseUrl() string {
	return BaseUrl(c.Host, c.Port)
}

func (c *ApplicationClient) Api(path string) string {
	return fmt.Sprintf("%s/sailingserver/api/v%d/%s", c.BaseUrl(), c.ApiVersion, path)
}

func SecurityApi(host string, port uint16, path string) string {
	return fmt.Sprintf("%s/security/api/restsecurity/%s", BaseUrl(host, port), path)
}

func (c *ApplicationClient) SecurityApi(path string) string {
	return SecurityApi(c.Host, c.Port, path)
}

type ReqData struct {
	FormData map[string]string
	Result   interface{}
	Query    map[string]string
}

func (c *ApplicationClient) exec(method string, endpoint string, data *ReqData) error {
	req := c.authenticatedRequest()
	if data.FormData != nil {
		req.SetFormData(data.FormData)
	}
	if data.Result != nil {
		req.SetResult(data.Result)
	}
	if data.Query != nil {
		req.SetQueryParams(data.Query)
	}
	res, err := req.Execute(method, endpoint)
	if err != nil {
		return err
	}

	if !isOk(res) {
		return error(&ApiError{
			Url:            endpoint,
			Method:         method,
			Response:       res.String(),
			ResponseStatus: res.StatusCode(),
		})
	}

	return nil
}

func (c *ApplicationClient) post(endpoint string, data *ReqData) error {
	return c.exec("POST", endpoint, data)
}

func (c *ApplicationClient) CreateEvent(name string, withLeaderboardGroup bool) (*SailingEvent, error) {
	input := map[string]string{
		"eventName":              name,
		"venuename":              "Default",
		"createregatta":          "false",
		"createleaderboardgroup": strconv.FormatBool(withLeaderboardGroup),
	}
	event := &SailingEvent{}
	err := c.post(c.Api("events/createEvent"), &ReqData{FormData: input, Result: event})

	if err != nil {
		return nil, err
	}

	return event, nil
}

func (c *ApplicationClient) ChangePassword(user string, newPassword string) error {
	input := map[string]string{
		"username": user,
		"password": newPassword,
	}
	return c.post(c.SecurityApi("change_password"), &ReqData{FormData: input})
}

func (c *ApplicationClient) CreateUser(name string, password string) error {
	query := map[string]string{
		"username": name,
		"password": password,
	}
	return c.post(c.SecurityApi("create_user"), &ReqData{Query: query})
}

func (c *ApplicationClient) InitializeApplication(eventName string, adminPassword string, username string, password string) (*SailingEvent, error) {
	event, eventErr := c.CreateEvent(eventName, false)
	changePassErr := c.ChangePassword("admin", adminPassword)
	createUserErr := c.CreateUser(username, password)

	if eventErr != nil || changePassErr != nil || createUserErr != nil {
		return event, fmt.Errorf("errors while initializing:\nEvent: %s\nChange Admin Password: %s\nCreate User: %s", eventErr, changePassErr, createUserErr)
	}

	return event, nil
}

func CreateAuthenticatedClient(config *ApplicationConfig) (*ApplicationClient, error) {
	data := AccessToken{}
	req := resty.R()
	req.SetResult(&data)
	req.SetBasicAuth(config.AdminUsername, config.AdminPassword)
	res, err := req.Get(SecurityApi(config.Host, config.Port, "access_token"))
	if err != nil {
		return nil, err
	}

	if !isOk(res) {
		println(res.String())
		return nil, fmt.Errorf("access token endpoint returned error: %d", res.StatusCode())
	}

	client := &ApplicationClient{
		Host:       config.Host,
		Port:       config.Port,
		Token:      data,
		ApiVersion: config.ApiVersion,
	}

	return client, nil
}

func IsApplicationAvailable(config *ApplicationConfig) bool {
	_, err := CreateAuthenticatedClient(config)
	return err == nil
}

func WaitForAuthenticatedClient(config *ApplicationConfig, delay time.Duration) (*ApplicationClient, error) {
	try := 0
	for {
		client, err := CreateAuthenticatedClient(config)
		if err == nil {
			return client, nil
		}
		try++
		if try >= 10 {
			return client, err
		}
		time.Sleep(delay)
	}
}
