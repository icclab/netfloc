#!/bin/bash

cp api/target/netfloc-api-1.0.0-SNAPSHOT.jar sdk4sdn
cp features/target/netfloc-features-1.0.0-SNAPSHOT.jar sdk4sdn
cp impl/target/netfloc-impl-1.0.0-SNAPSHOT.jar sdk4sdn
cp karaf/target/netfloc-karaf-1.0.0-SNAPSHOT.jar sdk4sdn

zip -r sdk4sdn/sdk4sdn.zip sdk4sdn
rm sdk4sdn/*.jar


