package common

import (
	"bytes"
	log "github.com/sirupsen/logrus"
	"io"
	"os"
	"time"
)

const (
	LogEnvVar = "LOG"
	LogFile   = "sailing-automation.log"
)

type SimpleFormatter struct {
	Source string
}

func (f *SimpleFormatter) Format(e *log.Entry) ([]byte, error) {
	var buf bytes.Buffer
	if f.Source != "" {
		buf.WriteString("<")
		buf.WriteString(f.Source)
		buf.WriteString("> ")
	}
	buf.WriteString(e.Time.Format(time.RFC3339))
	buf.WriteString(" [")
	buf.WriteString(e.Level.String())
	buf.WriteString("] ")
	buf.WriteString(e.Message)
	buf.WriteString("\n")

	return buf.Bytes(), nil
}

var LogOutput io.Writer

func init() {
	level, err := log.ParseLevel(GetEnvOrElse(LogEnvVar, ""))
	if err != nil {
		level = log.InfoLevel
	}
	log.SetLevel(level)
	log.SetFormatter(&SimpleFormatter{})

	f, err := os.OpenFile(LogFile, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0766)
	if err == nil {
		LogOutput = io.MultiWriter(os.Stdout, f)
	} else {
		LogOutput = os.Stdout
	}
	log.SetOutput(LogOutput)
}
