package com.jindi.infra.dataapi.spacecloud.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CompositeItem {

    private String field;

    private String op;

    private Object value;

    public CompositeItem(String field, String op, Object value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }
}
