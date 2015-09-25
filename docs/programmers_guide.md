# Introduction
Netfloc provides APIs and programmable interfaces to retrieve information from the OpenDaylight datastore repository and establish a per tenant specific network graph for end-to-end flow control between OpenStack VMs. Netfloc relies on the mdsal inventory and alignes to the ovsdb and openflowplugin bundles in the OpenDaylight project. It currently includes interfaces and their implementations for establishing a network graph based on the information retrieved from the mdsal repository. Netfloc relies completely on SDN and OpenFlow protocol to create abstractions of the underlying network and provide alternative libraries and flow patterns to the encapsulation protocols used in OpenStack environment. 

The goal of this document is to provide useful guide for network developers who wants to create applications using the Netfloc SDK or the APIs. Netfloc is currently in a preliminary stage including the network graph library, openflow plugin lldp discovery service and interfaces and their implementation to establish an end-to-end SDN based network discovery per tenant base. Also it includes the necesary interfaces to retrieve all the information regarding a current host in OpenStack (ports and tenants) and create and traverse the path between two termination points (two VMs in OpenStack). Specific description will be provided in the following sections to guide the developers in their testing and development process.


## Guide
This section describes the way a programmer can interact ith the Netfloc SDK. It is assumed that Netfloc is up and running and you also have a OpenFlow enabled devices or Mininet installed.

The [Netfloc_Restful_API_Specification_(PRELIMINARY)](./docs/netfloc_api_spec/netfloc.pdf) provides a reference page with the descriptions of the APIs and their usage. It includes a set of CRUD operations which 
can be used to retrieve the current tenants in the network, ports associated to that tenant, the end-to-end network path between two VMs including the ingress, egrees and all the subsequent intermediate ports. There is a possibility to define flow patterns to be installed on all the bridges of a network path that includes the matching source, destination and respective actions. Netfloc APIs provide abstractions and operations over physical and virtual resources filtered per specific host, tenant or over defined network path. The network graph is updated dynamically, once a network is instantiated in OpenStack. 



## Examples 

The following Example shows the format of the Apiary Blueprint Specification. Specifically it describes how to create a new Flow Pattern (TBI) which can later be applied to a Network Path.

```
POST http://private-f71da-test6192.apiary-mock.com/flowPattern
```

```JSON
{
    "srcBridge": [
        {
            "match": {
                "in_port": "srcHostPort",
                "hw_src": "srcMAC",
                "hw_dst": "dstMAC"
            },
            "actions": {
                "output": "nextLinkPort"
            }
        }
    ],
    "linkBridge": [
        {
            "match": {
                "hw_dst": "dstMAC"
            },
            "actions": {
                "output": "nextLinkPort"
            }
        }
    ],
    "dstBridge": [
        {
            "match": {
                "hw_dst": "dstMAC"
            },
            "actions": {
                "output": "dstHostPort"
            }
        }
    ]
}
```




