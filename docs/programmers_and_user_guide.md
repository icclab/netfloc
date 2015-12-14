# Introduction
Netfloc provides APIs and programmable interfaces to retrieve information from the OpenDaylight datastore repository and establish a per tenant specific network graph for end-to-end flow control between OpenStack VMs. Netfloc relies on the mdsal inventory and alignes to the ovsdb and openflowplugin bundles in the OpenDaylight project. It currently includes interfaces and their implementations for establishing a network graph based on the information retrieved from the mdsal repository. Netfloc relies completely on SDN and OpenFlow protocol to create abstractions of the underlying network and provide alternative libraries and flow patterns to the encapsulation protocols used in OpenStack environment. 

The goal of this document is to provide useful guide for network developers who wants to create applications using the Netfloc SDK or the APIs. Netfloc is currently in a preliminary stage including the network graph library, openflow plugin lldp discovery service and interfaces and their implementation to establish an end-to-end SDN based network discovery per tenant base. Also it includes the necesary interfaces to retrieve all the information regarding a current host in OpenStack (ports and tenants) and create and traverse the path between two termination points (two VMs in OpenStack). Specific description will be provided in the following sections to guide the developers in their testing and development process.


## Programmers Guide
This section describes the way a programmer can interact ith the Netfloc SDK. It is assumed that Netfloc is up and running and you also have a OpenFlow enabled devices or Mininet installed.

The [Netfloc_Restful_API_Specification_(PRELIMINARY)](./docs/netfloc_api_spec/netfloc.pdf) provides a reference page with the descriptions of the APIs and their usage (TBI). It includes a set of CRUD operations which 
can be used to retrieve the current tenants in the network, ports associated to that tenant, the end-to-end network path between two VMs including the ingress, egrees and all the subsequent intermediate ports. There is a possibility to define Flow Patterns (TBI) to be installed on all the bridges of a network path. Netfloc APIs provide abstractions and operations on Network Paths via Flow Patterns. The network graph is updated dynamically, once a network is instantiated in OpenStack.

### Network Graph
The Network Graph structure is maintained dynamically via Opendaylight APIs (OVSDB Southbound, Neutron, Openflowplugin). It represents the whole managed OVS network which is connected to the Opendaylight controller via the standard TCP ports:

```
ovs-vsctl set-manager tcp:$CONTROLLER_IP:6640
ovs-vsctl set-controller $BRIDGE_NAME tcp:$CONTROLLER_IP:6633
```
A Netfloc can retrieve this information via the Java API (TBI) or REST (TBI).

### Network Path
The Network Graph creates Network Paths between Host Ports (provided by the Neutron API) to enable connection oriented flow programming. A Network Path consists of three logical segments:

- the source bridge with the respective source port (and link port)
- the destination bridge with the respective destination port (and link port)
- the aggregation bridges with link ports

The Network Graph will currently only provide the shortest Network Path between two hosts by performing a BFS. A Network Path is a dynamic view onto a host-to-host connection and is not explicitly bound to OVS devices.

### Flow Pattern (TBI)

Flow Patterns are a parameterized structure which can be applied on Network Paths and are thus also segmented into source, destination and aggregation parts. Flow Patterns which are applied to Network Paths are dynamically maintained by Netfloc as long as the respective host-to-host connection is achievable.

### Netfloc Applications (TBI)

Netfloc provides applications based on the following example use cases to harden and refine the underlying library:

- non tunnel based tenant isolation
- resilience
- network function chaining
- monitoring & administration 

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

## User Guide

As Netfloc is a SDK type software we refer to the [OpenDaylight user guide](https://www.opendaylight.org/sites/opendaylight/files/bk-user-guide.pdf).
