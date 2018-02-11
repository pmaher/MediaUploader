To install the webservice project through maven:
mvn clean install

To run the webservice project: (spins up a local Tomcat server connected to an in memory H2 database)
java -jar target/MediaInfo-WebService-1.0-SNAPSHOT.jar

To make an example api call:
curl -H "Accept: application/json" 'http://localhost:8080/api/mediainfos'
or
curl -H "Accept: application/xml” 'http://localhost:8080/api/mediainfos/1'

Go directly to the mediainfos url in a browser to view all files uploaded:
http://localhost:8080/api/mediainfos

Or go directly to the webservice homepage to upload and view all uploads at:
http://localhost:8080/

The max upload filesize is 128KB (configurable via src/main/resources/application.properties)
The only acceptable format is *.mp3

