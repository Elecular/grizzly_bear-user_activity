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
3. You can run the following command to spin up an environment for the **spark batch**
```
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
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

#### Testing

```
npm test
```

#### Pull Request

Once you make your changes, make a pull request and github action will automatically run tests on your branch. Once the pull request gets approval, you can merge the branch with master

#### Deploying On Stage/Prod

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

#### Deploy On Any K8s Cluster

```
cd kubernetes/overlays/stage
curl -o kustomize --location https://github.com/kubernetes-sigs/kustomize/releases/download/v3.1.0/kustomize_3.1.0_linux_amd64
chmod u+x ./kustomize
./kustomize edit set image WEB_DOCKER_IMAGE=<image>
./kustomize edit set image BATCH_DOCKER_IMAGE=<image>
./kustomize build . | kubectl apply -f -
```

* Note that the images for WEB_DOCKER_IMAGE and BATCH_DOCKER_IMAGE can be found in Dockerfile.web and Dockerfile.batch respsectively
* You must setup a mongodb service and configure the config and secrets as shown below

##### Config-Map To Configure

```
user-activity-config-map:
  SPARK_CLUSTER_MASTER: <address of master cluster for spark jobs>
```

##### Secrets To Configure

```
user-activity-db-secret:
  MONGODB_URL: <mongodb_url>
  MONGODB_DATABASE: <database name>
```

Secrets in a kubernetes cluster can be configured using thsi command

```
kubectl create secret generic user-activity-db-secret --from-literal=MONGODB_URL=<mongodb_url>--from-literal=MONGODB_DATABASE=<mongodb_database>
```