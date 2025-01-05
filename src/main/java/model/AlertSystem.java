package model;

public class AlertSystem {
    public void checkForIntrusion(String packetInfo) {
        if (packetInfo.contains("DoS")) {
            System.out.println("Alerte : attaque DoS détectée !");
        }
    }
}
