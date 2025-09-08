package com.kh.mvidia.notion.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class NotionDao {

    public String selectTemplateUrl(SqlSession sqlSession, String templateRef) {
        return sqlSession.selectOne("salaryMapper.selectTemplateUrl", templateRef);
    }
}