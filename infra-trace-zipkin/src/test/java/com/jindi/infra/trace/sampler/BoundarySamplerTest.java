package com.jindi.infra.trace.sampler;

import java.util.Random;

/**
 * @Author:liuwenqing
 * @Date:2024/10/31 15:09
 * @Description:
 **/
public class BoundarySamplerTest {

    public static void main(String[] args) {

        long SALT = new Random().nextLong();
        System.out.println(SALT);

    }

}
