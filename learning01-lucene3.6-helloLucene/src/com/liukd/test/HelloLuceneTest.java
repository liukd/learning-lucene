package com.liukd.test;

import org.junit.Test;

public class HelloLuceneTest {

    @Test
    public void testIndex(){

        HelloLucene hl = new HelloLucene();
        hl.index();
    }

    @Test
    public void testSearch(){
        HelloLucene hl = new HelloLucene();
        hl.search();
    }
}
