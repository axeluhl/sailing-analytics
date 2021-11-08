GOCMD := go
GOBUILD := $(GOCMD) build
GOINSTALL := $(GOCMD) install -v
GOCLEAN := $(GOCMD) clean -v
GOTEST := $(GOCMD) test -cover -v -timeout 1200s
GOGET := $(GOCMD) get -u -v
GOFMT := gofmt
GOCOVER := $(GOCMD) tool cover
GOVET := $(GOCMD) vet
GOBENCHMARK=$(GOCMD) test ./src/sapsailing.com/... -run=XXX -bench=.
DEPENDENCIES := \
	golang.org/x/crypto/ssh \
	github.com/aws/aws-sdk-go/aws \
	github.com/aws/aws-sdk-go/aws/session \
	github.com/aws/aws-sdk-go/service/ec2 \
	github.com/sirupsen/logrus \
	github.com/stretchr/testify/assert \
	github.com/go-resty/resty \
	github.com/mitchellh/mapstructure \
	github.com/pkg/sftp \
	mvdan.cc/sh/syntax \
	golang.org/x/text \
	github.com/pbnjay/memory
VERSION=0.0.1
BUILD=`git rev-parse HEAD`
BRANCH=`git rev-parse --abbrev-ref HEAD`
PLATFORMS=linux
ARCHITECTURES=amd64

# Setup linker flags option for build that interoperate with variable names in src code
LDFLAGS=-ldflags "-X main.BuildVersion=${VERSION} -X main.BuildRevision=${BUILD} -X main.BuildBranch=${BRANCH}"

ifndef PREFIX
$(error PREFIX is not set)
endif

ifndef GOPATH
$(error GOPATH is not set)
endif

all: clean build test distro

test:
	@echo "executing all tests now"
	@$(GOTEST) ./src/sapsailing.com/$(PACKAGE)/... $(ARGS) 2>&1 | tee $${PREFIX}/testlogs/${VERSION}_${BUILD}_test.log ;
	@echo "done."

checktest:
	@echo "Skipped Tests..."
	@cat ${PREFIX}/testlogs/*_test.log | egrep -n "SKIP:"; test $$? -eq 1;

	@echo "Failed Tests..."
	@cat $${PREFIX}/testlogs/*_test.log | egrep -n "FAIL:|\[build failed\]|^FAIL\s+"; test $$? -eq 1;

vet:
	@$(GOVET) sapsailing.com/... 2>&1 | tee -a $${PREFIX}/testlogs/*_${BUILD}_test.log || test 1;
	@echo "done."

testcover:
	@cat $${PREFIX}/testlogs/*_test.log | grep -E '(ok|FAIL|\?)(.*)sapsailing.com\/(.*)(.*)' 2>&1 | tee -a ${PREFIX}/testlogs/*_${BUILD}_test.log
	@bash ./calculateCodeCoverage.sh $${PREFIX}/testlogs/*_test.log | tee -a $${PREFIX}/testlogs/*_${BUILD}_test.log

benchmark:
	@$(GOBENCHMARK) || exit 1;

fmt:
	@find ./src/sapsailing.com -name '*.go' -exec $(GOFMT) -w -s "{}" \;
	@echo "done."

build:
	$(foreach GOOS, $(PLATFORMS),\
	$(foreach GOARCH, $(ARCHITECTURES), cd ${PREFIX}/src/sapsailing.com/orchestrator; GOOS=$(GOOS) GOARCH=$(GOARCH) $(GOBUILD) ${LDFLAGS} -v -o $${PREFIX}/bin/orchestrator-$(GOOS)-$(GOARCH)))
	$(foreach GOOS, $(PLATFORMS),\
	$(foreach GOARCH, $(ARCHITECTURES), cd ${PREFIX}/src/sapsailing.com/agent; GOOS=$(GOOS) GOARCH=$(GOARCH) $(GOBUILD) ${LDFLAGS} -v -o $${PREFIX}/bin/agent-$(GOOS)-$(GOARCH)))
	@chmod +x $${PREFIX}/bin/*
	@echo "done."

clean:
	@$(GOCLEAN) ./src/sapsailing.com/... || exit 1;
	@rm -rf orchestration*.tgz
	@rm -rf bin/*
	@echo "done."

dep:
	@for p in $(DEPENDENCIES); do \
		echo "__ downloading $$p ...";\
		$(GOGET) $$p || exit 1; \
	done

distro:
	bash ./createDistro.sh "$(VERSION)_$(BUILD)" || exit 1;

install:
	@$(GOINSTALL) $${LDFLAGS} ./src/sapsailing.com/...
