package primitive

import (
	"fmt"

	"github.com/pbnjay/memory"
	log "github.com/sirupsen/logrus"
)

const (
	MemoryReservedForOsInMB          uint64 = 2048
	MinimumMemoryForJavaInstanceInMB uint64 = 1024
)

func GetMemoryForJavaInstanceInMB(numberOfJavaInstances uint64, dryRun bool) (uint64, error) {
	var memory uint64
	total := GetHostMemoryInMB()
	if dryRun {
		return total, nil
	}
	if total < MemoryReservedForOsInMB {
		log.Warningf("host memory %d mb smaller than reservation for os", total)
		return 0, fmt.Errorf("not enough available memory")
	} else if total < (MemoryReservedForOsInMB + MinimumMemoryForJavaInstanceInMB) {
		log.Warningf("host memory %d mb smaller than reservation for os and java minimum", total)
		return 0, fmt.Errorf("not enough available memory")
	}
	log.Debugf("host has %d mb memory", total)

	memory = (total - MemoryReservedForOsInMB) / numberOfJavaInstances
	log.Debugf("calculated %d mb memory for java instance", memory)

	return memory, nil
}

func GetHostMemoryInMB() uint64 {
	return memory.TotalMemory() / 1000000
}
