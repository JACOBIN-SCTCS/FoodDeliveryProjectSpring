from http import HTTPStatus
from threading import Thread
import requests

def getOrderStatus(orderId):
    order_url = "http://localhost:8081/order/"+str(orderId)
    http_response = requests.get(order_url)
    return http_response
