# MuleSoft SMB Extension

SMB Connector for Mule 4

## Publishing the Connector into Anypoint Exchange

It is recommended to publish the SMB connector in Anypoint Exchange so it can be easily discovered and reused by the organization.

Please, follow the steps described in the [official documentation](https://docs.mulesoft.com/exchange/to-publish-assets-maven) to publish the asset in Anypoint Exchange.

**IMPORTANT**: to skip unit tests, deploy the connector issuing the following command:

```
mvn deploy -DskipTests
```

## Running the demo

- Start a new Samba server docker image: 
```
sudo docker run -it -p 139:139 -p 445:445 -d dperson/samba -p  -u "mulesoft;mulesoft"  -s "shared;/shared;no;no;no;mulesoft" -w "WORKGROUP"
```

- Update the SMB Connector dependency declaration in pom.xml file

- Import the demo project in Anypoint Studio 7.x

- Start Anypoint Studio embedded Mule Runtime

- Send a request to the application: POST http://localhost:8081/write (payload can be anything, and it will be written to the file)


## Final Notes

Found and issue or had an exciting idea? Great! Feel free to fork this repo and create pull requests with bug fixes and/or feature implemetations. You can also submit [an issue](https://github.com/mulesoft-consulting/mule4-smb-connector-v2/issues), if you prefer!

Provide feedback, contribute and enjoy! :)
