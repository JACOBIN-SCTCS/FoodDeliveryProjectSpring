#!/bin/bash
eval $(minikube docker-env)
kubectl delete -n default deployment kawinwallet
kubectl delete -n default deployment kawinrestaurant
kubectl delete -n default deployment kawindatabase
kubectl delete -n default deployment kawindelivery

kubectl delete -n default service kawindatabase
kubectl delete -n default service kawindatabasewebserver
kubectl delete -n default service kawinrestaurant
kubectl delete -n default service kawinwallet
kubectl delete -n default service kawindelivery

docker image rm kawinwallet
docker image rm kawinrestaurant
docker imge rm kawindatabase

minikube stop