from http import HTTPStatus
import requests
import random
from helper.helper import *

# Check if the next highest agent available is assigned to an order 
# after an agent signs out while orders are coming


Pass = 'Pass'
Fail = 'Fail'

def test():
    test_result = Pass

    '''
        Reinitialize all the servies.
    '''
    http_response = requests.post("http://localhost:8080/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    agents = [201,202,203]
    random.shuffle(agents)  # Shuffle the signin order of  agents

    for agent in agents:
        http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": agent})

        result = checkAgentStatus(agent,"available")
        if result==False:
            return Fail
    
    # Customer 301 makes an order
    test_result,order_id1 = createOrder(301,101)
    if(test_result==Fail):
        return Fail
  
    
    # Order made by 301 must be assigned as there are free agents
    status = checkOrderStatus(order_id1,"assigned")
    if status==False:
        return Fail

    
    # Customer 302 makes an order
    test_result,order_id2 = createOrder(302,101)
    if(test_result==Fail):
        return Fail
    
    # Order made by 302 must also be assigned due to free agents availability
    status = checkOrderStatus(order_id2,"assigned")
    if status==False:
        return Fail

    # Agent 201 must be unavailable according to the policy
    result = checkAgentStatus(201,"unavailable")
    if result==False:
        return Fail

    # Agent 202 must be unavailable according to the policy
    result = checkAgentStatus(202,"unavailable")
    if result==False:
        return Fail
    
    # Agent 203 must be free as there are no pending orders
    result = checkAgentStatus(203,"available")
    if result==False:
        return Fail

    return test_result


if __name__ == "__main__":
    test_result = test()
    print("TEST next_highest_agentid_assigned : " + test_result)
