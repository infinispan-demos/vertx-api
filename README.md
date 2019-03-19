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

# Deploying on OpenShift

The new way of deploying a Infinispan is now using the [Operator framework](https://github.com/operator-framework).

Read this [Infinispan Simple Tutorial](https://github.com/infinispan/infinispan-simple-tutorials/tree/master/operator) 
to have it up and running!

The cluster created by the example above 'example-infinispan' is the host you need to use to connect with
 the hotrod client. Instead `localhost:11222` your server will be `example-infinispan:11222`
 This is configured by default in the code in [`CacheAccessVerticle`](./commons/src/main/java/cutenames/CacheAccessVerticle.java)

