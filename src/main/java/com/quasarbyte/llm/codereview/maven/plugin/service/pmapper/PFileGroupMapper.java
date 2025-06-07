package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PFileGroup;
import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;

public interface PFileGroupMapper {
    FileGroup map(PFileGroup fileGroup);
}
