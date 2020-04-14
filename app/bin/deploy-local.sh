#!/bin/bash

# This script is used for deploying this service on a locally hosted kubernetes.
# You must have the following requirements inorder to run this script:
# 1. Have kubernetes running locally (Minikube for example). This can be tested with kubectl --help.
# 2. Install kustomize (Can use brew install kustomize). This can be tested with kustomize --help

cd "$(dirname "$0")"
cd ../..
 
# Buidling images
docker build -f Dockerfile.web --tag user-activity-web:dev .
docker build -f Dockerfile.batch --tag user-activity-batch:dev .

cd kubernetes/overlays/local

kustomize build . | kubectl apply -f -