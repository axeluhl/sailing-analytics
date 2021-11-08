package primitive

import (
	"io/ioutil"
	"path"
	"testing"

	"github.com/stretchr/testify/require"
	"sapsailing.com/common"
	"sapsailing.com/orchestrator/paths"
)

func TestPatchBashConf(t *testing.T) {
	filePath := "/tmp/test.sh"
	sourceFile := path.Join(paths.GetPrefix(), "testdata", "test.sh")
	err := common.Copy(sourceFile, filePath)
	require.NoError(t, err, "Copying the file to tmp should not fail")

	fields := map[string]string{
		"NEW_VAR":               "new value",
		"NEW_VAR2":              "new value2",
		"INSTANCE_ID":           "patched-id",
		"SERVER_STARTUP_NOTIFY": "test",
	}
	_, err = PatchBashConf(filePath, fields)
	require.NoError(t, err, "Patching bash file should not fail")

	_, err = ioutil.ReadFile(filePath)
	require.NoError(t, err, "Reading the file after patching should not fail")

	//TODO: verify content
}
