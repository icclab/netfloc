# What is Netfloc?

NETwork FLOws for Clouds (Netfloc) is SDK framework for datacenter network programming. It is comprised of set of tools and libraries packed as Java bundles that interoperate with the OpenDaylight controller. Netfloc exposes REST API abstractions and java interfaces for network programmers to enable optimal tenant traffic isolation in cloud datacenters and fully SDN-enabled end-to-end management of OpenFlow enabled switches. 

 
## Why OpenDaylight as SDN controller?

OpenDaylight is hosting one of the biggest growing community for network programability and NFV support that has gone beyond being just being SDN controller. It supports variety of networking projects, standards and protocols.

## Work in progress

Currently Netfloc provides library support for OpenStack tenant-based network graph management and traversal. It is based on the following OpenDaylight features: mdsal, openflowplugin and ovsdb (see Architecture and Roadmap.) It also provides a support for the northbound OpenStack Neutron ML2 plugin APIs. For upcoming milestones, check the Roadmap section.

# Architecture Design

[Netfloc Architecture Design](docs/architecture/README.md)


## Features

- Facilitate pluggable and chainable network functions as Java bundles
- End-to-end network control in datacenters
- Reduce the complexity and protocol overhead in cloud networks
- Increase tenant traffic efficiency
- Enable optimal data center network planing and design
- OpenStack Neutron ML2 plugin and northbound API support
- ODL Lithium support
- OpenFlow support


## Prerequisites

JAVA 7 JDK, Maven 3.1.1, Vagrant and VirtualBox.
To run tests you will need OpenFlow 1.3 enabled network devices or Mininet and OpenvSwitch.
You can use the following source to download Vagrant machine with Mininet and OVS.

```
$git clone https://github.com/illotum/vagrant-mininet.git 
$cd vagrant-mininet
$vagrant up && vagrant ssh

```

# Installation and Testing

``` 
#Clone the Netfloc repo:

$git clone https://github.com/icclab/netfloc.git
$cd netfloc
$mvn clean install

#Netfloc already includes the Lithium version of the OpenDayligh controller. 
To run it, all you have to do is:

$cd karaf/target/assembly/
$./bin/karaf

```

You should see the karaf console of the OpenDaylgith controller. 
As a simple test run the topology discovery feature by starting a Miniet topology in the Vagrant machine and then installing flows on the ovs switch for LLDP traffic discovery. Netfloc uses this feature to notify the graph library any time a new switch (aka link) has been inserted in the SDN environment.

``` 
$mn  --topo linear,3 --mac --controller remote,ip=[Netfloc_Machine_IP]

mininet> h1 ping h2

$sudo ovs-vsctl set-manager tcp:[Netfloc_Machine_IP]:6640

$sudo ovs-ofctl add-flow s3 dl_type=0x88cc,actions=output:controller

$sudo ovs-ofctl dump-flows [bridge_name]

$ovs-ofctl dump-flows

```

## Netfloc APIs

Currently Netfloc defines API Specification for the following network resources:
- Tenant filtered network graph
- All host ports
- End-to-end network path 
- Flow patterns on netowrk paths
For more detailed information: [Netfloc APIs](./docs/netfloc_api_spec/netfloc.html): 


## Roadmap

Next milestone for Netfloc is to provide application examples based on the libraries to showcase efficient management of OpenFlow enabled switches in datacenters, optimize tenant traffic isolation in datacenter cloud networks, leverage novel and improve existing networking functions. Some of the application in scope are:

- Isolation Application:  Ensure end-to-end tenant segregation using novel non-GRE/VxLAN tunneling mechanism for optimized packet header.
- Resilience Application: Provide direct SDN control on a physical level enabling fully SDN-managed infrastructure.
- Service Function Chaining: Traffic classification and steering support for NFVs.
- Advanced monitoring features for cloud datacenters based on OpenFlow. 

In paralel, support for the following features has been scheduled:

- Nefloc northbound API specification
- Advanced flow programming library based on OpenFlow
- Testing and monitoring tools


## License

Netfloc is licensed under the
[Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
See the file LICENSE.

## Made by

<div align="center" >
<a href='http://blog.zhaw.ch/icclab'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/icclab-logo.png" title="icclab_logo" width=400px>
</a>
</div>

# This project is part of FIWARE

<div align="center" >
<a href='https://www.fiware.org/'>
<img src="https://raw.githubusercontent.com/icclab/hurtle/master/docs/figs/logo-FIWARE.png" title="FIWARE_logo" width=400px>
</a>
</div>








