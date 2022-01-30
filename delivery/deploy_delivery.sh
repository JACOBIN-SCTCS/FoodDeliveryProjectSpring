docker build -t deliveryservice .
docker run -v "$(pwd)"/initialData.txt:/initialData.txt  -p 8080:8080 deliveryservice &

