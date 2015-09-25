# Architecture Design
## Interfaces

In the centre of a Netfloc application sits the Network Graph data structure which is maintained by the following APIs:

- OVS components such as bridges, ports and interfaces through the OVSDB Southbound Plugin.
- Host information through the Openstack Neutron API.
- Topology discovery through the Openflowplugin

<div align="center" >
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/netfloc_ifaces.png" title="Netfloc Interfaces" width=400px>
</div>

## Network Graph

The Network Graph models the network components seen by the OVSDB Southbound Plugin. The graph itself holds all the information given by the mentioned APIs and exposes it by providing Network Paths which model host-to-host connections.

Netfloc models connection based flow programming by providing Flow Patterns (TBI). A Flow Pattern maps to a Network Path where the OVS components are divided into three logical segments. The source resp. destination bridges and the aggregation bridges.

<div align="center" >
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/netfloc_graph.png" title="Netfloc Network Graph" width=400px>
</div>

## Example Story

A simple example story can be the creation of a Neutron Port via Openstack. Netfloc syncs the updates the Network Graph by listening to the OVSDB Southbound Plugin and the Neutron API. The stored Flow Patterns can then be applied to possible new host-to-host connections.

<div align="center" >
<img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/netfloc_wf.png" title="Netfloc Workflow" width=400px>
</div>