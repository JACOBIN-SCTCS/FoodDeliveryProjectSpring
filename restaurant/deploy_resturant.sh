docker build -t resturantservice .
docker run -v "$(pwd)"/initialData.txt:/initialData.txt  -p 8080:8080 resturantservice &

