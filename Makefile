export DOCKER_BRIDGE ?= $(shell (uname -a | grep Linux > /dev/null) && echo 172.17.0.1 || echo docker.for.mac.localhost)
export SERVICES = serverless,cloudformation,sts,sqs,dynamodb,s3,sns
export AWS_ACCESS_KEY_ID ?= test
export AWS_SECRET_ACCESS_KEY ?= test
export AWS_DEFAULT_REGION ?= us-east-1
export START_WEB ?= 1
export THUNDRA_APIKEY = <YOUR-THUNDRA-API-KEY-HERE>
usage:           ## Show this help
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

install:         ## Install dependencies
	npm install
	which serverless || npm install -g serverless
	which localstack || pip install localstack

build:          ## Build app
	echo "Building Serverless app ..."
	mvn clean package -DskipTests

test:           ## Test app
	echo "Building Serverless app ..."
	mvn clean test

deploy:         ## Deploy the app locally
	echo "Deploying Serverless app to local environment ..."
	SLS_DEBUG=1 serverless deploy --stage local

start:           ## Build, deploy and start the app locally
	@make build;
	@make deploy;

lint:            ## Run code linter
	@npm run lint
	@flake8 demo

.PHONY: usage install start lint
