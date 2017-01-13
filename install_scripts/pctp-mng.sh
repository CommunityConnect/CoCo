#we see this error due to conflict with the default ptcp port
#opendaylight-user@root>log:exception-display 
#org.apache.thrift.transport.TTransportException: Could not create ServerSocket on address 0.0.0.0/0.0.0.0:6644.

#workaround:
sudo ovs-vsctl set-manager ptcp:6655
