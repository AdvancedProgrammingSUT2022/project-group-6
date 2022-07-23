package serverapp.models.gainable;

import java.util.ArrayList;

import serverapp.controllers.InitializeGameInfo;
import serverapp.models.Player;
import serverapp.models.maprelated.Hex;
import serverapp.models.units.Unit;
import serverapp.models.units.Worker;

public class Technology implements Construction {
    private String name;
    private int cost;
    private ArrayList<String> neededPreviousTechnologies = new ArrayList<>();
    private int leftTurns;
    private Player owner;

    public Technology(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        String[] info = InitializeGameInfo.getTechnologyInfo().get(name).split(" ");
        this.cost = Integer.parseInt(info[0]);

        String[] neededTechnologies = info[1].split(",");
        for (String temp : neededTechnologies) {
            neededPreviousTechnologies.add(temp);
        }

    }

    @Override
    public int getLeftTurns() {
        return this.leftTurns;
    }

    @Override
    public void decreaseLeftTurns() {
        this.leftTurns -= 1;
    }

    @Override
    public void setLeftTurns(int leftTurns) {
        this.leftTurns = leftTurns;
    }

    public ArrayList<String> getNeededPreviousTechnologies() {
        return neededPreviousTechnologies;
    }

    public String getName() {
        return this.name;
    }

    public int getCost() {
        return this.cost;
    }

    @Override
    public void build(String type) {
        owner.unlockTechnology(this.name);
    }

    @Override
    public Hex getHex() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void zeroMpWorker() {
        // TODO Auto-generated method stub

    }

    @Override
    public Unit getWorker() {
        // TODO Auto-generated method stub
        return null;
    }


}