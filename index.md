---
layout: default
---

[What is Netfloc?](https://github.com/icclab/netfloc/) NETwork FLOws for Clouds (Netfloc) is a framework for datacenter network programming. It is comprised of set of tools and libraries packed as Java bundles that interoperate with the OpenDaylight controller. Netfloc exposes REST API abstractions and Java interfaces for network programmers to enable optimal integration in cloud datacenters and fully SDN-enabled end-to-end management of OpenFlow enabled switches.
 
## Why OpenDaylight as SDN controller?

OpenDaylight is hosting one of the biggest growing community for network programability and NFV support that has gone beyond being just being SDN controller. It supports variety of networking projects, standards and protocols.

## Work in progress

Currently Netfloc provides library support for OpenStack tenant-based network graph management and traversal. It is based on the following OpenDaylight features: mdsal, openflowplugin and ovsdb (see Architecture and Roadmap.) It also provides a support for the northbound OpenStack Neutron ML2 plugin APIs. For upcoming milestones, check the Roadmap section.

## Architecture Design

Check the <a href="https://github.com/icclab/netfloc/blob/master/docs/architecture.md/">Netfloc architecture</a> for overall architecture, components and their implementation details and examples.


## Features

- Facilitate pluggable and chainable network functions as Java bundles
- End-to-end network control in datacenters
- Reduce the complexity and protocol overhead in cloud networks
- Increase tenant traffic efficiency
- Enable optimal data center network planing and design
- OpenStack Neutron ML2 plugin and northbound API support
- ODL Lithium support
- OpenFlow support


## See further details on:

<p><a href="https://github.com/icclab/netfloc/blob/master/docs/installation_guide.md/">Netfloc instalation and adminisatrion</a></p>
<p><a href="https://github.com/icclab/netfloc/blob/master/docs/programmers_guide.md">Netfloc programmers guide</a></p>


## Netfloc APIs

Currently Netfloc defines [API Specification](https://github.com/icclab/netfloc/blob/master/docs/netfloc_api_spec/netfloc.html) for the following network resources:


- Tenant filtered network graph
- All host ports
- End-to-end network path 
- Flow patterns on netowrk paths

## Roadmap

Next milestone for Netfloc is to provide application examples based on the libraries to showcase efficient management of OpenFlow enabled switches in datacenters, optimize tenant traffic isolation in datacenter cloud networks, leverage novel and improve existing networking functions. Some of the application in scope are:

- Isolation: Ensure end-to-end tenant segregation using novel non-GRE/VxLAN tunneling mechanism for optimized packet header.
- Resilience: Provide direct SDN control on a physical level enabling fully SDN-managed infrastructure.
- Service Function Chaining: Traffic classification and steering support for NFVs.
- Advanced monitoring for cloud datacenters based on OpenFlow. 

In paralel, support for the following features has been scheduled:

- Nefloc northbound API specification
- Advanced flow programming library based on OpenFlow
- Testing and monitoring tools


### License

Netfloc is licensed under the [Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

<footer>
        <p>This project is made and maintained by <a href="https://github.com/icclab">icclab</a></p>
        <p><img src="https://raw.githubusercontent.com/icclab/netfloc/master/docs/img/icclab-logo.png"></p>
        <p><small>Hosted on <a href="http://chibicode.github.io/solo/">GitHub Pages</a> &mdash; Theme by <a href="https://github.com/chibicode/solo">chibicode</a>
        </small>
        </p>
</footer>


<div class="github-fork-ribbon-wrapper right fixed" style="width: 150px;height: 150px;position: fixed;overflow: hidden;top: 0;z-index: 9999;pointer-events: none;right: 0;"><div class="github-fork-ribbon" style="position: absolute;padding: 2px 0;background-color: #333;background-image: linear-gradient(to bottom, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.15));-webkit-box-shadow: 0 2px 3px 0 rgba(0, 0, 0, 0.5);-moz-box-shadow: 0 2px 3px 0 rgba(0, 0, 0, 0.5);box-shadow: 0 2px 3px 0 rgba(0, 0, 0, 0.5);z-index: 9999;pointer-events: auto;top: 42px;right: -43px;-webkit-transform: rotate(45deg);-moz-transform: rotate(45deg);-ms-transform: rotate(45deg);-o-transform: rotate(45deg);transform: rotate(45deg);"><a href="https://github.com/icclab/netfloc/" style="font: 700 13px &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif;color: #fff;text-decoration: none;text-shadow: 0 -1px rgba(0, 0, 0, 0.5);text-align: center;width: 200px;line-height: 20px;display: inline-block;padding: 2px 0;border-width: 1px 0;border-style: dotted;border-color: rgba(255, 255, 255, 0.7);">Fork me on GitHub</a></div></div>
