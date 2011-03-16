# how-to configure ubuntu with atheros AR5001 wifi as an access_point 
# with dynamic dtnd routing. assumes you have dtnd and tcl installed.
# in order to enable normal wifi again, you have to comment out the wlan0
# lines in /etc/network/interfaces, reboot, and stop hostapd & dnsmasq

apt-get install wireless-tools iw hostapd dnsmasq

cp ap_config/hostapd.conf /etc/
cp ap_config/dnsmasq.conf /etc/dnsmasq.d/
cp ap_config/dtnd-control /etc/
cp ap_config/on_dhcp_lease /etc/

edit /etc/dnsmasq.d/dnsmasq.conf to repersent the ip range you want to dnsmasq's dhcp server to serve

edit /etc/network/interfaces wlan0 ipaddress, etc... similar to how ap_config/interfaces is configured
