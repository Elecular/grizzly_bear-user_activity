# User Activity Service

![Test](https://github.com/nj20/grizzly_bear-user_activity/workflows/Test/badge.svg?branch=master) ![Deploy To GKE](https://github.com/nj20/grizzly_bear-user_activity/workflows/Deploy%20To%20GKE/badge.svg?branch=master)

### Introduction

This service is used for logging user activity and getting aggregated data.


### Workflows

This section will describe how to develop, test and push the application to staging and production

##### Development

* You can run the following command to start the **web service** and start making changes. The service will automatically pickup any changes! 
  ```
  npm run start:dev
  ```

* You can run the following command to spin up an environment for the **spark batch**
  ```
  docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
  ```
  Once you have spin up the docker containers, you can attach to the spark container
  ```
  docker exec -it batch_spark-batch_1 bash
  ```
  Then you can make some changes in your file and run teh batch
  ```
  sbt local/run
  ```


##### Testing

```
npm test
```

##### Pull Request

Once you make your changes, make a pull request and github action will automatically run tests on your branch. Once the pull request gets approval, you can merge the branch with master

##### Deploying On Stage/Prod

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

##### Deploy On Any K8s Cluster

```
cd kubernetes/overlays/stage
curl -o kustomize --location https://github.com/kubernetes-sigs/kustomize/releases/download/v3.1.0/kustomize_3.1.0_linux_amd64
chmod u+x ./kustomize
./kustomize edit set image WEB_DOCKER_IMAGE=<image>
./kustomize edit set image BATCH_DOCKER_IMAGE=<image>
./kustomize build . | kubectl apply -f -
```

* Note that the images for WEB_DOCKER_IMAGE and BATCH_DOCKER_IMAGE can be found in app/Dockerfile and batch/Dockerfile respsectively
* You must setup a mongodb service and configure the config and secrets as shown below

###### Secrets To Configure

```
user-activity-db-secret:
  MONGODB_URL: <mongodb_url>
  MONGODB_DATABASE: <database name>
```