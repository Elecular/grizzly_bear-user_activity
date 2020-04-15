# User Activity Service

![Test](https://github.com/nj20/grizzly_bear-user_activity/workflows/Test/badge.svg?branch=master) ![Deploy To GKE](https://github.com/nj20/grizzly_bear-user_activity/workflows/Deploy%20To%20GKE/badge.svg?branch=master)

---

### Development

#### Web Development

You can run the following command to start the **web service** in development mode. The service will automatically pickup any changes! 
  ```
  npm run start:dev
  ```

#### Batch Development

1. You must first install [Google Cloud CLI](https://cloud.google.com/sdk/docs/downloads-versioned-archives)
2. You must then authenticate docker to use the google cloud registry using the following command
```
gcloud auth configure-docker
```
3. You can run the following command in the batch folder to spin up an environment for the **spark batch**
```
docker-compose pull && docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```
4. Once you have spin up the docker containers, you can attach to the spark container
```
docker exec -it batch_spark-batch_1 bash
```
5. Then you can make some changes in your file and run the batch
```
sbt "local/run BatchName"
```

---

### Testing

For **all** tests

```
npm test
```

For **unit** and **integration** tests

```
npm test:jest
```

For **acceptance** tests

```
npm test:acceptance
```

---

### Deloying Docker Image

#### Web Image

The Dockerfile is located in app/Dockerfile. This can be used to deploy a container for this service. In order for the container to work properly, you must pass in the following environment variables:

1. MONGODB_URL : URL of the mongodb database
2. MONGODB_DATABASE : A Database name.

Here is an example docker-compose file that is deploying this docker image on port 80:

```
version: "3"
services:
    web:
        depends_on:
            - db
        image: user-activity
        ports:
            - "80:3000"
        volumes:
            - ./:/code/
            - /code/node_modules
        links:
            - "db:database"
        environment:
            MONGODB_URL: mongodb://username:password@database:27017
            MONGODB_DATABASE: user_activity
    db:
        image: mongo
        environment:
            MONGO_INITDB_ROOT_USERNAME: username
            MONGO_INITDB_ROOT_PASSWORD: password
```

To deploy the image with some mock data, you must start the service with the following command. Normally, the service just starts with npm start, but in this case, we are passing some additional command line arguments inorder to add mock data.

```
npm start -- \
  --randomDataDump \
  --projects 5e865ed82a2aeb6436f498dc,5e865ed82a2aeb6436f498de,5e865ed82a2aeb6436f498d7,5e865ed82a2aeb6436f498dd \
  --segments one,two,three,four \
  --userActions buy,click,view \
  --userVolume 50 --sessionVolume 200 --activityVolume 1000 \
  --minTimestamp 79839129600000 --maxTimestamp 79839302400000
```

#### Batch Image

The Dockerfile is located at batch/Dockerfile. The dockerfile can be used to create a container and run batches in this container. You must pass the following environment variables:
```
MASTER: <master url of spark cluster (to run locally, set it to local)>
MONGODB_URL: <mongodb-url>
MONGODB_DATABASE: <mongodb-database>
PRIVATE_EXPERIMENTS_SERVICE_HOST: <url of private app of https://github.com/nj20/grizzly_bear-experiments>
PRIVATE_EXPERIMENTS_SERVICE_PORT: <port of private app of https://github.com/nj20/grizzly_bear-experiments>
```

You can set the CMD of this Dockerfile to the following command to run any batch:
```
./spark-2.4.5-bin-hadoop2.7/bin/spark-submit --class "UserActivityProcessor" target/scala-2.11/build.jar <name of the batch>
```

You can find an example docker-compose file for this image in batch/docker-compose.yml


---

### Deploying App and Batch Locally On K8s

We can deploy this service locally on k8s. You must have first installed the following software

1: [kustomize](https://kustomize.io/): Used for making k8s template files

```
kustomize version
```

2: Local K8s Cluster: You can install this through [Minikube](https://kubernetes.io/docs/setup/learning-environment/minikube/) or [Docker Desktop](https://www.docker.com/products/docker-desktop).

3: [Docker](https://www.docker.com/)

Once you have installed these software you must

1. Create a MongoDB service (Either in the your local k8s cluster or anywhere else you like)
2. Configure the following secret in your local k8s cluster

```
user-activity-db-secret:
  MONGODB_URL: <mongodb_url>
  MONGODB_DATABASE: <database name>
```

3. Run the following command. This will deploy that web app and schedule all the batch jobs

```
./app/bin/deploy-local.sh
```

---

### Deploying On Stage/Prod

Once the branch is merged, you can run the following commands to deploy the applocation on stage and then on prod.

```
./app/bin/deploy-stage.sh <github-auth-token>
```
```
./app/bin/deploy-prod.sh <github-auth-token>
```
If the github-auth-token is invalid, the command will return the following response:
```
{
  "message": "Bad credentials",
  "documentation_url": "https://developer.github.com/v3"
}
```

### Deploy On Any K8s Cluster

```
cd kubernetes/overlays/stage
kustomize edit set image WEB_DOCKER_IMAGE=<image>
kustomize edit set image BATCH_DOCKER_IMAGE=<image>
kustomize build . | kubectl apply -f -
```

* Note that the images for WEB_DOCKER_IMAGE and BATCH_DOCKER_IMAGE can be found in app/Dockerfile and batch/Dockerfile respsectively
* You must setup a mongodb service and configure the config and secrets as shown below

#### Config-Map To Configure

```
user-activity-config-map:
  SPARK_CLUSTER_MASTER: <address of master cluster for spark jobs>
```

#### Secrets To Configure

```
user-activity-db-secret:
  MONGODB_URL: <mongodb_url>
  MONGODB_DATABASE: <database name>
```

Secrets in a kubernetes cluster can be configured using this command

```
kubectl create secret generic user-activity-db-secret --from-literal=MONGODB_URL=<mongodb_url>--from-literal=MONGODB_DATABASE=<mongodb_database>
```