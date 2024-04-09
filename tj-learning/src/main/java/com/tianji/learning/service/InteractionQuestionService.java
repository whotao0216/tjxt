package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;

/**
 * @author lyh
 * @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Service
 * @createDate 2024-04-09 21:27:39
 */
public interface InteractionQuestionService extends IService<InteractionQuestion> {
    public void saveQuestion(QuestionFormDTO dto);

    public void editQuestion(Long id, QuestionFormDTO dto);

    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query);
}
