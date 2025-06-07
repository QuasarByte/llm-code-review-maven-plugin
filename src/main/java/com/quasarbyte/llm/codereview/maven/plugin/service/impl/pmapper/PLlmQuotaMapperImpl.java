package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PLlmQuota;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PLlmQuotaMapper;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmQuota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PLlmQuotaMapperImpl implements PLlmQuotaMapper {

    private static final Logger logger = LoggerFactory.getLogger(PLlmQuotaMapperImpl.class);

    @Override
    public LlmQuota map(PLlmQuota quota) {
        logger.info("Mapping PLlmQuota: {}", quota != null ? format(quota) : "null");
        if (quota == null) {
            logger.warn("Provided PLlmQuota is null, returning null.");
            return null;
        } else {
            logger.debug("Mapping requestQuota: {}", quota.getRequestQuota());
            LlmQuota result = new LlmQuota().setRequestQuota(quota.getRequestQuota());
            logger.info("Mapped PLlmQuota to LlmQuota with requestQuota: {}", result.getRequestQuota());
            return result;
        }
    }

    private static String format(PLlmQuota quota) {
        return "PLlmQuota{" +
                "requestQuota=" + quota.getRequestQuota() +
                '}';
    }
}
