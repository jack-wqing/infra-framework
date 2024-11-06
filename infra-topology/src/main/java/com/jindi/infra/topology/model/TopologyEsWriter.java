package com.jindi.infra.topology.model;

import com.jindi.infra.logger.elasticsearch.ElasticSearchWriter;
import com.jindi.infra.topology.consts.TopologyConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TopologyEsWriter {

    @Resource
    public ElasticSearchWriter elasticSearchWriter;

    public void writeException(Throwable cause, String name) {
        if (elasticSearchWriter != null) {
            String stackTrace = ExceptionUtils.getStackTrace(cause);
            elasticSearchWriter.write(TopologyConst.ES_TYPE, name, stackTrace);
        }
    }

}
