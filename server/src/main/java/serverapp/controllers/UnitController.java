package serverapp.controllers;
import javafx.scene.control.Alert;
import org.json.JSONObject;
import serverapp.enums.FeatureNames;
import serverapp.enums.HexState;
import serverapp.enums.TerrainNames;
import serverapp.enums.UnitState;
import serverapp.models.Player;
import serverapp.models.gainable.Improvement;
import serverapp.models.maprelated.Hex;
import serverapp.models.maprelated.Movement;
import serverapp.models.units.Civilian;
import serverapp.models.units.Melee;
import serverapp.models.units.Military;
import serverapp.models.units.Ranged;
import serverapp.models.units.Settler;
import serverapp.models.units.Siege;
import serverapp.models.units.Unit;
import serverapp.models.units.Worker;

import java.util.ArrayList;

import java.util.Objects;
import java.util.Random;

import static serverapp.controllers.GameController.*;

public class UnitController {

    private static Unit selectedUnit;
    private static final Hex[][] hex = getWorld().getHex();
    private static final ArrayList<Movement> unfinishedMovements = new ArrayList<Movement>();

    public static ArrayList<Movement> getUnfinishedMovements() {
        return unfinishedMovements;
    }

    
    public static void setCurrentPlayer(Player player) {
        GameController.setCurrentPlayer(player);
    }

    public static boolean hasMilitary(int x, int y) {
        return GameController.getWorld().getHex()[x][y].getMilitaryUnit() != null;
    }

    public static boolean hasCivilian(int x, int y) {
        return GameController.getCiviliansByLocation(x, y) != null;
    }

    public static boolean canMove(Unit unit, Hex destination) {
        return Math.abs(unit.getCurrentHex().getX() - destination.getX() + unit.getCurrentHex().getY() - destination.getY()) == 1;
    }


    public static boolean canMoveThrough(int x, int y) {
        return !hex[x][y].getTerrain().getName().equals("Mountain") && !hex[x][y].getTerrain().getName().equals("Ocean");
    }

    public static boolean isHexOccupied(int destinationX, int destinationY) {
        Unit unit = selectedUnit;
        return (unit instanceof Military && hex[destinationX][destinationY].getMilitaryUnit() != null)
                || (unit instanceof Civilian && hex[destinationX][destinationY].getCivilianUnit() != null);
    }

    public static Unit getSelectedUnit() {
        return selectedUnit;
    }

    public static void setSelectedUnit(Unit selectedUnit) {
        UnitController.selectedUnit = selectedUnit;
    }

    private static void makeVisible(int x, int y, int[][] tempDirection, int i) {
        if (!isOutOfBounds(x + tempDirection[i][0], y + tempDirection[i][1])) {
            hex[x + tempDirection[i][0]][y + tempDirection[i][1]].setState(HexState.Visible, GameController.getCurrentPlayer());
        }
    }

    private static void changeView(int[][] direction, int x, int y) {
        for (int j = 0; j < direction.length; j++) {
            makeVisible(x, y, direction, j);
            int[][] tempDirection = getDirection(y + direction[j][1]);
            for (int i = 0; i < tempDirection.length; i++) {
                makeVisible(x + direction[j][0], y + direction[j][1], tempDirection, i);
            }
        }
    }

    private static void setRevealedTiles() {
        for (int i = 0; i < getWorld().getHexInWidth(); i++) {
            for (int j = 0; j < getWorld().getHexInHeight(); j++) {
                //    if (hex[i][j].getState(GameController.getCurrentPlayer()) == null)
                //        System.out.println("is null");
                if (hex[i][j].getState(GameController.getCurrentPlayer()).equals(HexState.Visible)) {
                    hex[i][j].setState(HexState.Revealed, GameController.getCurrentPlayer());
                    GameController.getCurrentPlayer().addToRevealedHexes(hex[i][j]);
                }
            }
        }
    }

    public static String constructRoad(int x, int y) {
        if (GameController.isOutOfBounds(x, y))
            return "chosen position is not valid";
        if (selectedUnit == null)
            return "you should choose a unit first";
        if (!selectedUnit.getName().equals("Worker"))
            return "you should choose a worker unit";
        if (!GameController.getCurrentPlayer().getAchievedTechnologies().get("TheWheel"))
            return "you don't have required technology for building roads";
        if (hex[x][y].hasRoad())
            return "this hex already has road";
        if (hex[x][y].getTerrain().getName().equals(TerrainNames.Mountain.getCharacter()))
            return "you can't construct road on mountain";
        if (hex[x][y].getTerrain().getName().equals(TerrainNames.Ocean.getCharacter()))
            return "you can't construct road on ocean";
        if (hex[x][y].getFeature() != null && hex[x][y].getFeature().getName().equals(FeatureNames.Ice.getCharacter()))
            return "you can't build road on ice";
        UnitController.getSelectedUnit().setOrdered(true);
        selectedUnit.setState(UnitState.Active);

        Improvement road = new Improvement("Road", selectedUnit, hex[x][y]);
        road.setLeftTurns(3);
        GameController.getCurrentPlayer().addUnfinishedProject(road);
        return "the road will be constructed in 3 turns";
    }

    public static String constructRailRoad(int x, int y) {
        if (GameController.isOutOfBounds(x, y))
            return "chosen position is not valid";
        if (!selectedUnit.getName().equals("Worker"))
            return "you should choose a worker unit";
        if (!GameController.getCurrentPlayer().getAchievedTechnologies().get("Road"))
            return "you don't have required technology for building roads";
        if (hex[x][y].hasRailRoad())
            return "this hex already has railroad";
        if (hex[x][y].getTerrain().getName().equals(TerrainNames.Mountain.getCharacter()))
            return "you can't construct railroad on mountain";
        if (hex[x][y].getTerrain().getName().equals(TerrainNames.Ocean.getCharacter()))
            return "you can't construct railroad on ocean";
        if (hex[x][y].getFeature() != null && hex[x][y].getFeature().getName().equals(FeatureNames.Ice.getCharacter()))
            return "you can't build railroad on ice";
        UnitController.getSelectedUnit().setOrdered(true);
        selectedUnit.setState(UnitState.Active);
        Improvement railroad = new Improvement("RailRoad", selectedUnit, hex[x][y]);
        railroad.setLeftTurns(3);
        GameController.getCurrentPlayer().addUnfinishedProject(railroad);
        return "the railroad will be constructed in 3 turns";
    }

    public static void makeUnit(String name, Hex hex, String type) {
        String theType = InitializeGameInfo.unitInfo.get(name).split(" ")[7];
        if (theType.equals("Settler")) {
            Settler newSettler = new Settler(name, hex, GameController.getCurrentPlayer());
            newSettler.build(type);
        } else if (theType.equals("Worker")) {
            Worker newWorker = new Worker(name, hex, GameController.getCurrentPlayer());
            newWorker.build(type);
        } else if (theType.equals("Ranged")) {
            Ranged newRanged = new Ranged(name, hex, GameController.getCurrentPlayer());
            newRanged.build(type);
        } else if (theType.equals("Siege")) {
            Siege newSiege = new Siege(name, hex, GameController.getCurrentPlayer());
            newSiege.build(type);
        } else if (theType.equals("Melee")) {
            Melee newMelee = new Melee(name, hex, GameController.getCurrentPlayer());
            newMelee.build(type);
        }

    }

    public static String setUpSiegeForRangeAttack() {
        if (selectedUnit == null) return "you did not select a unit";
        if (!(selectedUnit instanceof Siege)) return "selected unit is not a siege";
        selectedUnit.setState(UnitState.setUpForRangeAttack);
        selectedUnit.setOrdered(true);
        ((Siege) selectedUnit).setReadyToAttack(true);
        return "siege is ready for the next turn";
    }

    public static String fortify() {
        if (selectedUnit == null || selectedUnit instanceof Civilian) return "you did not select a military unit";
        if (Objects.equals(selectedUnit.getCombatType(), "Mounted")) return "a Mounted unit can not fortify";
        if (Objects.equals(selectedUnit.getCombatType(), "Armored")) return "a Armored unit can not fortify";
        selectedUnit.setState(UnitState.Fortified);
        if (selectedUnit.isFirstFortify()) {
            selectedUnit.increaseBounes((selectedUnit.getCombatStrength() * 125 / 100));
            selectedUnit.setFirstFortify(false);
        } else selectedUnit.increaseBounes((selectedUnit.getCombatStrength() * 150 / 100));
        return "fortified successfully";
    }

    public static String fortifyUtilHeal() {
        selectedUnit.setState(UnitState.FortifiedUntilHeal);
        return "fortified successfully";
    }

    public static String garrison() {
        //todo: move unit to capital city and move errors
        if (selectedUnit == null || selectedUnit instanceof Civilian) {
            return "you did not select a military unit";
        }
        if (selectedUnit.getCurrentHex().getCapital() == null) {
            return "there is no capital";
        }
        if (selectedUnit.getCurrentHex().getOwner() != GameController.getCurrentPlayer()) {
            return ("this is not your city");
        }
        selectedUnit.setOrdered(true);
        selectedUnit.setState(UnitState.Garrisoned);
        return "garrisoned successfully";
    }

    public static String alert() {
        selectedUnit.setState(UnitState.Alert);
        return "alerted successfully";
    }

    public static String deleteMilitaryUnit(Unit unit) {
        unit.getOwner().removeUnit(unit);
        unit.getCurrentHex().setMilitaryUnit(null);
        if(selectedUnit == unit) selectedUnit = null;
        return "unit deleted";
    }

    public static String deleteUnitAction() {
        Unit unit = selectedUnit;
        if (unit instanceof Civilian) unit.getCurrentHex().setCivilianUnit(null);
        else unit.getCurrentHex().setMilitaryUnit(null);
        unit.getOwner().removeUnit(unit);
        selectedUnit = null;
        GameController.getCurrentPlayer().increaseGold(unit.getCost() / 10);
        return "unit deleted";
    }

    public static String sleepUnit() {
        if (selectedUnit.getState() == UnitState.Sleep) {
            return "unit is already sleep";
        }
        selectedUnit.setState(UnitState.Sleep);
        return "successfully sleep";
    }

    public static String wakeUpUnit() {
        if (selectedUnit.getState() == UnitState.Sleep || selectedUnit.getState() == UnitState.Alert ||
                selectedUnit.getState() == UnitState.FortifiedUntilHeal || selectedUnit.getState() == UnitState.Fortified) {
            selectedUnit.setState(UnitState.Active);
            return "successfully waked up";
        }
        return "unit is already awake";
    }

    public static String pillage() {
        if (selectedUnit == null) return "select a military unit first";
        if (selectedUnit.getCurrentHex().getImprovement().size() == 0) return "there is no improvement";
        selectedUnit.setState(UnitState.Active);
        selectedUnit.setOrdered(true);
        selectedUnit.getCurrentHex().setPillaged(true);
        if (!selectedUnit.getCurrentHex().getImprovement().isEmpty()) reverseImprovement();
        return "pillaged successfully";
    }

    private static void reverseImprovement() {
        for (Improvement temp : selectedUnit.getCurrentHex().getImprovement()) {
            String type = temp.getName();
            switch (type) {
                case "Camp":
                    Improvement.reverseCamp(selectedUnit.getCurrentHex());
                    break;
                case "Farm":
                    Improvement.reverseFarm(selectedUnit.getCurrentHex());
                    break;
                case "Mine":
                    Improvement.reverseMine(selectedUnit.getCurrentHex());
                    break;
                case "Pasture":
                    Improvement.reversePasture(selectedUnit.getCurrentHex());
                    break;
                case "Plantation":
                    Improvement.reversePlantation(selectedUnit.getCurrentHex());
                    break;
                case "Quarry":
                    Improvement.reverseQuarry(selectedUnit.getCurrentHex());
                    break;
            }
        }
    }

    public static void forceEndMovement(Movement movement) {
        unfinishedMovements.remove(movement);
    }

    private static int[] getDirectionIndex(int[][] direction, int dx, int dy, Unit unit) {
        if (dx != 0) dx = dx / Math.abs(dx);
        if (dy != 0) dy = dy / Math.abs(dy);
        for (int[] ints : direction) {
            if (ints[0] == dx && ints[1] == dy
                    && !isOutOfBounds(unit.getCurrentHex().getX() + ints[0], unit.getCurrentHex().getY() + ints[1])
                    && !hex[unit.getCurrentHex().getX() + ints[0]][unit.getCurrentHex().getY() + ints[1]].getTerrain().getName().equals(TerrainNames.Mountain.getCharacter())
                    && !hex[unit.getCurrentHex().getX() + ints[0]][unit.getCurrentHex().getY() + ints[1]].getTerrain().getName().equals(TerrainNames.Ocean.getCharacter())
                    && !isHexOccupied(unit.getCurrentHex().getX() + ints[0], unit.getCurrentHex().getY() + ints[1]))
                return ints;
        }
        for (int[] ints : direction) {
            if ((ints[0] == dx || ints[1] == dy)
                    && !isOutOfBounds(unit.getCurrentHex().getX() + ints[0], unit.getCurrentHex().getY() + ints[1])
                    && !hex[unit.getCurrentHex().getX() + ints[0]][unit.getCurrentHex().getY() + ints[1]].getTerrain().getName().equals(TerrainNames.Mountain.getCharacter())
                    && !hex[unit.getCurrentHex().getX() + ints[0]][unit.getCurrentHex().getY() + ints[1]].getTerrain().getName().equals(TerrainNames.Ocean.getCharacter())
                    && !isHexOccupied(unit.getCurrentHex().getX() + ints[0], unit.getCurrentHex().getY() + ints[1]))
                return ints;
        }
        return null;
    }

    public static boolean checkForZOC(Hex current) {
        int[][] direction = getDirection(current.getY());

        for (int i = 0; i < direction.length; i++) {
            for (int j = i + 1; j < direction.length; j++) {
                Hex neighbor1, neighbor2;
                if (!isOutOfBounds(current.getX() + direction[i][0], current.getY() + direction[i][1])
                        && !isOutOfBounds(current.getX() + direction[j][0], current.getY() + direction[j][1])) {
                    neighbor1 = hex[current.getX() + direction[i][0]][current.getY() + direction[i][1]];
                    neighbor2 = hex[current.getX() + direction[j][0]][current.getY() + direction[j][1]];
                    int[][] tempDirection1 = getDirection(neighbor1.getY());
                    int[][] tempDirection2 = getDirection(neighbor2.getY());
                    Hex hex1, hex2;

                    for (int k1 = 0; k1 < tempDirection1.length; k1++) {
                        if (!isOutOfBounds(neighbor1.getX() + tempDirection1[i][0], neighbor1.getY() + tempDirection1[i][1])) {
                            hex1 = hex[neighbor1.getX() + tempDirection1[i][0]][neighbor1.getY() + tempDirection1[i][1]];
                            if (hex1.getOwner() != null && hex1.getOwner() != getCurrentPlayer() && hex1.getMilitaryUnit() != null) {
                                for (int k2 = 0; k2 < tempDirection2.length; k2++) {
                                    if (!isOutOfBounds(neighbor2.getX() + tempDirection1[j][0], neighbor2.getY() + tempDirection1[j][1])) {
                                        hex2 = hex[neighbor1.getX() + tempDirection1[i][0]][neighbor1.getY() + tempDirection1[i][1]];

                                        if (hex2.getOwner() != null && hex2.getOwner() != getCurrentPlayer() && hex2.getMilitaryUnit() != null)
                                            return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkForRiverOnWay(Movement movement, Hex nextHex) {
        int dx = nextHex.getX() - movement.getCurrentHex().getX();
        int dy = nextHex.getY() - movement.getCurrentHex().getY();

        if (dx == -1 && dy == -1 && movement.getCurrentHex().getHasRiver()[0])
            return true;
        if (dx == 0 && dy == -1 && movement.getCurrentHex().getHasRiver()[1])
            return true;
        if (dx == -1 && dy == 1 && movement.getCurrentHex().getHasRiver()[2])
            return true;
        if (dx == 0 && dy == 1 && movement.getCurrentHex().getHasRiver()[3])
            return true;
        return false;
    }


    public static String moveUnit(Movement movement) {
        JSONObject jsonObject = new JSONObject();

        Unit unit = movement.getUnit();
        Hex nextHex = getNextHex(movement);
        if (nextHex == null || (nextHex.getOwner() != null && nextHex.getOwner() != GameController.getCurrentPlayer())) {
            forceEndMovement(movement);
           jsonObject.put("movement result","the unit can't go further");
            return jsonObject.toString();
        }
        int x = nextHex.getX();
        int y = nextHex.getY();

        if ((unit.getCurrentHex().hasRoad() || unit.getCurrentHex().hasRailRoad())
                && (hex[x][y].hasRoad() || hex[x][y].hasRailRoad())
                && (int) (hex[x][y].getTerrain().getMovePoint() * 0.2) >= 0)
            unit.decreaseMP((int) (hex[x][y].getTerrain().getMovePoint() * 0.2));
        else if (checkForRiverOnWay(movement, nextHex))
            unit.setMP(0);
        else if (checkForZOC(hex[x][y]))
            unit.setMP(0);
        else if (hex[x][y].getTerrain().getMovePoint() >= 0)
            unit.decreaseMP(hex[x][y].getTerrain().getMovePoint());
        else {
            unfinishedMovements.remove(movement);
            jsonObject.put("movement result","the unit doesn't have enough move points");
            return jsonObject.toString();
        }

        setRevealedTiles();
        for (Unit playerUnit : getCurrentPlayer().getUnits()) {
            changeView(GameController.getDirection(playerUnit.getY()), playerUnit.getX(), playerUnit.getY());
        }

        if (unit.getState().equals(UnitState.Fortified))
            unit.setState(UnitState.Active);

        if (unit instanceof Civilian) {
            unit.getCurrentHex().setCivilianUnit(null);
            hex[x][y].setCivilianUnit((Civilian) unit);
            if(hex[x][y].getHasRuins()!=0)
            {
                activateRuin(hex[x][y].getHasRuins(), hex[x][y],jsonObject);
                jsonObject.put("had ruins",hex[x][y].getHasRuins());
                return jsonObject.toString();
            }
        } else {
            unit.getCurrentHex().setMilitaryUnit(null);
            hex[x][y].setMilitaryUnit((Military) unit);
            if(hex[x][y].getHasRuins()!=0)
            {
                jsonObject.put("had ruins",hex[x][y].getHasRuins());
                activateRuin(hex[x][y].getHasRuins(), hex[x][y],jsonObject);
                return jsonObject.toString();
            }
        }
        unit.changeCurrentHex(hex[x][y]);

        if (nextHex.getX() == movement.getDestination().getX() && nextHex.getY() == movement.getDestination().getY())
            unfinishedMovements.remove(movement);

        jsonObject.put("movement result","unit is on its way");
        return jsonObject.toString();
    }

    public static void activateRuin(int i, Hex hex, JSONObject jsonObject)
    {
        switch(i)
        {
            case 1:
                GameController.getCurrentPlayer().increasePopulation(1);
                break;
            case 2:
                int amount=(new Random().nextInt(5)+1)*100;
                jsonObject.put("amount2", amount);
                GameController.getCurrentPlayer().increaseGold(amount);
                break;
            case 3:
                GameController.getCurrentPlayer().increaseGold(89);
                UnitController.makeUnit("Settler", hex, "gold");
                Hex[][] temp=InitializeGameInfo.getWorld().getHex();
                boolean found=false;
                outer:
                for (int k = 0; k < getWorld().getHexInHeight(); k++)
                {
                    for (int j = 0; j < getWorld().getHexInWidth(); j++)
                    {
                        if(temp[k][j].getOwner().equals(GameController.getCurrentPlayer())&&temp[k][j].getCivilianUnit()==null)
                        {
                            GameController.getCurrentPlayer().increaseGold(89);
                            UnitController.makeUnit("Settler", hex, "gold");
                            found=true;
                            break outer;
                        }
                    }
                }
                if(!found)
                break;
            default:
                break;
        }

        hex.setRuinsValue(0);
    }


    public static String startMovement(int x, int y) {
        JSONObject jsonObject = new JSONObject();
        if (selectedUnit == null){
            jsonObject.put("movement result","you have to choose a unit first");
            return jsonObject.toString();
        }
        else if (isHexOccupied(x, y)){
            jsonObject.put("movement result","Destination hex already has a unit of this type");
            return jsonObject.toString();
        }
        else if (!canMoveThrough(x, y)){
            jsonObject.put("movement result","The unit can't go through chosen destination hex(motion/ocean)");
            return jsonObject.toString();
        }
        else if (selectedUnit instanceof Civilian && hex[x][y].getMilitaryUnit() != null && hex[x][y].getOwner() != getCurrentPlayer())
        {   jsonObject.put("movement result","a noncombat unit can't move to a tile with enemy military unit");
            return jsonObject.toString();
        }
        selectedUnit.setState(UnitState.Active);
        selectedUnit.setOrdered(true);
        Movement movement = new Movement(selectedUnit, selectedUnit.getCurrentHex(), hex[x][y]);
        unfinishedMovements.add(movement);
        return moveUnit(movement);
    }


    public static Hex getNextHex(Movement movement) {
        Unit unit = movement.getUnit();
        int[] direction;
        int[][] oddDirection = new int[][]{{-1, 0}, {0, -1}, {1, -1}, {1, 0}, {0, 1}, {1, 1}};
        int[][] evenDirection = new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, 0}, {-1, 1}, {0, 1}};
        int deltaX = movement.getDestination().getX() - unit.getCurrentHex().getX();
        int deltaY = movement.getDestination().getY() - unit.getCurrentHex().getY();

        if (unit.getCurrentHex().getY() % 2 == 0) {
            direction = getDirectionIndex(evenDirection, deltaX, deltaY, unit);
        } else {
            direction = getDirectionIndex(oddDirection, deltaX, deltaY, unit);
        }
        if (direction == null) {
            forceEndMovement(movement);
            return null;
        }
        return hex[unit.getCurrentHex().getX() + direction[0]][unit.getCurrentHex().getY() + direction[1]];
    }

    public static String promoteUnit(int x, int y) {
        if (isOutOfBounds(x, y))
            return "invalid location";
        if (hex[x][y].getMilitaryUnit() == null)
            return "there is no military unit in this tile";
        if (getCurrentPlayer().getGold() < 10)
            return "you don't have enough gold";

        Military military = hex[x][y].getMilitaryUnit();
        military.setCombatStrength(military.getCombatStrength() + 1);//promote military unit
        getCurrentPlayer().decreaseGold(10);
        if (hex[x][y].getCity().getGold() >= 10)
            hex[x][y].getCity().decreaseGold(10);
        else
            hex[x][y].getCity().decreaseGold(hex[x][y].getCity().getGold());

        return "unit promoted successfully";
    }

    public static void changeTurn() {
        for (int i = 0; i < unfinishedMovements.size(); i++) {
            if (unfinishedMovements.get(i).getUnit().getOwner() == getCurrentPlayer())
                moveUnit(unfinishedMovements.get(i));
        }
    }

    public static String selectUnit(int i, int j, String type) {
        switch (type){
            case  "military":{
                selectedUnit = hex[i][j].getMilitaryUnit();
                break;
            }
            case "civilian": {
                selectedUnit = hex[i][j].getCivilianUnit();
                break;
            }
        }
        return " ";
    }

    public static void increaseHealth(int amount)
    {
        // int amount=Integer.parseInt(health);
        selectedUnit.increaseHealth(amount);
    }
    public static String getHealth()
    {
        return Integer.toString(selectedUnit.getHealth());
    }
}