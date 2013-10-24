# AirMAX vs. MikroTik

Use-Case: Transmit data to a local network by using an outdoor wireless setup. The setup has to be very simple (PtP OSI Layer 2, PtMP not required) and must cover at least 4nm. The offshore antenna must be very small and must be able to provide functionality defined in OSI layer 3.

## Comparison of hardware

Specifications from http://www.ubnt.com/downloads/datasheets/bulletm/bm_ds_web.pdf, http://i.mt.lv/routerboard/files/Groove-ug.pdf and here http://i.mt.lv/routerboard/files/metal-52.pdf. Transmission based on TDMA (http://en.wikipedia.org/wiki/Time_division_multiple_access).

<table border="1" cellpadding="1" cellspacing="1" style="width:100%">
	<thead>
		<tr>
			<th scope="col">&nbsp;</th>
			<th scope="col"><span style="font-weight:normal">Bullet M Standard</span></th>
			<th scope="col"><span style="font-weight:normal">Bullet M Titanium</span></th>
			<th scope="col"><span style="font-weight:normal">MikroTik Groove</span></th>
			<th scope="col"><span style="font-weight:normal">MikroTik Metal</span></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Band</td>
			<td colspan="2" rowspan="1" style="text-align:center">2412-2462 Hz (M2), 5170-5825 Hz (M5)</td>
			<td colspan="2" rowspan="1" style="text-align:center">2192-2732 Hz (2Hn, 2SHPn), 5170-5825 Hz&nbsp;(5Hn,&nbsp;5SHPn)&nbsp;</td>
		</tr>
		<tr>
			<td>Case</td>
			<td style="text-align:center">Plastic</td>
			<td style="text-align:center">Aluminium</td>
			<td style="text-align:center">Plastic</td>
			<td style="text-align:center">Metal</td>
		</tr>
		<tr>
			<td>Antenna Connector</td>
			<td colspan="4" rowspan="1" style="text-align:center">N-type Male Jack (no special antenna required)</td>
		</tr>
		<tr>
			<td>CPU</td>
			<td colspan="4" rowspan="1" style="text-align:center">400 MHz (Aetheros)</td>
		</tr>
		<tr>
			<td>RAM</td>
			<td colspan="2" rowspan="1" style="text-align:center">32 MB, 8 MB Flash</td>
			<td rowspan="1" style="text-align:center">32 MB (2Hn), 64MB (A-2Hn)</td>
			<td rowspan="1" style="text-align:center">64 MB</td>
		</tr>
		<tr>
			<td>Network</td>
			<td colspan="4" rowspan="1" style="text-align:center">1 Port 10/100 MBit</td>
		</tr>
		<tr>
			<td>Dimension</td>
			<td style="text-align:center">152x37x31mm</td>
			<td style="text-align:center">XX</td>
			<td colspan="2" rowspan="1" style="text-align:center">177x44x44mm</td>
		</tr>
		<tr>
			<td>Weight</td>
			<td style="text-align:center">180g</td>
			<td style="text-align:center">196g</td>
			<td colspan="2" rowspan="1" style="text-align:center">193g</td>
		</tr>
		<tr>
			<td>Power</td>
			<td colspan="2" rowspan="1" style="text-align:center">12 to 24V, 0,5A PoE</td>
			<td rowspan="1" style="text-align:center">8-28V, 0,5A PoE</td>
			<td rowspan="1" style="text-align:center">8-30V, 0,5 A PoE</td>
		</tr>
		<tr>
			<td>Consumption</td>
			<td style="text-align:center">7-11W</td>
			<td style="text-align:center">6-11W</td>
			<td style="text-align:center">5-11W</td>
			<td style="text-align:center">4-11W</td>
		</tr>
		<tr>
			<td>Modes</td>
			<td colspan="4" rowspan="1" style="text-align:center">Station, Access Point, AP Repeater</td>
		</tr>
		<tr>
			<td>TX Power / Bandwith</td>
			<td colspan="2" rowspan="1" style="text-align:center">
			<p>2 GHz</p>

			<p>802.11b/g: 28dBm@6Mbps to 23dBm@54Mbps<br />
			802.11n: 28dBm@2Mbps to&nbsp;&nbsp; &nbsp;22dBm@65Mbps</p>

			<p>5 GHz</p>

			<p>802.11b/g: 25dBm@6Mbps to 20dBm@54Mbps<br />
			802.11n: 25dBm@2Mbps to&nbsp;&nbsp; &nbsp;19dBm@65Mbps</p>
			</td>
			<td colspan="2" rowspan="1" style="text-align:center">
			<p>2GHz</p>

			<p>802.11b/g: 27dBm@6Mbps to 24dBm@54Mbps<br />
			802.11n: 27dBm@2Mbps to 20dBm@65Mbps</p>

			<p>5 GHz</p>

			<p>802.11b/g: 23dBm@6Mbps to 19dBm@54Mbps<br />
			802.11n: 22dBm@2Mbps to&nbsp;15dBm@65Mbps</p>
			</td>
		</tr>
		<tr>
			<td>Price</td>
			<td style="text-align:center">65,- EUR (M2+M5)</td>
			<td style="text-align:center">95,- EUR (M2+M5)</td>
			<td style="text-align:center">61,- EUR (2Hn), 55,- EUR (5Hn)</td>
			<td style="text-align:center">88,- EUR (2SHPn),&nbsp;90,- EUR (5SHPn)</td>
		</tr>
	</tbody>
</table>

## Comparison of software

<table border="1" cellpadding="1" cellspacing="1" style="width:100%">
	<thead>
		<tr>
			<th scope="col">&nbsp;</th>
			<th scope="col"><span style="font-weight:normal">AirOS</span></th>
			<th scope="col"><span style="font-weight:normal">RouterOS</span></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>URL</td>
			<td style="text-align: center;">http://www.ubnt.com/airos</td>
			<td style="text-align: center;">http://www.mikrotik.com/software.html&nbsp;(http://demo.mt.lv/webfig/)</td>
		</tr>
		<tr>
			<td>Wireless Modes</td>
			<td colspan="2" rowspan="1">
			<ul>
				<li>Access Point</li>
				<li>Station/Client</li>
				<li>AP-Repeater</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Network Modes</td>
			<td colspan="2" rowspan="1">
			<ul>
				<li>Transparent Layer 2 Bridge</li>
				<li>Router</li>
				<li>SOHO Router</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Wifi Configuration</td>
			<td colspan="2" rowspan="1">
			<ul>
				<li>WPA, WPA2, WEP</li>
				<li>Trasnmit Power Control</li>
				<li>Channel Shifting</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Network Configuration</td>
			<td colspan="2" rowspan="1">
			<ul>
				<li>DHCP</li>
				<li>PPoE</li>
				<li>Routing</li>
				<li>Firewall</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Device Configuration</td>
			<td colspan="2" rowspan="1">
			<ul>
				<li>SSH, Telnet, Web</li>
				<li>Performance Reporting</li>
				<li>Ping Watchdog</li>
				<li>dDNS</li>
			</ul>
			</td>
		</tr>
	</tbody>
</table>
