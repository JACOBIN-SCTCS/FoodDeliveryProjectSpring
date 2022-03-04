from http import HTTPStatus
from threading import Thread
import requests

# Check if the order delivered remains consistent when given concurrent requests to the same resource.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result):  # First concurrent request

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})

    result["1"] = http_response


def t2(result):  # First concurrent request

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    result["2"] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 302, "restId": 101, "itemId":1, "qty": 1})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'

    # Requesting for another order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail2'

    res_body = http_response.json()


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail3"

    # Get status of order
    http_response = requests.get(f"http://localhost:8081/order/1001")

    if(http_response.status_code != HTTPStatus.NOT_FOUND):
        return 'Fail4'

    # Get status of order
    http_response = requests.get(f"http://localhost:8081/order/1000")

    if(http_response.status_code == HTTPStatus.NOT_FOUND):
        # Requesting for order
        http_response = requests.post(
            "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
        
        if(http_response.status_code != HTTPStatus.CREATED):
            return 'Fail5'

        orderId3 = res_body.get("orderId")

        if orderId3 != 1000:
            return 'Fail6'
    elif (http_response.status_code == HTTPStatus.OK):
        # Requesting for order
        http_response = requests.post(
            "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
        
        if(http_response.status_code != HTTPStatus.CREATED):
            return 'Fail7'

        orderId3 = res_body.get("orderId")

        if orderId3 != 1001:
            return 'Fail8'
    
    return 'Pass'


if __name__ == "__main__":

    print(test())