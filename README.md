# MuleSoft SMB Extension

Initial MuleSoft SMB Connector implementation for Mule 4.2.x


## Pending tasks:
- Documentation
- Run unit tests against different SMB versions
- Run unit tests against different server authentication methods
- Code revision

Add this dependency to your application pom.xml

```
<groupId>org.mule.connectors</groupId>
<artifactId>mule-smb-connector</artifactId>
<version>1.0.0</version>
<classifier>mule-plugin</classifier>
```

## Running the demo

- Start a new Samba server docker image: 
```
sudo docker run -it -p 139:139 -p 445:445 -d dperson/samba -p  -u "mulesoft;mulesoft"  -s "shared;/shared;no;no;no;mulesoft" -w "WORKGROUP"
```

- Install the connector in the local Maven Repository:
```
mvn clean install -DskipTests
``` 

- Import the demo project in Anypoint Studio 7.x

- Start Anypoint Studio embedded Mule Runtime

- Send a request to the application: POST http://localhost:8081/write (payload can be anything, and it will be written to the file)

## Known Issues / Technical Debts

- More unit tests required to validate operations on objects containg special characters in their name (like whitespaces, colons, etc.)
- Install cryptix dependency in MuleSoft's Maven Repo
- Improve file path URI resolution handling
- Refactor SmbClient class (move helper methods to a different class)
- Fix  SmbClient helper methods that may be duplicated
- Implement isConnected method in SmbClient class
- Replace with overwrite does not work when renaming directories
- Add MIME Type to SmbDirectorySource
- SmbUtils: check if declared methods are defined somewhere else
- Verify if the share root can be assumed as being the working directory (maybe this concept of working directory does not apply in SMB, as opposed to sFTP)
- The logger operation should be implemented as a LogAppender
- Revise some unit tests (confirm if behavior is as expected)
- Review unit tests that are still failing (9 out of 170 - 1 ignored)
- Define a decent icon to represent the SMB Connector
