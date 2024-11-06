package com.jindi.infra.space.constant;

public enum GraphQLTypeEnums {

    SPACE_CLOUD("SpaceCloud"),
    ROMA("Roma");


    private String name;

    GraphQLTypeEnums(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
