package net.moddedminecraft.mmctickets.data;


public enum ticketStatus {
    Open("Open"), Claimed("Claimed"), Held("Held"), Closed("Closed");

    private String status;

    ticketStatus(String stat) {
        this.status = stat;
    }

    @Override
    public String toString(){
        return status;
    }
}
