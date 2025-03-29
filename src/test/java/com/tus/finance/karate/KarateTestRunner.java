package com.tus.finance.karate;
import org.springframework.boot.test.context.SpringBootTest;

import com.intuit.karate.junit5.Karate;

@SpringBootTest
public class KarateTestRunner {

    @Karate.Test
    Karate testAll() {
        return Karate.run().relativeTo(getClass());
    }
}
