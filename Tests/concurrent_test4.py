from http import HTTPStatus
from threading import Thread
import requests

# Check if the order delivered remains consistent when given concurrent requests to the same resource.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result, id, orderId):  # First concurrent request

    # Add amount 500 to wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8081/orderDelivered", json={"orderId": orderId})

    result[id] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Checks the status of agent 201
    http_response = requests.get(f"http://localhost:8081/agent/201")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail1'

    res_body = http_response.json()

    status1 = res_body.get("status")

    if (status1 != "signed-out"):
        return "Fail2"

    #Signing in Agent 201
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail3'

    # Checks the status of agent 201
    http_response = requests.get(f"http://localhost:8081/agent/201")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail4'

    res_body = http_response.json()

    status2 = res_body.get("status")

    if (status2 != "available"):
        return "Fail5"

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 3})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail6'

    res_body = http_response.json()

    orderId = res_body.get("orderId")

    # Requesting for another order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 3})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail7'

    res_body = http_response.json()

    orderId2 = res_body.get("orderId")

    # Get status of order
    http_response = requests.get(f"http://localhost:8081/order/{orderId}")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail8'

    res_body = http_response.json()

    agent_id3 = res_body.get("agentId")
    status3 = res_body.get("status")

    if (status3 != "assigned"):
        return "Fail9"

    if (agent_id3 != 201):
        return "Fail10"

    thread = [0 for i in range(10)]

    ### Parallel Execution Begins ###
    for i in range(10):
        thread[i] = Thread(target=t1, kwargs={"result": result, "id": i, "orderId": orderId})

    for i in range(10):
        thread[i].start()

    for i in range(10):
        thread[i].join()

    ### Parallel Execution Ends ###

    for i in range(1000):
        if result[i].status_code != HTTPStatus.CREATED:
            return "Fail11"



    # Get status of order
    http_response = requests.get(f"http://localhost:8081/order/{orderId2}")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail12'

    res_body = http_response.json()

    agent_id4 = res_body.get("agentId")
    status4 = res_body.get("status")

    if (status4 != "assigned"):
        return "Fail13"

    if (agent_id3 != 201):
        return "Fail14"
    
    return 'Pass'


if __name__ == "__main__":

    print(test())