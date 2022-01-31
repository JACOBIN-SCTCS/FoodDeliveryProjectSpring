docker build -t deliveryservice .
docker run -v "$(pwd)"/initialData.txt:/initialData.txt  -p 8081:8081 deliveryservice &

