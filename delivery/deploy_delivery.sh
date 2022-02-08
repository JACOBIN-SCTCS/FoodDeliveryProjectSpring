docker build -t deliveryservice .
sudo docker run -v "$(pwd)"/initialData.txt:/initialData.txt -p 8081:8080 --rm --name deliveryservice-container deliveryservice &
