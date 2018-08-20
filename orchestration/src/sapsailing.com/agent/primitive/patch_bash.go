package primitive

import (
	"io/ioutil"
	"os"
	"path"

	log "github.com/sirupsen/logrus"
	bash "mvdan.cc/sh/syntax"
	"sapsailing.com/common"
)

func buildQuotedString(value string) *bash.Word {
	literalValue := bash.WordPart(&bash.Lit{Value: value})
	quotedString := bash.WordPart(&bash.DblQuoted{Parts: []bash.WordPart{literalValue}})
	word := &bash.Word{Parts: []bash.WordPart{quotedString}}

	return word
}

func buildAssignment(name string, value string) *bash.Stmt {
	assignment := &bash.Assign{Name: &bash.Lit{Value: name}, Value: buildQuotedString(value)}
	return &bash.Stmt{Cmd: bash.Command(&bash.CallExpr{Assigns: []*bash.Assign{assignment}})}
}

func PatchBashConf(scriptPath string, values map[string]string) (common.UndoFunction, error) {

	f, err := os.Open(scriptPath)
	if err != nil {
		return nil, err
	}

	parser := bash.NewParser(bash.KeepComments)
	parsed, err := parser.Parse(f, path.Base(scriptPath))
	if err != nil {
		return nil, err
	}

	err = f.Close()
	if err != nil {
		return nil, err
	}

	changeCounter := 0
	applied := map[string]bool{}

	bash.Walk(parsed, func(node bash.Node) bool {
		switch node.(type) {
		case *bash.Assign:
			assignment := node.(*bash.Assign)

			varName := assignment.Name.Value
			newValue, toBeChanged := values[varName]

			if toBeChanged {
				applied[varName] = true
				changeCounter++

				assignment.Value = buildQuotedString(newValue)
			}
			return false
		default:
			return true
		}
	})

	for k, v := range values {
		_, alreadyApplied := applied[k]

		if !alreadyApplied {
			changeCounter++
			parsed.Stmts = append(parsed.Stmts, buildAssignment(k, v))
		}
	}

	if changeCounter > 0 {
		backupDir, err := ioutil.TempDir("/tmp", "bash-patch-")
		if err != nil {
			return nil, err
		}
		backupFile := path.Join(backupDir, path.Base(scriptPath))
		err = common.Copy(scriptPath, backupFile)
		if err != nil {
			return nil, err
		}

		f, err = os.OpenFile(scriptPath, os.O_WRONLY|os.O_CREATE|os.O_SYNC|os.O_TRUNC, 0755)
		if err != nil {
			return nil, err
		}

		printer := bash.NewPrinter(bash.KeepPadding)
		printer.Print(f, parsed)
		err = f.Close()
		if err != nil {
			return nil, err
		}

		return func() {
			log.Warnf("Rolling back patch to %s...", scriptPath)
			os.Rename(backupFile, scriptPath)
		}, nil
	}

	return func() {}, nil
}
