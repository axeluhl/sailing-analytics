# AirMAX vs. MikroTik

Use-Case: Transmit data to a local network by using an outdoor wireless setup. The setup has to be very simple (PtP OSI Layer 2, PtMP not required) and must cover at least 4nm. The offshore antenna must be very small and must be able to provide functionality defined in OSI layer 3.

## Comparison

Specifications from http://www.ubnt.com/downloads/datasheets/bulletm/bm_ds_web.pdf, http://i.mt.lv/routerboard/files/Groove-ug.pdf and here http://i.mt.lv/routerboard/files/metal-52.pdf. Transmission based on TDMA (http://en.wikipedia.org/wiki/Time_division_multiple_access).

<table border="1" cellpadding="1" cellspacing="1" style="width:100%">
	<thead>
		<tr>
			<th scope="col">&nbsp;</th>
			<th scope="col">Bullet M Standard</th>
			<th scope="col">Bullet M Titanium</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Band</td>
			<td colspan="2" rowspan="1" style="text-align: center;">2412-2462 Hz (M2), 5170-5825 Hz (M5)</td>
		</tr>
		<tr>
			<td>Case</td>
			<td style="text-align: center;">Plastic</td>
			<td style="text-align: center;">Aluminium</td>
		</tr>
		<tr>
			<td>Antenna Connector</td>
			<td colspan="2" rowspan="1" style="text-align: center;">N-type Male Jack (no special antenna required)</td>
		</tr>
		<tr>
			<td>CPU</td>
			<td colspan="2" rowspan="1" style="text-align: center;">400 MHz</td>
		</tr>
		<tr>
			<td>RAM</td>
			<td colspan="2" rowspan="1" style="text-align: center;">32 MB, 8 MB Flash</td>
		</tr>
		<tr>
			<td>Network</td>
			<td colspan="2" rowspan="1" style="text-align: center;">10/100 MBit</td>
		</tr>
		<tr>
			<td>Weight</td>
			<td style="text-align: center;">180g</td>
			<td style="text-align: center;">196g</td>
		</tr>
		<tr>
			<td>Power</td>
			<td colspan="2" rowspan="1" style="text-align: center;">Up to 24V, 0,5A PoE (Adapter included)</td>
		</tr>
		<tr>
			<td>Consumption</td>
			<td style="text-align: center;">7W</td>
			<td style="text-align: center;">6W</td>
		</tr>
		<tr>
			<td>Modes</td>
			<td colspan="2" rowspan="1" style="text-align: center;">Station, Access Point, AP Repeater</td>
		</tr>
	</tbody>
</table>

<table border="1" cellpadding="1" cellspacing="1" style="width:100%">
	<thead>
		<tr>
			<th scope="col">&nbsp;</th>
			<th scope="col">AirOS</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Wireless Modes</td>
			<td>
			<ul>
				<li>Access Point</li>
				<li>Station/Client</li>
				<li>AP-Repeater</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Network Modes</td>
			<td>
			<ul>
				<li>Transparent Layer 2 Bridge</li>
				<li>Router</li>
				<li>SOHO Router</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Wifi Configuration</td>
			<td>
			<ul>
				<li>WPA, WPA2, WEP</li>
				<li>Trasnmit Power Control</li>
				<li>Channel Shifting</li>
			</ul>
			</td>
		</tr>
		<tr>
			<td>Network Configuration</td>
			<td>
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
			<td>
			<ul>
				<li>SSH, Telnet, Web</li>
				<li>Performance Reporting</li>
				<li>Location Sensor</li>
				<li>Ping Watchdog</li>
				<li>dDNS</li>
			</ul>
			</td>
		</tr>
	</tbody>
</table>