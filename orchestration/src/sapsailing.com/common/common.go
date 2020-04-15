package common

import (
	"encoding/json"
	"golang.org/x/text/runes"
	"golang.org/x/text/transform"
	"golang.org/x/text/unicode/norm"
	"io"
	"io/ioutil"
	"math/rand"
	"os"
	"os/exec"
	"regexp"
	"strings"
	"unicode"
)

const (
	SapSailingDomain = "sapsailing.com"
	SshUser          = "sailing"
)

var letterRunes = []rune("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")

func RandStringRunes(n int) string {
	b := make([]rune, n)
	for i := range b {
		b[i] = letterRunes[rand.Intn(len(letterRunes))]
	}
	return string(b)
}

func GetEnvOrElse(s string, def string) string {
	v, found := os.LookupEnv(s)
	if found {
		return v
	}
	return def
}

func LoadFromFile(path string, dst interface{}) error {
	f, err := os.Open(path)
	if err != nil {
		return err
	}
	defer f.Close()
	return LoadFromReader(f, &dst)
}

func LoadFromReader(r io.Reader, dst interface{}) error {
	b, err := ioutil.ReadAll(r)
	if err != nil {
		return err
	}

	if len(b) == 0 {
		return nil
	}

	err = json.Unmarshal(b, &dst)
	if err != nil {
		return err
	}

	return nil
}

func RunCommandSimple(bin string, args ...string) (bool, string, error) {
	return RunCommand("", []string{}, bin, args...)
}

func RunCommand(workDir string, env []string, bin string, args ...string) (bool, string, error) {
	cmd := exec.Command(bin, args...)
	cmd.Dir = workDir
	cmd.Env = append(os.Environ(), env...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return false, string(output), err
	}
	return cmd.ProcessState.Success(), string(output), nil
}

func Copy(src string, dst string) error {
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()

	out, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer out.Close()

	_, err = io.Copy(out, in)
	if err != nil {
		return err
	}
	return nil
}

func DomainToHostname(domain string) string {
	return strings.Replace(domain, "."+SapSailingDomain, "", 1)
}

func HostnameToDomain(hostname string) string {
	return hostname + "." + SapSailingDomain
}

func HostnameToMasterDomain(hostname string) string {
	return hostname + "-master." + SapSailingDomain
}

func HostnameToMasterDbName(hostname string) string {
	return hostname
}

func HostnameToReplicaDbName(hostname string) string {
	return HostnameToMasterDbName(hostname) + "-replica"
}

var eventNameAttachNums = regexp.MustCompile("[^\\p{L}\\d]+(\\d+)")
var eventNameStripPattern = regexp.MustCompile("[^\\p{L}\\d]+")
var eventNameLeadingNumToTail = regexp.MustCompile("^((?:\\d+-)+)(.+)")
var diacriticsTransform = transform.Chain(norm.NFD, runes.Remove(runes.In(unicode.Mn)), norm.NFC)

func EventNameToHostname(eventName string) string {
	sep := "-"
	lower := strings.ToLower(eventName)
	numsAttached := eventNameAttachNums.ReplaceAllString(lower, "$1")
	dashed := strings.Trim(eventNameStripPattern.ReplaceAllString(numsAttached, sep), sep)
	noLeadingNum := strings.TrimRight(eventNameLeadingNumToTail.ReplaceAllString(dashed, "$2"+sep+"$1"), sep)
	noDiacitics, _, err := transform.String(diacriticsTransform, noLeadingNum)
	if err == nil {
		return noDiacitics
	}
	return noLeadingNum
}

type EventSpec struct {
	DisplayName string
	Hostname    string
}

func ParseEvents(rawEvents []string) []EventSpec {
	events := make([]EventSpec, len(rawEvents))

	for i, rawEvent := range rawEvents {
		parts := strings.SplitN(rawEvent, ":", 2)
		event := EventSpec{}
		if len(parts) > 1 {
			event.Hostname = parts[0]
			event.DisplayName = parts[1]
		} else {
			event.DisplayName = parts[0]
			event.Hostname = EventNameToHostname(event.DisplayName)
		}
		events[i] = event
	}

	return events
}

func GetEnvMap() map[string]string {
	out := map[string]string{}
	for _, env := range os.Environ() {
		parts := strings.SplitN(env, "=", 2)
		if len(parts) > 1 {
			out[parts[0]] = parts[1]
		} else {
			out[parts[0]] = ""
		}
	}
	return out
}
