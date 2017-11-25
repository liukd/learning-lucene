package com.liukd.index;

import org.junit.Before;
import org.junit.Test;

public class SearchUtilTest {

    private SearchUtil su ;

    @Before
    public void init(){
        su = new SearchUtil();
    }

    /**
     * 总共查询到的值和参数num没有关系的
     */
    @Test
    public void searchByTerm(){
        su.searchByTerm("name", "zhang", 3);
    }

    @Test
    public void searchBytermRange(){
        // 开区间和闭区间的
        //su.searchBytermRange("id", "2", "4", 10);

        // 字符串比较大小，使用compareTo方法
        System.out.println("lhangsan".compareTo("a") + " / " + "lhangsan".compareTo("n"));
        System.out.println("zhaoliu".compareTo("a") + " / " + "zhaoliu".compareTo("n"));
        su.searchByTermRange("name", "a", "n", true, true);
    }

    @Test
    public void searchByNumericRange(){
        su.searchByNumericRange("attach", 2, 5, true, false, 10);
    }

    @Test
    public void searchByPrefix(){
        su.searchByPrefixQuery("name", "zhang");
    }

    @Test
    public void searchByWildCard(){
        su.searchByWildcard("name", "?a*");
    }

    @Test
    public void searchByBoolean(){
        su.searchByBoolean();
    }

    @Test
    public void searchByFuzzyQuery(){
        su.searchByFuzzyQuery();
    }



}
