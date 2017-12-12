This is a simple example on how to build your first [Vert.x](http://www.vertx.io) using Infinispan.

Example include :
- REST API
- PUSH API

## Running locally 

- Download Infinispan server and unzip it
- Start Infinispan Standalone Server running `./infinispan-server/bin/standalone.sh` 

### REST API

- `cd resapi`
- run `mvn exec:java -Dexec.mainClass="cutenames.CuteNamesRestAPI"`

You can now put and get names using curl.

`curl -X POST -H "Content-Type: application/json" -d '{"id":"42", "name":"Oihana"}' "http://localhost:8081/api/cutenames"`

`curl -X GET -H "Content-Type: application/json" "http://localhost:8081/api/cutenames/42"`

`curl -X POST -H "Content-Type: application/json" -d '{"name":"Elaia"}' "http://localhost:8081/api/cutenames"`

### Push API
- cd `socketapi`
- run `mvn exec:java -Dexec.mainClass="cutenames.SendCuteNamesAPI"`

### Running the client application

- `cd cute-react`
- run `npm install`
- run `npm start`
- Go to `http://localhost:9000/`

Each time a new name will be posted, the name will be displayed in the client application.


## Deploying the solution on Openshift

- Docker version 1.13.1. Above versions are not guaranteed to be fully working on Openshift
- OpenShift Client 3.6
- Kubernetes 1.6
- Kubetail 1.2.1

Docker daemon has to be running !

- Start Openshift cluster with the service catalog `./bin/start-openshift.sh`
- Start Infinispan cluster `./bin/start-infinispan.sh`
- Deploy the verticles `mvn fabric8:deploy`

The configuration of the client should be changed to go to the URL of Openshift instead of localhost.
TODO: Deploy the client app in Openshift