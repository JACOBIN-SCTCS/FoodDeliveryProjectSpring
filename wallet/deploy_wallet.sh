docker build -t walletservice .
docker run -v "$(pwd)"/initialData.txt:/initialData.txt  -p 8082:8082 walletservice &

