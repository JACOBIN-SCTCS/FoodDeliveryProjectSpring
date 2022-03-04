#!/bin/bash
minikube start --extra-config=kubelet.housekeeping-interval=10s 
minikube addons enable metrics-server
eval $(minikube docker-env)


# Initialize database
cd Database
bash createDatabaseService.sh 
kubectl apply -f database.yaml
# Expose H2 Web Console
sleep 5
kubectl expose deployment kawindatabase --name=kawindatabasewebserver --type=LoadBalancer --port=8082
kubectl expose deployment kawindatabase --type=LoadBalancer --port=9092

cd ..
cd Delivery
./mvnw package -Dmaven.test.skip=true
docker build -t kawindelivery .
kubectl apply -f delivery.yaml 
sleep 5
kubectl expose deployment kawindelivery --type=LoadBalancer --port=8080
kubectl apply -f auto_scale.yaml

# Navigate to wallet service
cd ..
cd Wallet/
./mvnw package
docker build -t kawinwallet .
kubectl apply -f wallet.yaml
sleep 5
kubectl expose deployment kawinwallet --type=LoadBalancer --port=8080

# Navigate to restaurant service
cd ..
cd Restaurant/
./mvnw package
docker build -t kawinrestaurant .
kubectl apply -f restaurant.yaml
sleep 5
kubectl expose deployment kawinrestaurant --type=LoadBalancer --port=8080


echo "Port Forwarding in progress"
sleep 7
kubectl port-forward deployment/kawinwallet 8082:8080 &
kubectl port-forward deployment/kawinrestaurant 8080:8080 &
kubectl port-forward deployment/kawindelivery 8081:8080 &
kubectl port-forward deployment/kawindatabase 8083:8082 &



