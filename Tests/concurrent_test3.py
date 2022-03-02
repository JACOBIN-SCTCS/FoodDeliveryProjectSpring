from http import HTTPStatus
from threading import Thread
import requests

# Check if a customer's wallet balance is consistent
# after we add and deduct the same amount from it concurrently.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result):  # First concurrent request

    # Add amount 500 to wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8080/refillItem", json={"restId": 101, "itemId":1, "qty":1})

    result["1"] = http_response


def t2(result):  # Second concurrent request

    # Deduct amount 500 from wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})

    result["2"] = http_response

def t3(result):  # First concurrent request

    # Add amount 500 to wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8082/addBalance", json={"custId": 301, "amount":400})

    result["1"] = http_response

def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Check balance of customer 301
    http_response = requests.get(
        f"http://localhost:8081/agent/201")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail1'

    res_body = http_response.json()

    agent_id1 = res_body.get("agentId")
    status1 = res_body.get("status")

    ### Parallel Execution Begins ###

    thread = [0 for i in range(300)]
    for i in range(100):
        thread[3*i] = Thread(target=t1, kwargs={"result": result})
        thread[(3*i)+1] = Thread(target=t2, kwargs={"result": result})
        thread[(3*i)+2] = Thread(target=t3, kwargs={"result": result})

    for i in range(100):
        thread[3*i].start()
        thread[(3*i)+1].start()
        thread[(3*i)+2].start()

    for i in range(100):
        thread[3*i].join()
        thread[(3*i)+1].join()
        thread[(3*i)+2].join()

    ### Parallel Execution Ends ###

    
    return 'Pass'


if __name__ == "__main__":

    print(test())