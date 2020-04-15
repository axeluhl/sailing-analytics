package cmd

import (
	"fmt"
	"github.com/mitchellh/mapstructure"
	"os"
	"reflect"
	"strings"
)

type CommandFactory func() Command
type CommandMap map[string]CommandSpec
type Command interface {
	Execute() uint8
}
type CommandSpec struct {
	Help    string
	Factory CommandFactory
}

func DefaultMain(commands CommandMap) uint8 {
	return RunCommands(os.Args[1:], commands, PrintCommandsFallback)
}

func RunCommands(args []string, commands CommandMap, fallback func(commandMap CommandMap, err error) uint8) uint8 {
	if len(args) > 0 {
		name := strings.ToLower(args[0])
		command, ok := commands[name]
		if ok {
			data := command.Factory()
			err := MapParams(&data, ParseParams(args[1:]))
			if err != nil {
				return fallback(commands, err)
			}
			return data.Execute()
		}
	}
	return fallback(commands, nil)
}

func ParseParams(rawParams []string) map[string][]string {
	params := map[string][]string{}

	for i := range rawParams {
		rawParam := rawParams[i]
		sepIdx := strings.Index(rawParam, "=")
		var name string
		if sepIdx >= 0 {
			name = rawParam[0:sepIdx]
		} else {
			name = rawParam
		}
		name = strings.ToLower(name)
		paramValues, seen := params[name]
		if !seen {
			paramValues = []string{}
		}
		if sepIdx >= 0 {
			params[name] = append(paramValues, rawParam[sepIdx+1:])
		} else {
			params[name] = paramValues
		}
	}

	return params
}

func flattenOnDemand(source reflect.Type, target reflect.Type, val interface{}) (interface{}, error) {
	if source.Kind() == reflect.Slice && target.Kind() != reflect.Slice && source.Elem() == reflect.TypeOf("") {
		slice := val.([]string)
		return slice[len(slice)-1], nil
	}
	return val, nil
}

func MapParams(cmd *Command, params map[string][]string) error {
	meta := mapstructure.Metadata{}
	conf := &mapstructure.DecoderConfig{
		DecodeHook:       flattenOnDemand,
		Result:           cmd,
		WeaklyTypedInput: true,
		TagName:          "cmd",
		Metadata:         &meta,
		ZeroFields:       false,
	}
	decoder, _ := mapstructure.NewDecoder(conf)
	err := decoder.Decode(params)
	if err != nil {
		return err
	}

	structPtr := reflect.ValueOf(cmd).Elem().Elem()
	structVal := structPtr.Elem()
	structType := structVal.Type()
	supplied := map[string]bool{}
	for _, key := range meta.Keys {
		supplied[strings.ToLower(key)] = true
	}

	missingCounter := 0
	for i := 0; i < structType.NumField(); i++ {
		field := structType.Field(i)
		name := field.Tag.Get("cmd")
		if name == "" {
			name = strings.ToLower(field.Name)
		}
		_, fieldOptional := field.Tag.Lookup("cmd_optional")
		_, fieldSupplied := supplied[name]
		if !fieldSupplied && !fieldOptional {
			missingCounter++

			fmt.Printf("Missing argument: %s=<%s>\n", name, field.Type)
			helpText := field.Tag.Get("cmd_help")
			if helpText != "" {
				fmt.Printf("\t%s\n", helpText)
			}
		}
	}
	if missingCounter > 0 {
		return fmt.Errorf("missing arguments")
	}

	return err
}

func PrintCommandsFallback(commandMap CommandMap, err error) uint8 {
	if err != nil {
		fmt.Printf("\nError: %s\n", err.Error())
	} else {
		fmt.Println("Possible commands:")
		for name, command := range commandMap {
			fmt.Printf("- %s: %s\n", name, command.Help)
		}
	}
	return 1
}
