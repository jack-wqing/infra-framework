package com.jindi.infra.space.oneservice;

import com.jindi.infra.dataapi.oneservice.annotation.OneService;
import com.jindi.infra.dataapi.oneservice.annotation.OneServiceApi;
import com.jindi.infra.dataapi.oneservice.param.OneServiceDTO;
import com.jindi.infra.dataapi.oneservice.query.QueryOneService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OneService(project = "test_for_online", folder = "test_for_online")
public class OneServiceQuery extends QueryOneService {

    @OneServiceApi(api = "test_for_online")
    public List<String> queryList() {
        OneServiceDTO oneServiceDTO = new OneServiceDTO();
        oneServiceDTO.put("keyword", 1);
        return queryList(oneServiceDTO, String.class);
    }

}
