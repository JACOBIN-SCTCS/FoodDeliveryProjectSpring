import requests
from http import HTTPStatus
Pass = 'Pass'
Fail = 'Fail'

# Helper function for creating an order
def createOrder(customerid,restid):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : customerid,
        "restId" : restid,
        "itemId" : 2,
        "qty" : 1
    })
    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
        return(Fail,-1)

    orderid = http_response.json().get("orderId")
    if(orderid==-1):
        return(Fail,-1)
    
    return (test_result,orderid)


# Helper function for checking whether status of order matches with the required status
def checkOrderStatus(orderId,status,order_present=True):
    http_response = requests.get(f"http://localhost:8081/order/{orderId}")
    if order_present:
        if(http_response.status_code != HTTPStatus.OK):
            return False
        ret_status = http_response.json().get("status")
        if(ret_status!=status):
            return False   
    else:
        if(http_response.status_code != HTTPStatus.NOT_FOUND):
            return False    
    return True

# Helper function for checking whether the agent status is the desired or not
def checkAgentStatus(agentId,status):
    http_response = requests.get(f"http://localhost:8081/agent/{agentId}")
    agentStatus = http_response.json().get("status")
    if agentStatus!=status:
        return False
    return True    
