# Thundra LocalStack Demo

Simple demo application deployed to LocalStac and monitored/traced/debugged by Thundra

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

Set your Thundra API key into `serverless.yml`
```
thundra_apikey: <YOUR-THUNDRA-API-KEY-HERE>
```

## Running

Start the application locally in LocalStack:
```
make start
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
  helloService: thundra-demo-localstack-local-helloService
...
```

And then send the request to your endpoint on Localstack:
```
curl http://localhost:4566/restapis/${apiId}/${stage}/_user_request_/${path}
```

For hello service, you can send the request in the following format:
```
curl "http://localhost:4566/restapis/${apiId}/${stage}/_user_request_/hello?name=${name}"
```

## License

This code is available under the Apache 2.0 license.
