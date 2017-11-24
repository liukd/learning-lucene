package com.liukd.index;

import org.junit.Test;

public class IndexUtilTest {

    @Test
    public void testIndex(){
        IndexUtil iu = new IndexUtil();
        iu.index();
    }


    /**
     *
     */
    @Test
    public void testQuery(){
        IndexUtil iu = new IndexUtil();
        iu.query();
    }

    @Test
    public void testSearch(){
        IndexUtil iu = new IndexUtil();
        iu.search();
    }

    @Test
    public void testDelete(){
        IndexUtil iu = new IndexUtil();
        iu.delete();
    }

    @Test
    public void testUndelete(){
        IndexUtil iu = new IndexUtil();
        iu.undelete();
    }
}
