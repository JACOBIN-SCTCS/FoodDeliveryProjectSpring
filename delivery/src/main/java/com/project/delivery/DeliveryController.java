package com.project.delivery;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


class AgentRequest
{
    int agentID;

}


class Agent
{
    int agentId;
    int status;

    Agent(int agentId)
    {
        this.agentId = agentId;
        this.status = 0;
    }

}

@RestController
class DeliveryController 
{
    List<Agent> agentsList;

    DeliveryController()
    {
        agentsList = new ArrayList<Agent>();
        agentsList.add(new Agent(101));
        agentsList.add(new Agent(102));
        agentsList.add(new Agent(103));

    }

    @GetMapping("/random")
    String printHello()
    {
        String str = new String();
        str += agentsList.get(0).agentId;
        str += agentsList.size();
        return str;
        
    }
    
    @PostMapping(path="/random",consumes="application/json")
    void addAgent(@RequestBody AgentRequest request)
    {
        agentsList.add(new Agent(request.agentID));    
    }


}
