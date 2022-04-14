package me.isaac.defencetowers.tower;

public enum TargetType {
    CLOSEST("Closest"),
    FARTHEST("Farthest"),
    STRONGEST("Strongest"),
    WEAKEST("Weakest"),
    MOST_HEALTH("Most Health"),
    LEAST_HEALTH("Least Health");

    final String string;

    TargetType(String string) {
        this.string = string;
    }

    public String toString() {
        return string;
    }

}
