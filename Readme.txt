Steps to run the services
###########################

    (The path to /initialData.txt in host is taken as ~/Downloads/initialData.txt)

    Resturant Service 
        1) cd Restaurant
        2) ./mvnw package
        3) docker build -t restaurant-service .
        4) docker run -p 8080:8080 --rm --name restaurant --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt restaurant-service
        To stop the container
            docker stop restaurant
        To remove the Docker Image
            docker image rm restaurant-service
    
    Delivery Service
        1) cd Delivery
        2)./mvnw package
        3) docker build -t delivery-service .
        4) docker run -p 8081:8080 --rm --name delivery --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt delivery-service

        To stop the container
            docker stop delivery
        To remove the Docker Image
            docker image rm delivery-service

    Wallet Service
        1) cd Wallet
        2) ./mvnw package
        3) docker build -t wallet-service .
        4) docker run -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt wallet-service

        To stop the container
            docker stop wallet
        To remove the Docker Image
            docker image rm wallet-service


Steps to run the test cases
##############################
    Language used for Tests : Python 3
    Packages used  

        1. requests
            Installation command 
                pip3 install requests 
                        OR  
                pip install requests

    
    To run either Public/Private Test Cases all at once
        1. cd Tests
        2. python3 run_tests.py
                OR
           python run_tests.py


    To run Individual Public Test Cases
        1. cd Tests 
        2. cd public-test-cases
        3. python3 <filename>.py
                OR
           python <filename>.py

    To run Individual  Private Test Cases
        1. cd Tests
        2. cd self-test-cases
        3. python3 <filename>.py
                OR
           python <filename.py>
    



