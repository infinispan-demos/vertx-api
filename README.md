This is a simple example on how to build your first [Vert.x](http://www.vertx.io) using Infinispan.

Examples include :
- Creating a simple REST API
- Creating and consuming a simple PUSH API
- Clustering with Infinispan

# Running these examples locally

REST and PUSH APi examples use [Client/Server mode](http://infinispan.org/docs/stable/user_guide/user_guide.html#client_server) and 
the [Hot Rod protocol](http://infinispan.org/docs/stable/user_guide/user_guide.html#hotrod:java-client).
Both examples need an Infinispan Server running locally.
You will need an Infinispan Server to run [CuteNamesRestAPITest](restapi/src/main/java/cutenames/CuteNamesRestAPI.java) and
[SendCuteNamesAPITest](socketapi/src/main/java/cutenames/SendCuteNamesAPI.java)

The clustered example, uses the [Vert.x Infinispan Cluster Manager](https://vertx.io/docs/vertx-infinispan/java/). Each node
of the clustered application uses an embedded infinispan instance. We don't need any server running. 
See the [readme](clustered/README.md) for more explanations.

## Install and Run an Infinispan Server

You can use docker or download the server manually.

### Running Infinispan Server with the docker image

For example, if you want to test with the version 9.3.0.Final:
 
```docker run -it -p 11222:11222 jboss/infinispan-server:9.3.0.Final```

### Running a downloaded Infinispan Server

- Download the infinispan server from [here](http://infinispan.org/download)
- Start Infinispan Standalone Server running `./infinispan-server/bin/standalone.sh` 

### REST API

- `cd restapi`
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

### Clustered Vert.x

Clustered Vert.x example showcases a dummy application in cluster mode using Infinispan cluster manager.
See the dedicated [readme file](clustered/README.md).

# Deploying on Openshift

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
