package test;

import org.pcap4j.core.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

class Test {
    public static void main(String[] args) throws PcapNativeException {
        PcapNetworkInterface wifiInterface = getWifiInterface();

        if (wifiInterface != null) {
            System.out.println("Interface Wi-Fi sélectionnée : " + wifiInterface.getName());
            System.out.println("Description : " + wifiInterface.getDescription());
        } else {
            System.out.println("Impossible de détecter l'interface Wi-Fi !");
        }
    }

    /**
     * Détecte et sélectionne l'interface Wi-Fi en se basant sur le GUID récupéré via netsh.
     *
     * @return L'interface Wi-Fi correspondante ou null si non trouvée.
     */
    public static PcapNetworkInterface getWifiInterface() {
            try {
                // Étape 1 : Exécuter "netsh wlan show interfaces" pour obtenir le GUID de l'interface Wi-Fi
                String wifiGuid = null;
                Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    // Recherche d'une ligne contenant "Interface GUID" (ou équivalent selon la langue)
                    if (line.toLowerCase().contains("guid")) {
                        wifiGuid = line.split(":")[1].trim(); // Extraire le GUID
                        break;
                    }
                }

                // Si aucun GUID n'a été trouvé
                if (wifiGuid == null) {
                    System.out.println("GUID de l'interface Wi-Fi non trouvé !");
                    return null;
                }

                System.out.println("GUID Wi-Fi détecté : " + wifiGuid);

                // Étape 2 : Comparer avec les interfaces listées par Pcap4J
                List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
                if (allDevs == null || allDevs.isEmpty()) {
                    System.out.println("Aucune interface réseau trouvée !");
                    return null;
                }

                for (PcapNetworkInterface nif : allDevs) {
                    if (nif.getName().toLowerCase().contains(wifiGuid.toLowerCase())) {
                        return nif; // Correspondance trouvée
                    }
                }

                // Si aucune correspondance
                System.out.println("Aucune interface Pcap4J ne correspond au GUID Wi-Fi !");
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }
}
