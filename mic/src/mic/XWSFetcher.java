package mic;

/**
 * Created by amatheny on 2/8/17.
 */
public class XWSFetcher {

    public static XWSList fetchFromUrl(String xwsUrl) {
        return Util.loadRemoteJson(xwsUrl, XWSList.class);
    }

    public static void main(String... args) {
        String xwsUrl = "https://yasb-xws.herokuapp.com/?f=Rebel%20Alliance&d=v4!s!8:120,-1,-1,-1:-1:-1:;1:-1,78:-1:-1:;4:-1,70:-1:-1:U.30;45:-1,40:-1:-1:&sn=Anti-Synergy";
        XWSList list = fetchFromUrl(xwsUrl);

        System.out.println(String.format("Got %d point %s list named %s", list.getPoints(), list.getFaction(), list.getName()));
        for (XWSList.XWSPilot pilot : list.getPilots()) {
            //String pilotStr = pilot.getName() + " (" + pilot.getShip() + ")";
            StringBuilder pilotStr = new StringBuilder();
            pilotStr.append(pilot.getName()).append(" (").append(pilot.getShip()).append(")");
            for (String upgradeType : pilot.getUpgrades().keySet()) {
                for (String upgradeName : pilot.getUpgrades().get(upgradeType)) {
                    //pilotStr += ", " + upgradeName;
                    pilotStr.append(", ").append(upgradeName);
                }
            }
            System.out.println(pilotStr.toString());
        }
    }
}
