# Thundra LocalStack Demo

Simple demo application deployed to LocalStack and monitored/traced/debugged by Thundra

## Prerequisites

* LocalStack
* Docker
* JDK 8+
* Maven 3+
* Node.js / `npm`
* `make`

## Installing

Install the dependencies using this command:
```
make install
```

Set your Thundra API key and project id into `Makefile`
```
export THUNDRA_APIKEY = <YOUR-THUNDRA-API-KEY-HERE>
export THUNDRA_AGENT_TEST_PROJECT_ID = <YOUR-THUNDRA-PROJECT-ID-HERE>
```

## Running

Start the application locally in LocalStack:
```
make start-embedded
```

## Testing

Get your API endpoint from the deploy output by `endpoints` property:
```
...
Service Information
service: thundra-demo-localstack
...
endpoints:
  http://localhost:4566/restapis/${apiId}/${stage}/_user_request_
functions:
  http_handleRequest: thundra-demo-localstack-local-http_handleRequest
  backend_processRequest: thundra-demo-localstack-local-backend_processRequest
  backend_archiveResult: thundra-demo-localstack-local-backend_archiveResult
...
```

And then send the request to your endpoint on Localstack:
```
curl http://localhost:4566/restapis/${apiId}/${stage}/_user_request_/${path}
```

For http_handleRequest service, you can send the request in the following format:
```
curl "http://localhost:4566/restapis/${apiId}/${stage}/_user_request_/requests"
curl -X POST "http://localhost:4566/restapis/${apiId}/${stage}/_user_request_/requests"
```

## License

This code is available under the Apache 2.0 license.
