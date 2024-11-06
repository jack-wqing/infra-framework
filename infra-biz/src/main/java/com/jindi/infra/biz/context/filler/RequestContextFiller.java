package com.jindi.infra.biz.context.filler;


import javax.servlet.http.HttpServletRequest;

public interface RequestContextFiller {

    void fill(HttpServletRequest request);

}
