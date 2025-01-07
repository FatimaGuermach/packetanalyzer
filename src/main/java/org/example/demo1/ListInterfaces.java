package org.example.demo1;

import org.pcap4j.core.*;
import java.util.List;

public class ListInterfaces {
            public static void main(String[] args) throws PcapNativeException {
        List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
        if (devices.isEmpty()) {
            System.out.println("No network interfaces found.");
            return;
        }

        for (int i = 0; i < devices.size(); i++) {
            PcapNetworkInterface device = devices.get(i);
            System.out.println(i + ": " + device.getName() + " (" + device.getDescription() + ")");
        }
    }
}
