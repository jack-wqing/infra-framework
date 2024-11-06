package com.jindi.infra.space.param;

import com.jindi.infra.space.QuerySpaceCloud;
import com.jindi.infra.space.wrapper.WhereWrapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SpaceRequestDTOTest extends QuerySpaceCloud {

    @Test
    void test_getRomaParams_singleParam_eq() {
        String sql = " id = 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.eq("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_eq_string() {
        String sql = " company_name = '测试公司名称1'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.eq("company_name", "测试公司名称1");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_ne() {
        String sql = " id <> 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.ne("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_ne_string() {
        String sql = " company_name <> '测试公司名称1'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.ne("company_name", "测试公司名称1");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_isNull() {
        String sql = " id IS NULL";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.isNull("id", true);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_isNotNull() {
        String sql = " id IS NOT NULL";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.isNull("id", false);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_gt() {
        String sql = " id > 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.gt("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_gte() {
        String sql = " id >= 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.gte("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_lt() {
        String sql = " id < 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.lt("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_lte() {
        String sql = " id <= 1";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.lte("id", 1L);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_in_long() {
        String sql = " id in ('1', '2', '3')";
        WhereWrapper wrapper = new WhereWrapper();
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);
        wrapper.in("id", ids);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_in_string() {
        String sql = " company_name in ('测试公司名称2', '测试公司名称1', '测试公司名称2')";
        WhereWrapper wrapper = new WhereWrapper();
        List<String> names = new ArrayList<>();
        names.add("测试公司名称2");
        names.add("测试公司名称1");
        names.add("测试公司名称2");
        wrapper.in("company_name", names);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_not_in_long() {
        String sql = " id not in ('1', '2', '3')";
        WhereWrapper wrapper = new WhereWrapper();
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);
        wrapper.nin("id", ids);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_not_in_string() {
        String sql = " company_name not in ('测试公司名称2', '测试公司名称1', '测试公司名称2')";
        WhereWrapper wrapper = new WhereWrapper();
        List<String> names = new ArrayList<>();
        names.add("测试公司名称2");
        names.add("测试公司名称1");
        names.add("测试公司名称2");
        wrapper.nin("company_name", names);
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_like() {
        String sql = " company_name like '%测试公司名称1%'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.like("company_name", "测试公司名称1");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_lLike() {
        String sql = " company_name like '测试公司名称1%'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.lLike("company_name", "测试公司名称1");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_singleParam_rLike() {
        String sql = " company_name like '%测试公司名称1'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.rLike("company_name", "测试公司名称1");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }


    @Test
    void test_getRomaParams_multiParams_and() {
        String sql = " id = 1 and company_name <> '测试公司名称1' and area_code > 0 and type < 2 and company_desc like '%测试like%'";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.eq("id", 1);
        wrapper.ne("company_name", "测试公司名称1");
        wrapper.gt("area_code", 0);
        wrapper.lt("type", 2);
        wrapper.like("company_desc", "测试like");
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(true).get("where"));
    }

    @Test
    void test_getRomaParams_multiParams_or() {
        String sql = "(company_name <> '测试公司名称1' and area_code > 0 and type < 2 and company_desc like '%测试like%')";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.or(w -> {
            w.ne("company_name", "测试公司名称1");
            w.gt("area_code", 0);
            w.lt("type", 2);
            w.like("company_desc", "测试like");
        });
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(false).get("where"));
    }


    @Test
    void test_getRomaParams_complexParams() {
        String sql = " id = 1 or (company_name <> '测试公司名称1' and area_code > 0 and type < 2 and company_desc like '%测试like%') and (company_name <> '测试公司名称1' and area_code > 0 and type < 2 and company_desc like '%测试like%') or (area_code > 0 or (company_name <> '测试公司名称1' and area_code > 0) and (company_name <> '测试公司名称1' and area_code > 0)) and (area_code > 0 or (company_name <> '测试公司名称1' and area_code > 0) and (company_name <> '测试公司名称1' and area_code > 0))";
        WhereWrapper wrapper = new WhereWrapper();
        wrapper.eq("id", 1);
        wrapper.or(w -> {
            w.ne("company_name", "测试公司名称1");
            w.gt("area_code", 0);
            w.lt("type", 2);
            w.like("company_desc", "测试like");
        });
        wrapper.and(w -> {
            w.ne("company_name", "测试公司名称1");
            w.gt("area_code", 0);
            w.lt("type", 2);
            w.like("company_desc", "测试like");
        });
        wrapper.or(w -> {
            w.gt("area_code", 0);
            w.or(m -> {
                m.ne("company_name", "测试公司名称1");
                m.gt("area_code", 0);
            });
            w.and(m -> {
                m.ne("company_name", "测试公司名称1");
                m.gt("area_code", 0);
            });
        });
        wrapper.and(w -> {
            w.gt("area_code", 0);
            w.or(m -> {
                m.ne("company_name", "测试公司名称1");
                m.gt("area_code", 0);
            });
            w.and(m -> {
                m.ne("company_name", "测试公司名称1");
                m.gt("area_code", 0);
            });
        });
        WhereComposite whereComposite = buildWhereComposite(wrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(false).get("where"));
    }

    @Test
    void test_getRomaParams_or(){
        String sql = " ((company_id = 3323992056 and human_name = '秦某某') or (company_id = 2127827572 and human_name = '胡某某'))";
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.and(whereParam ->
                    whereParam.or(whereWrapperNew -> whereWrapperNew.eq("company_id",3323992056L).eq("human_name", "秦某某"))
                            .or(whereWrapperNew -> whereWrapperNew.eq("company_id",2127827572).eq("human_name", "胡某某"))
        );

        WhereComposite whereComposite = buildWhereComposite(whereWrapper);
        Assert.assertEquals(sql, whereComposite.getRomaParams(false).get("where"));
    }

}
