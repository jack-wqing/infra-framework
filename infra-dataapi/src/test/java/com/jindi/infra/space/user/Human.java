package com.jindi.infra.space.user;

import lombok.Data;

@Data
public class Human {
    private Long id;
    private String human_id;
    private String human_name;
    private Integer total_post_counts;
}
