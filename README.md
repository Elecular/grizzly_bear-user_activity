# User Activity Service

![Test](https://github.com/nj20/grizzly_bear-user_activity/workflows/Test/badge.svg?branch=master)

![Deploy To GKE](https://github.com/nj20/grizzly_bear-user_activity/workflows/Deploy%20To%20GKE/badge.svg?branch=master)

### Introduction

This service is used for logging user activity and getting aggregated data about this data.

---

### Development And Deployment

##### Development

You can run the following command to start the service:


##### Deploy On Stage/Prod

Run the following command to deploy on stage:
```
user$ ./app/bin/deploy-stage.sh <github-auth-token>
```

If the github-auth-token is invalid, the command will return the following response:
```
{
  "message": "Bad credentials",
  "documentation_url": "https://developer.github.com/v3"
}
```

##### Deploy On Any K8s Cluster

This is how you deploy this application on a k8s cluster

```
user$ cd kubernetes/overlays/stage
user$ curl -o kustomize --location https://github.com/kubernetes-sigs/kustomize/releases/download/v3.1.0/kustomize_3.1.0_linux_amd64
user$ chmod u+x ./kustomize
user$ ./kustomize edit set image XXXX=<image>
user$ ./kustomize build . | kubectl apply -f -
```