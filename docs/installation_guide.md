# Netfloc Instalation guide

## Prerequisites

JAVA 7 JDK, Maven 3.1.1, Vagrant and VirtualBox.
To run tests you will need OpenFlow 1.3 enabled network devices or Mininet and OpenvSwitch.
You can use the following source to download Vagrant machine with Mininet and OVS.

```
$git clone https://github.com/illotum/vagrant-mininet.git 
$cd vagrant-mininet
$vagrant up && vagrant ssh

```

## Installation and Testing

``` 
# Clone the Netfloc repo:

$git clone https://github.com/icclab/netfloc.git
$cd netfloc
$mvn clean install

# Netfloc already includes the Lithium version of the OpenDayligh controller. 
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

## Troubleshooting

```
#If you experience some errors in the build regarding some missing bundles or features, please run the following:

$rm -rf ~/.m2/repository/org/opendaylight/
$mvn clean install

If the problem happens when you introduced some changes and installed new bundles / features in Netfloc, try this:

$rm -rf journals snapshots
$bin/karaf clean

It will clean the distribution data store from the previous executions.

What you can also do once you work with a stable version of features and bundles and don't want Maven to check online for new artifacts is go offline mode after the successful build. For that you will need to have all the artifacts available in your Maven Local repo and use the dependency plugin's "go-offline". 

$mvn dependency:go-offline
$mvn clean install -o

```

