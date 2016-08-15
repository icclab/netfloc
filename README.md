<div align="center">
<a align="center" href='https://www.fiware.org/'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/netfloc.png" title="SESAME_logo" width=400px>
</a>
</div>

# What is Netfloc?

NETwork FLOws for Clouds (Netfloc) is SDN-based SDK for datacenter network programming. It is comprised of set of tools and libraries packed as Java bundles that interoperate with the OpenDaylight controller. Netfloc exposes REST API abstractions and Java interfaces for network programmers to enable optimal integration in cloud datacenters and fully SDN-enabled end-to-end management of OpenFlow enabled switches. 

A more extensive documentation can be found here:

[Installation and Administration Guide](http://netfloc.readthedocs.org/en/latest/installation_and_administration_guide/)

[User and Programmers Guide](http://netfloc.readthedocs.org/en/latest/user_and_programmers_guide/)
 
## Why OpenDaylight as SDN controller?

OpenDaylight is hosting one of the biggest growing community for network programability and NFV support that has gone beyond being just being SDN controller. It supports variety of networking projects, standards and protocols.

## Work in progress

Currently Netfloc provides library support for OpenStack tenant-based network graph management and traversal. It is based on the following OpenDaylight features: **mdsal, openflowplugin** and **ovsdb** (see [Architecture](#architecture). It also provides a support for the northbound OpenStack Neutron ML2 plugin APIs. Check [Netfloc status](#netfloc-status) for the latest updates and the [Roadmap](#roadmap) section for upcoming milestones.

## Architecture Design

[Netfloc Architecture Design](https://github.com/icclab/netfloc/blob/master/docs/architecture.md)

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

The SDK4SDN is implemented in Java programming language and comes with integrated OpenDaylight controller, version Lithium. It can be also used as standalone feature inside other OLD versions. It has been tested and supported using OpenStack Kilo and Juno releases.

To use SDK4SDN, the network needs to be fully SDN enabled through the OVS switches. The network interfaces of the switch should be connected with the interfaces of the OpenStack nodes and the ODL.

Installation of the following is required:

* JAVA 7 JDK
* Maven 3.1.1
* OpenFlow 1.3 enabled network devices
* OpenvSwitch
* Open Stack basic environment (ex. 3 nodes: compute, control, neutron)



## Installation and Testing


Clone and install Netfloc in the sdn-control node:

```
$git clone https://github.com/icclab/netfloc.git
$cd netfloc
$mvn clean install
```

To make sure all the state is clear before running Netfloc, the following steps and checkups are required:

* OVS running in all of the nodes (run ovs-vsctl show and check the configuration of the OVSs)* SDK not running: ./karaf/target/assembly/bin/status* Cleanup OpenStack environment (delete all: VMs, router interfaces, routers, and networks)
* Source the admin file: source keystonerc_admin* Make sure you delete the following directories in the SDN node:
```
rm -rf karaf/target/assembly/datarm -rf karaf/target/assembly/journalrm -rf karaf/target/assembly/snapshots
```

Configure the OVSs of each Open Stack node to connect to ODL on port 6633. Set up ODL as manager on port 6640:
```
ovs-vsctl set-manager tcp:[Controller_IP]:6640ovs-vsctl set-contorller tcp:[Controller_IP]:6633
```
After the above steps, start the SDN controller (Netfloc): 

```
./karaf/target/assembly/bin/start
```To monitor Netfloc logs in the ODL run: 

```
tail -f ./karaf/target/assembly/data/log/karaf.log
```
It displays the initialization process and the link discovery. Netfloc startup has completed when the log message shows: ***GraphListener***. 

The current version of Netfloc includes the Lithium version of the OpenDayligh controller. 
If you want to use Netfloc libraries in other ODL controllers generate SDK library from Netfloc:

```
$cd scripts
$./compile_sdk4sdn.sh
```

The zip folder contains all the necesarry **jar** files to be copied and extracted in the **deploy folder** of any ODL controller. 
A successful installation can be confirmed inside the karaf console as shown below:

```
opendaylight-user@root>bundle:list | grep netfloc
105 | Installed |  80 | 1.0.0.SNAPSHOT         | netfloc-api                                                        
106 | Installed |  80 | 1.0.0.SNAPSHOT         | netfloc-impl                                                       
107 | Active    |  80 | 1.0.0.SNAPSHOT         | netfloc-karaf                                                      
108 | Active    |  80 | 1.0.0.SNAPSHOT         | netfloc-features 
```
## Netfloc APIs

The Service Function Chain basic APIs are fully functional at the moment. Netfloc defines API Specification for the following network resources, for which development of Northbound APIs has been scheduled:

- Tenant filtered network graph
- All host ports
- End-to-end network path 
- Flow patterns on network paths
- Chain patterns 

For more detailed information: [Netfloc APIs](http://icclab.github.io/netfloc/docs/netfloc_api_spec/netfloc.html)

## Netfloc status

Currently Netfloc implements libraries to support for the following applications:

- **Isolation Application**:  Ensure end-to-end tenant segregation using novel non-GRE/VxLAN tunnelling mechanism for optimized packet header.
- **Resilience Application**: Provide direct SDN control on a physical level enabling fully SDN-managed infrastructure.
- **Service Function Chaining**: Traffic classification and steering support for NFVs. There is a test-case application based on this library. 


## Roadmap

Next milestone for Netfloc is to provide application examples based on the supported libraries. New libraries to be implemented:

- Service Function Chain resilience
- Inter-datacenter SFC support
- Advanced testing and monitoring features
- QoS support
- Expose all libraries via API and Java bundles


## License

Netfloc is licensed under the
[Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
See the file LICENSE.

## Made by

<div align="center" >
<a href='http://blog.zhaw.ch/icclab'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/se_sp_logo.jpg" title="icclab_logo" width=500px>
</a>
</div>

## Netfloc is part of FIWARE, T-Nova and SESAME

<div align="center">
<a href='https://www.fiware.org/'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/Logo-FIWARE.png" title="FIWARE_logo" width=230px>
</a>
<a href='https://www.fiware.org/'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/logo_tnova.jpg" title="T-Nova_logo" width=450px>
</a>
<a href='https://www.fiware.org/'>
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/sesame-logo.png" title="SESAME_logo" width=140px>
</a>
</div>








