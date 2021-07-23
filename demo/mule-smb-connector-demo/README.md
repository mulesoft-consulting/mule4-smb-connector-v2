# Mule SMB Connector Demo App

## Introduction

This project demonstrate how to use Mule SMB Connector funcionalities.

## Requirements

In order to run this demo application, a file server supporting SMBv2 or SMBv3 is required, like Microsoft Windows or Samba Server.

If no file server is available, Docker can be used to run a Samba server locally, issuing the following command:

``` 
docker run -it -p 139:139 -p 445:445 -d dperson/samba -p  -u "mulesoft;mulesoft"  -s "share;/share;no;no;no;mulesoft" -w "WORKGROUP" -r
````

This command will instantiate a docker container running a Samba server, and will create a share named `share`, that can be accessed by the user `mulesoft` identified by the password `mulesoft`.

The user must have read and write permissions on the share root in order to run the application successfully.

## Application Setup

Before running the demo, make sure the connection properties defined in the configuration are correct by editing the configuration file (`config.yaml`) located under the `src/main/resources`. 
The default property values correspond to the connection properties required to connect to the Samba server running in a docker container, created by issuing the docker command provided in the Requirement section.

## Running the Application

1. Checks if the configuration file is correctly configured
2. Starts the Mule App
3. Sends the any JSON payload using the request `POST /write`

The application will process the request according to the following steps:
1. Logs the payload in the `<SHARE_ROOT>/logs/output.log`
2. Writes the payload to the file `<SHARE_ROOT>/input/request-<correlationId>.txt`
3. Adds more log entries to the `<SHARE_ROOT>/logs/output.log`
4. Returns the following payload in the response:
```
{"result": "OK"}
```
5. In case of any failure, the error will be written to the `<SHARE_ROOT>/logs/output.log` log file and will return the following response:
```
{"result": "NOK", "error": <errror description>}
```

All files written to the `<SHARE_ROOT>/input` directory will be processed as follow:
1. On New or Updated File listener starts the flow
2. Logs messages in the `<SHARE_ROOT>/logs/output.log` log file
3. Moves the file to the `/pending` directory

Every 20 seconds, the flow will process all files available in the `/pending` directory:
1. Creates the `/pending` directory if not exists
2. Lists all files in the `/pending` directory
3. For each file found:
3.1. Logs messages in the `<SHARE_ROOT>/logs/output.log` log file
3.2. Copies the file to the `/copied` directory
3.3. Moves the file to the `/processed` directory

All files written to the `<SHARE_ROOT>/copied` directory will be processed as follow:
1. On New or Updated File listener starts the flow
2. Logs messages in the `<SHARE_ROOT>/logs/output.log` log file
3. Deletes the copied file








